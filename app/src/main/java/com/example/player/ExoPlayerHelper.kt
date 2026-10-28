package com.example.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.data.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class ExoPlayerHelper(
    private val context: Context,
    private val onChannelWatched: (String) -> Unit
) {
    private var exoPlayer: ExoPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _playerState = MutableStateFlow(ActivePlayerState())
    val playerState: StateFlow<ActivePlayerState> = _playerState.asStateFlow()

    private var retryAttemptCount = 0
    private val maxAutoRetries = 2
    private var isDisposed = false

    fun getPlayer(): ExoPlayer {
        return exoPlayer ?: createPlayer().also { exoPlayer = it }
    }

    private fun createPlayer(): ExoPlayer {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 AutoStreamTV/1.0")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(
                mapOf(
                    "Accept" to "*/*",
                    "Connection" to "keep-alive"
                )
            )

        val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15000,
                /* maxBufferMs = */ 45000,
                /* bufferForPlaybackMs = */ 2000,
                /* bufferForPlaybackAfterRebufferMs = */ 4000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setForceHighestSupportedBitrate(false)
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setAllowVideoNonSeamlessAdaptiveness(true)
            )
        }

        val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()

        player.addListener(playerListener)
        player.playWhenReady = true
        return player
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> "UNKNOWN($playbackState)"
            }
            Log.d("AutoStreamTV_Player", "onPlaybackStateChanged: $stateName, playWhenReady=${exoPlayer?.playWhenReady}")

            when (playbackState) {
                Player.STATE_IDLE -> {
                    // Do not overwrite Connecting state with Idle while player is preparing
                    if (_playerState.value.playbackStatus !is PlaybackStatus.Connecting &&
                        _playerState.value.playbackStatus !is PlaybackStatus.Error
                    ) {
                        _playerState.value = _playerState.value.copy(
                            playbackStatus = PlaybackStatus.Idle,
                            isPlaying = false
                        )
                    }
                }
                Player.STATE_BUFFERING -> {
                    _playerState.value = _playerState.value.copy(
                        playbackStatus = PlaybackStatus.Buffering
                    )
                }
                Player.STATE_READY -> {
                    retryAttemptCount = 0
                    Log.i("AutoStreamTV_Player", "Stream READY and playing for: ${_playerState.value.currentChannel?.name}")
                    _playerState.value = _playerState.value.copy(
                        playbackStatus = PlaybackStatus.Ready,
                        isPlaying = exoPlayer?.isPlaying == true
                    )
                    _playerState.value.currentChannel?.id?.let { onChannelWatched(it) }
                }
                Player.STATE_ENDED -> {
                    _playerState.value = _playerState.value.copy(
                        playbackStatus = PlaybackStatus.Ended,
                        isPlaying = false
                    )
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d("AutoStreamTV_Player", "onIsPlayingChanged: $isPlaying")
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            if (videoSize.width > 0 && videoSize.height > 0) {
                Log.d("AutoStreamTV_Player", "Video size: ${videoSize.width}x${videoSize.height}")
                _playerState.value = _playerState.value.copy(
                    qualityInfo = _playerState.value.qualityInfo.copy(
                        width = videoSize.width,
                        height = videoSize.height
                    )
                )
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_VIDEO && group.isSelected) {
                    for (i in 0 until group.length) {
                        if (group.isTrackSelected(i)) {
                            val format = group.getTrackFormat(i)
                            _playerState.value = _playerState.value.copy(
                                qualityInfo = _playerState.value.qualityInfo.copy(
                                    width = if (format.width != androidx.media3.common.Format.NO_VALUE) format.width else _playerState.value.qualityInfo.width,
                                    height = if (format.height != androidx.media3.common.Format.NO_VALUE) format.height else _playerState.value.qualityInfo.height,
                                    bitrate = if (format.bitrate != androidx.media3.common.Format.NO_VALUE) format.bitrate.toLong() else 0L,
                                    isAdaptive = group.isAdaptiveSupported
                                )
                            )
                            Log.d("AutoStreamTV_Player", "Selected track format: ${format.width}x${format.height}, bitrate=${format.bitrate}")
                            break
                        }
                    }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val currentChannel = _playerState.value.currentChannel
            Log.e("AutoStreamTV_Player", "==========================================================")
            Log.e("AutoStreamTV_Player", "[PLAYBACK ERROR] Channel: ${currentChannel?.name}")
            Log.e("AutoStreamTV_Player", "[PLAYBACK ERROR] Stream URL: ${currentChannel?.streamUrl}")
            Log.e("AutoStreamTV_Player", "[PLAYBACK ERROR] Code: ${error.errorCode} (${error.errorCodeName})")
            Log.e("AutoStreamTV_Player", "[PLAYBACK ERROR] Exception message: ${error.message}")
            Log.e("AutoStreamTV_Player", "[PLAYBACK ERROR] Cause: ${error.cause?.javaClass?.name}: ${error.cause?.message}")

            if (error.cause is HttpDataSource.InvalidResponseCodeException) {
                val httpError = error.cause as HttpDataSource.InvalidResponseCodeException
                Log.e("AutoStreamTV_Player", "[PLAYBACK ERROR] HTTP Response Code: ${httpError.responseCode}")
                Log.e("AutoStreamTV_Player", "[PLAYBACK ERROR] HTTP Response Message: ${httpError.responseMessage}")
                Log.e("AutoStreamTV_Player", "[PLAYBACK ERROR] Response Headers: ${httpError.headerFields}")
            }
            Log.e("AutoStreamTV_Player", "==========================================================")

            // Behind live window recovery
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                Log.d("AutoStreamTV_Player", "Behind live window, seeking to default position and re-preparing...")
                exoPlayer?.seekToDefaultPosition()
                exoPlayer?.prepare()
                return
            }

            val errorType = classifyError(error)
            val isTemporary = errorType == StreamErrorType.NETWORK_TIMEOUT

            if (isTemporary && retryAttemptCount < maxAutoRetries && !isDisposed) {
                retryAttemptCount++
                val delayMs = 1500L * retryAttemptCount
                Log.d("AutoStreamTV_Player", "Auto-retrying stream in $delayMs ms (attempt $retryAttemptCount of $maxAutoRetries)...")

                _playerState.value = _playerState.value.copy(
                    playbackStatus = PlaybackStatus.Error(
                        errorType = errorType,
                        message = "Connection blip. Reconnecting (${retryAttemptCount}/$maxAutoRetries)...",
                        canRetry = true,
                        retryAttempt = retryAttemptCount,
                        maxRetries = maxAutoRetries
                    )
                )

                mainHandler.postDelayed({
                    if (!isDisposed && _playerState.value.currentChannel != null) {
                        exoPlayer?.prepare()
                        exoPlayer?.play()
                    }
                }, delayMs)
            } else {
                val diagnosticMessage = when (errorType) {
                    StreamErrorType.NETWORK_TIMEOUT -> "Network connection timed out. Server may be unresponsive or connection dropped."
                    StreamErrorType.RESTRICTED -> "Stream is geo-restricted or unauthorized by broadcaster (HTTP 401/403)."
                    StreamErrorType.UNAVAILABLE -> "Broadcaster live stream feed is currently offline or unreachable (HTTP 404)."
                    StreamErrorType.UNSUPPORTED_FORMAT -> "Stream media format/codec is not supported on this device."
                    StreamErrorType.UNKNOWN -> "Playback encountered an issue: ${error.message ?: "Unknown error"}"
                }

                _playerState.value = _playerState.value.copy(
                    playbackStatus = PlaybackStatus.Error(
                        errorType = errorType,
                        message = diagnosticMessage,
                        canRetry = true,
                        retryAttempt = retryAttemptCount,
                        maxRetries = maxAutoRetries
                    ),
                    isPlaying = false
                )
            }
        }
    }

    private fun classifyError(error: PlaybackException): StreamErrorType {
        if (error.cause is HttpDataSource.InvalidResponseCodeException) {
            val code = (error.cause as HttpDataSource.InvalidResponseCodeException).responseCode
            return when (code) {
                401, 403, 451 -> StreamErrorType.RESTRICTED
                404, 410 -> StreamErrorType.UNAVAILABLE
                else -> StreamErrorType.NETWORK_TIMEOUT
            }
        }

        return when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> StreamErrorType.NETWORK_TIMEOUT

            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                val msg = error.message?.lowercase() ?: ""
                when {
                    msg.contains("403") || msg.contains("401") || msg.contains("451") -> StreamErrorType.RESTRICTED
                    msg.contains("404") || msg.contains("410") -> StreamErrorType.UNAVAILABLE
                    else -> StreamErrorType.NETWORK_TIMEOUT
                }
            }

            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> StreamErrorType.UNSUPPORTED_FORMAT

            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> StreamErrorType.UNAVAILABLE
            else -> StreamErrorType.UNKNOWN
        }
    }

    fun playChannel(channel: Channel) {
        val player = getPlayer()
        retryAttemptCount = 0

        Log.i("AutoStreamTV_Player", "playChannel called for '${channel.name}' -> ${channel.streamUrl}")

        _playerState.value = _playerState.value.copy(
            currentChannel = channel,
            playbackStatus = PlaybackStatus.Connecting,
            isPlaying = false,
            qualityInfo = StreamQualityInfo()
        )

        try {
            val url = channel.streamUrl.trim()
            val urlLower = url.lowercase()
            val mimeType = when {
                urlLower.contains(".m3u8") || urlLower.contains("/hls") || urlLower.contains("playlist") || urlLower.contains("chunk") || urlLower.contains("master") -> MimeTypes.APPLICATION_M3U8
                urlLower.contains(".mpd") || urlLower.contains("/dash") -> MimeTypes.APPLICATION_MPD
                urlLower.contains(".mp4") -> MimeTypes.APPLICATION_MP4
                else -> MimeTypes.APPLICATION_M3U8
            }

            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMediaId(channel.id)
                .setMimeType(mimeType)
                .build()

            // Correctly clean up previous playback
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            player.play()
        } catch (e: Exception) {
            Log.e("AutoStreamTV_Player", "Failed to initialize playback for ${channel.name}", e)
            _playerState.value = _playerState.value.copy(
                playbackStatus = PlaybackStatus.Error(
                    errorType = StreamErrorType.UNKNOWN,
                    message = "Could not open stream: ${e.message}"
                )
            )
        }
    }

    fun retryCurrent() {
        val current = _playerState.value.currentChannel ?: return
        Log.d("AutoStreamTV_Player", "Manual retry requested for: ${current.name}")
        playChannel(current)
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun toggleFullscreen() {
        _playerState.value = _playerState.value.copy(
            isFullscreen = !_playerState.value.isFullscreen
        )
    }

    fun setFullscreen(fullscreen: Boolean) {
        _playerState.value = _playerState.value.copy(isFullscreen = fullscreen)
    }

    fun setControlsVisible(visible: Boolean) {
        _playerState.value = _playerState.value.copy(controlsVisible = visible)
    }

    fun closePlayer() {
        Log.d("AutoStreamTV_Player", "closePlayer called, stopping ExoPlayer")
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _playerState.value = ActivePlayerState(isFullscreen = false)
    }

    fun release() {
        isDisposed = true
        mainHandler.removeCallbacksAndMessages(null)
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }
}
