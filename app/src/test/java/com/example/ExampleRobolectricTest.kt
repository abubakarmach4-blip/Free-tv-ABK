package com.example

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Channel
import com.example.player.ExoPlayerHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AutoStream TV", appName)
    }

    @OptIn(UnstableApi::class)
    @Test
    fun `exoPlayerHelper initializes and prepares verified sample HLS stream`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var watchedChannelId: String? = null
        val helper = ExoPlayerHelper(context) { id ->
            watchedChannelId = id
        }

        val testChannel = Channel(
            id = "test_mux_hls",
            name = "Verified HLS Reference Stream",
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            category = "International TV"
        )

        helper.playChannel(testChannel)

        val player = helper.getPlayer()
        assertNotNull(player)
        assertEquals(1, player.mediaItemCount)

        val currentMediaItem = player.getMediaItemAt(0)
        assertEquals("test_mux_hls", currentMediaItem.mediaId)
        assertEquals(MimeTypes.APPLICATION_M3U8, currentMediaItem.localConfiguration?.mimeType)

        helper.release()
    }
}
