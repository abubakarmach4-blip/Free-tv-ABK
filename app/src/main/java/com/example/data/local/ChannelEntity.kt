package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val category: String,
    val country: String? = null,
    val language: String? = null,
    val groupTitle: String? = null,
    val isVerified: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val channelId: String,
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_watches")
data class RecentEntity(
    @PrimaryKey val channelId: String,
    val watchedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_sources")
data class PlaylistSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val isEnabled: Boolean = true,
    val lastSyncTime: Long = 0L
)
