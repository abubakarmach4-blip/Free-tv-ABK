package com.example

import com.example.data.local.InitialChannels
import com.example.data.model.ChannelCategories
import com.example.data.network.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ExampleUnitTest {

    @Test
    fun m3uParser_parsesValidExtinfAndStreamUrl() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="ptv.world" tvg-name="PTV World" tvg-logo="https://example.com/ptv.png" tvg-country="PK" tvg-language="urd" group-title="News",PTV World Official
            https://stream.example.com/ptv/live.m3u8
            #EXTINF:-1 tvg-id="dd.sports" tvg-name="DD Sports" tvg-country="IN" group-title="Cricket and Sports",DD Sports Cricket
            https://stream.example.com/cricket/stream.m3u8
        """.trimIndent()

        val parsed = M3uParser.parse(ByteArrayInputStream(sampleM3u.toByteArray(Charsets.UTF_8)))
        assertEquals(2, parsed.size)

        val ptv = parsed[0]
        assertEquals("PTV World Official", ptv.name)
        assertEquals("https://stream.example.com/ptv/live.m3u8", ptv.streamUrl)
        assertEquals("https://example.com/ptv.png", ptv.logoUrl)
        assertEquals("PK", ptv.country)
        assertEquals(ChannelCategories.PAKISTAN, ptv.category)

        val ddSports = parsed[1]
        assertEquals("DD Sports Cricket", ddSports.name)
        assertEquals(ChannelCategories.INDIA, ddSports.category)
    }

    @Test
    fun categoryClassification_identifiesAllTargetCategories() {
        assertEquals(
            ChannelCategories.SPORTS,
            M3uParser.categorize("Sky Cricket HD", "Sports", "UK", "eng")
        )
        assertEquals(
            ChannelCategories.PAKISTAN,
            M3uParser.categorize("Hum News TV", "News", "PK", "urd")
        )
        assertEquals(
            ChannelCategories.INDIA,
            M3uParser.categorize("DD National", "General", "IN", "hin")
        )
        assertEquals(
            ChannelCategories.NEWS,
            M3uParser.categorize("DW News English", "News", "DE", "eng")
        )
        assertEquals(
            ChannelCategories.DOCUMENTARIES,
            M3uParser.categorize("Timeline History Doc", "Documentary", "US", "eng")
        )
        assertEquals(
            ChannelCategories.WILDLIFE,
            M3uParser.categorize("Explore Wild Animals Live", "Wildlife", "US", "eng")
        )
        assertEquals(
            ChannelCategories.SCIENCE,
            M3uParser.categorize("NASA Space Stream", "Science", "US", "eng")
        )
        assertEquals(
            ChannelCategories.ENTERTAINMENT,
            M3uParser.categorize("Rakuten Action Movies", "Movies", "ES", "eng")
        )
        assertEquals(
            ChannelCategories.KIDS,
            M3uParser.categorize("Duck TV Cartoon Kids", "Kids", "US", "eng")
        )
        assertEquals(
            ChannelCategories.INTERNATIONAL,
            M3uParser.categorize("Arirang World", "General", "KR", "eng")
        )
    }

    @Test
    fun initialChannels_containsAllTenCategories() {
        val seed = InitialChannels.getSeedChannels()
        assertTrue(seed.size >= 10)
        
        val categories = seed.map { it.category }.toSet()
        assertTrue(categories.contains(ChannelCategories.PAKISTAN))
        assertTrue(categories.contains(ChannelCategories.INDIA))
        assertTrue(categories.contains(ChannelCategories.SPORTS))
        assertTrue(categories.contains(ChannelCategories.NEWS))
        assertTrue(categories.contains(ChannelCategories.DOCUMENTARIES))
        assertTrue(categories.contains(ChannelCategories.WILDLIFE))
        assertTrue(categories.contains(ChannelCategories.SCIENCE))
        assertTrue(categories.contains(ChannelCategories.ENTERTAINMENT))
        assertTrue(categories.contains(ChannelCategories.KIDS))
        assertTrue(categories.contains(ChannelCategories.INTERNATIONAL))
    }
}
