package com.example.ui.viewmodel

import android.app.Application
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.example.data.local.PlaylistSourceEntity
import com.example.data.model.Channel
import com.example.data.model.ChannelCategories
import com.example.data.repository.ChannelRepository
import com.example.data.repository.SyncState
import com.example.player.ActivePlayerState
import com.example.player.ExoPlayerHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChannelRepository(application)

    private val _selectedCategory = MutableStateFlow(ChannelCategories.ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showPlaylistDialog = MutableStateFlow(false)
    val showPlaylistDialog: StateFlow<Boolean> = _showPlaylistDialog.asStateFlow()

    private val exoPlayerHelper = ExoPlayerHelper(
        context = application,
        onChannelWatched = { channelId ->
            viewModelScope.launch {
                repository.recordWatch(channelId)
            }
        }
    )

    val playerState: StateFlow<ActivePlayerState> = exoPlayerHelper.playerState
    val syncState: StateFlow<SyncState> = repository.syncState

    val playlistSources: StateFlow<List<PlaylistSourceEntity>> = repository.getPlaylistSources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favorites: StateFlow<List<Channel>> = repository.getFavoritesStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentChannels: StateFlow<List<Channel>> = repository.getRecentStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val channels: StateFlow<List<Channel>> = kotlinx.coroutines.flow.combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        category to query
    }.flatMapLatest { (category, query) ->
        repository.getChannelsStream(
            selectedCategory = category,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
        }
    }

    fun getExoPlayer() = exoPlayerHelper.getPlayer()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshCatalog(customUrl: String? = null) {
        viewModelScope.launch {
            repository.refreshCatalog(customUrl)
        }
    }

    fun playChannel(channel: Channel) {
        exoPlayerHelper.playChannel(channel)
    }

    fun playNextChannel() {
        val current = playerState.value.currentChannel ?: return
        val list = channels.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex + 1 < list.size) {
            playChannel(list[currentIndex + 1])
        } else if (list.isNotEmpty()) {
            playChannel(list[0])
        }
    }

    fun playPreviousChannel() {
        val current = playerState.value.currentChannel ?: return
        val list = channels.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            playChannel(list[currentIndex - 1])
        } else if (list.isNotEmpty()) {
            playChannel(list.last())
        }
    }

    fun retryPlayback() {
        exoPlayerHelper.retryCurrent()
    }

    fun togglePlayPause() {
        exoPlayerHelper.togglePlayPause()
    }

    fun toggleFullscreen() {
        exoPlayerHelper.toggleFullscreen()
    }

    fun setFullscreen(fullscreen: Boolean) {
        exoPlayerHelper.setFullscreen(fullscreen)
    }

    fun setControlsVisible(visible: Boolean) {
        exoPlayerHelper.setControlsVisible(visible)
    }

    fun closePlayer() {
        exoPlayerHelper.closePlayer()
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            repository.toggleFavorite(channel.id)
        }
    }

    fun setShowPlaylistDialog(show: Boolean) {
        _showPlaylistDialog.value = show
    }

    fun addCustomPlaylistSource(name: String, url: String) {
        viewModelScope.launch {
            repository.addCustomPlaylistSource(name, url)
        }
    }

    fun removePlaylistSource(id: Int) {
        viewModelScope.launch {
            repository.removePlaylistSource(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayerHelper.release()
    }
}
