package com.example.player

import com.example.data.model.Channel

sealed class PlaybackStatus {
    object Idle : PlaybackStatus()
    object Connecting : PlaybackStatus()
    object Buffering : PlaybackStatus()
    object Ready : PlaybackStatus()
    object Ended : PlaybackStatus()
    data class Error(
        val errorType: StreamErrorType,
        val message: String,
        val canRetry: Boolean = true,
        val retryAttempt: Int = 0,
        val maxRetries: Int = 3
    ) : PlaybackStatus()
}

enum class StreamErrorType(val title: String) {
    NETWORK_TIMEOUT("Connection Timeout"),
    RESTRICTED("Stream Access Restricted"),
    UNAVAILABLE("Channel Feed Offline"),
    UNSUPPORTED_FORMAT("Format Not Supported"),
    UNKNOWN("Playback Error")
}

data class StreamQualityInfo(
    val width: Int = 0,
    val height: Int = 0,
    val bitrate: Long = 0L,
    val isAdaptive: Boolean = true
) {
    val resolutionLabel: String
        get() = when {
            height >= 1080 -> "1080p FHD"
            height >= 720 -> "720p HD"
            height >= 480 -> "480p SD"
            height > 0 -> "${height}p"
            else -> "Auto Live"
        }
}

data class ActivePlayerState(
    val currentChannel: Channel? = null,
    val playbackStatus: PlaybackStatus = PlaybackStatus.Idle,
    val isPlaying: Boolean = false,
    val isFullscreen: Boolean = false,
    val qualityInfo: StreamQualityInfo = StreamQualityInfo(),
    val controlsVisible: Boolean = true
)
