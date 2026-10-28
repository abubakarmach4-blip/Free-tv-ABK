package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ChannelEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.InitialChannels
import com.example.data.local.PlaylistSourceEntity
import com.example.data.local.RecentEntity
import com.example.data.model.Channel
import com.example.data.model.ChannelCategories
import com.example.data.network.M3uParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val message: String) : SyncState()
    data class Success(val channelCount: Int, val sourceName: String) : SyncState()
    data class Error(val error: String, val showingCached: Boolean) : SyncState()
}

class ChannelRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val database = AppDatabase.getInstance(context)
    private val channelDao = database.channelDao()
    private val favoriteDao = database.favoriteDao()
    private val recentDao = database.recentDao()
    private val playlistSourceDao = database.playlistSourceDao()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        // Initial check and bootstrap from IO
    }

    suspend fun initializeDatabase() = withContext(ioDispatcher) {
        // Seed verified initial channels immediately so user has zero waiting time
        // and updated working stream URLs overwrite stale cached entries
        val seedChannels = InitialChannels.getSeedChannels()
        channelDao.insertChannels(seedChannels)

        if (playlistSourceDao.getSourceCount() == 0) {
            playlistSourceDao.insertSource(
                PlaylistSourceEntity(
                    name = "IPTV-Org Public Index",
                    url = InitialChannels.DEFAULT_PLAYLIST_URL,
                    isEnabled = true,
                    lastSyncTime = 0L
                )
            )
        }
    }

    fun getChannelsStream(
        selectedCategory: String,
        searchQuery: String,
        favoritesOnly: Boolean = false,
        recentsOnly: Boolean = false
    ): Flow<List<Channel>> {
        val baseFlow: Flow<List<ChannelEntity>> = when {
            searchQuery.isNotBlank() -> channelDao.searchChannels(searchQuery.trim())
            selectedCategory == ChannelCategories.ALL -> channelDao.getAllChannels()
            else -> channelDao.getChannelsByCategory(selectedCategory)
        }

        return combine(
            baseFlow,
            favoriteDao.getAllFavoriteIds(),
            recentDao.getRecentChannelIds()
        ) { channels, favIds, recentIds ->
            val favSet = favIds.toSet()
            val recentMap = recentIds.mapIndexed { index, id -> id to (recentIds.size - index).toLong() }.toMap()

            var result = channels.map { entity ->
                Channel(
                    id = entity.id,
                    name = entity.name,
                    streamUrl = entity.streamUrl,
                    logoUrl = entity.logoUrl,
                    category = entity.category,
                    country = entity.country,
                    language = entity.language,
                    groupTitle = entity.groupTitle,
                    isVerified = entity.isVerified,
                    isFavorite = favSet.contains(entity.id),
                    lastWatchedTime = recentMap[entity.id]
                )
            }

            if (favoritesOnly) {
                result = result.filter { it.isFavorite }
            }

            if (recentsOnly) {
                result = result.filter { recentMap.containsKey(it.id) }
                    .sortedByDescending { it.lastWatchedTime ?: 0L }
            }

            result
        }
    }

    fun getFavoritesStream(): Flow<List<Channel>> {
        return getChannelsStream(
            selectedCategory = ChannelCategories.ALL,
            searchQuery = "",
            favoritesOnly = true
        )
    }

    fun getRecentStream(): Flow<List<Channel>> {
        return getChannelsStream(
            selectedCategory = ChannelCategories.ALL,
            searchQuery = "",
            recentsOnly = true
        )
    }

    fun getPlaylistSources(): Flow<List<PlaylistSourceEntity>> {
        return playlistSourceDao.getAllSources()
    }

    suspend fun refreshCatalog(customUrl: String? = null) = withContext(ioDispatcher) {
        _syncState.value = SyncState.Syncing("Connecting to public playlist feed...")
        val targetUrl = customUrl ?: InitialChannels.DEFAULT_PLAYLIST_URL

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile) AutoStreamTV/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = "HTTP ${response.code}: Failed to download playlist"
                _syncState.value = SyncState.Error(errorMsg, showingCached = true)
                response.close()
                return@withContext
            }

            val body = response.body
            if (body == null) {
                _syncState.value = SyncState.Error("Empty playlist response", showingCached = true)
                return@withContext
            }

            _syncState.value = SyncState.Syncing("Parsing channel catalog...")
            val parsedChannels = body.byteStream().use { inputStream ->
                M3uParser.parse(inputStream)
            }

            if (parsedChannels.isEmpty()) {
                _syncState.value = SyncState.Error("No valid streams found in playlist", showingCached = true)
                return@withContext
            }

            // Keep seed verified channels and merge newly discovered channels
            val seedMap = InitialChannels.getSeedChannels().associateBy { it.id }
            val mergedChannels = parsedChannels.map { parsed ->
                seedMap[parsed.id]?.copy(lastUpdated = System.currentTimeMillis()) ?: parsed
            }.toMutableList()

            // Ensure seed channels remain present
            seedMap.values.forEach { seed ->
                if (mergedChannels.none { it.id == seed.id }) {
                    mergedChannels.add(0, seed)
                }
            }

            channelDao.insertChannels(mergedChannels)
            _syncState.value = SyncState.Success(mergedChannels.size, "AutoStream TV Catalog")
            Log.d("ChannelRepository", "Successfully synced ${mergedChannels.size} channels.")
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error refreshing playlist: ${e.message}", e)
            _syncState.value = SyncState.Error(
                error = e.localizedMessage ?: "Network connection error",
                showingCached = true
            )
        }
    }

    suspend fun toggleFavorite(channelId: String) = withContext(ioDispatcher) {
        if (favoriteDao.isFavorite(channelId)) {
            favoriteDao.removeFavorite(channelId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(channelId = channelId))
        }
    }

    suspend fun recordWatch(channelId: String) = withContext(ioDispatcher) {
        recentDao.insertRecent(RecentEntity(channelId = channelId))
    }

    suspend fun addCustomPlaylistSource(name: String, url: String) = withContext(ioDispatcher) {
        playlistSourceDao.insertSource(
            PlaylistSourceEntity(
                name = name,
                url = url,
                isEnabled = true
            )
        )
        refreshCatalog(url)
    }

    suspend fun removePlaylistSource(id: Int) = withContext(ioDispatcher) {
        playlistSourceDao.deleteSource(id)
    }
}
