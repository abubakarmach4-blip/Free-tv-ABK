package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY isVerified DESC, name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE category = :category ORDER BY isVerified DESC, name ASC")
    fun getChannelsByCategory(category: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%' ORDER BY isVerified DESC, name ASC")
    fun searchChannels(query: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: String): ChannelEntity?

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getChannelCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels")
    suspend fun clearChannels()
}

@Dao
interface FavoriteDao {
    @Query("SELECT channelId FROM favorites ORDER BY addedTime DESC")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE channelId = :channelId)")
    suspend fun isFavorite(channelId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE channelId = :channelId")
    suspend fun removeFavorite(channelId: String)
}

@Dao
interface RecentDao {
    @Query("SELECT channelId FROM recent_watches ORDER BY watchedTime DESC LIMIT 25")
    fun getRecentChannelIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentEntity)

    @Query("DELETE FROM recent_watches")
    suspend fun clearRecents()
}

@Dao
interface PlaylistSourceDao {
    @Query("SELECT * FROM playlist_sources ORDER BY id ASC")
    fun getAllSources(): Flow<List<PlaylistSourceEntity>>

    @Query("SELECT * FROM playlist_sources WHERE isEnabled = 1")
    suspend fun getActiveSources(): List<PlaylistSourceEntity>

    @Query("SELECT COUNT(*) FROM playlist_sources")
    suspend fun getSourceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: PlaylistSourceEntity)

    @Update
    suspend fun updateSource(source: PlaylistSourceEntity)

    @Query("DELETE FROM playlist_sources WHERE id = :id")
    suspend fun deleteSource(id: Int)
}
