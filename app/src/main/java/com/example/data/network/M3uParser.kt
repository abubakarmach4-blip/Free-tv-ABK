package com.example.data.network

import com.example.data.local.ChannelEntity
import com.example.data.model.ChannelCategories
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.Locale

object M3uParser {

    private val ATTRIBUTE_REGEX = Regex("""([a-zA-Z0-9_-]+)="([^"]*)"""")

    fun parse(inputStream: InputStream, defaultCategory: String? = null): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        var currentAttributes: Map<String, String>? = null
        var currentTitle: String? = null

        reader.useLines { lines ->
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty()) continue

                if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                    val commaIndex = line.lastIndexOf(',')
                    val headerPart = if (commaIndex != -1) line.substring(0, commaIndex) else line
                    currentTitle = if (commaIndex != -1 && commaIndex < line.length - 1) {
                        line.substring(commaIndex + 1).trim()
                    } else {
                        null
                    }

                    val attributes = mutableMapOf<String, String>()
                    ATTRIBUTE_REGEX.findAll(headerPart).forEach { matchResult ->
                        val key = matchResult.groupValues[1].lowercase(Locale.ROOT)
                        val value = matchResult.groupValues[2].trim()
                        attributes[key] = value
                    }
                    currentAttributes = attributes
                } else if (!line.startsWith("#")) {
                    // Line is potential stream URL
                    val streamUrl = line
                    if (isValidStreamUrl(streamUrl)) {
                        val attrs = currentAttributes ?: emptyMap()
                        val tvgName = attrs["tvg-name"]
                        val name = (currentTitle ?: tvgName ?: "Live Channel").trim()
                        val logo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                        val country = attrs["tvg-country"]?.uppercase(Locale.ROOT)
                        val language = attrs["tvg-language"]?.lowercase(Locale.ROOT)
                        val groupTitle = attrs["group-title"]

                        val category = defaultCategory ?: categorize(
                            name = name,
                            group = groupTitle,
                            country = country,
                            language = language
                        )

                        val id = generateChannelId(streamUrl, name)

                        channels.add(
                            ChannelEntity(
                                id = id,
                                name = name,
                                streamUrl = streamUrl,
                                logoUrl = logo,
                                category = category,
                                country = country,
                                language = language,
                                groupTitle = groupTitle,
                                isVerified = false,
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                    }
                    currentAttributes = null
                    currentTitle = null
                }
            }
        }
        return channels
    }

    fun categorize(
        name: String,
        group: String?,
        country: String?,
        language: String?
    ): String {
        val n = name.lowercase(Locale.ROOT)
        val g = group?.lowercase(Locale.ROOT) ?: ""
        val c = country?.uppercase(Locale.ROOT) ?: ""
        val l = language?.lowercase(Locale.ROOT) ?: ""

        // 1. Pakistan
        if (c == "PK" || c == "PAK" || l == "urd" || l == "pan" ||
            n.contains("pakistan") || g.contains("pakistan") || n.contains("ptv") ||
            n.contains("hum news") || n.contains("geo ") || n.contains("ary ") ||
            n.contains("express news") || n.contains("dunya news")
        ) {
            return ChannelCategories.PAKISTAN
        }

        // 2. India
        if (c == "IN" || c == "IND" || l == "hin" || l == "tam" || l == "tel" ||
            n.contains("india") || g.contains("india") || n.startsWith("dd ") ||
            n.contains("aaj tak") || n.contains("ndtv") || n.contains("sansad") ||
            n.contains("zee news") || n.contains("republic bharat")
        ) {
            return ChannelCategories.INDIA
        }

        // 3. Cricket and Sports
        if (g.contains("sport") || n.contains("sport") || n.contains("cricket") ||
            n.contains("football") || n.contains("racing") || n.contains("tennis") ||
            g.contains("cricket") || n.contains("red bull tv") || n.contains("fight") ||
            n.contains("unbeaten") || n.contains("motor") || g.contains("motor")
        ) {
            return ChannelCategories.SPORTS
        }

        // 4. Wildlife and Animals
        if (g.contains("wild") || g.contains("animal") || g.contains("nature") ||
            n.contains("wild") || n.contains("animal") || n.contains("safari") ||
            n.contains("earthcam") || n.contains("explore.org") || n.contains("zoo") ||
            n.contains("nature")
        ) {
            return ChannelCategories.WILDLIFE
        }

        // 5. Science and Education
        if (g.contains("science") || g.contains("education") || g.contains("tech") ||
            n.contains("science") || n.contains("education") || n.contains("nasa") ||
            n.contains("ted") || n.contains("space") || n.contains("learn") ||
            g.contains("documentary and education")
        ) {
            return ChannelCategories.SCIENCE
        }

        // 6. Documentaries
        if (g.contains("doc") || n.contains("doc") || n.contains("timeline") ||
            n.contains("history") || g.contains("history") || n.contains("investigation")
        ) {
            return ChannelCategories.DOCUMENTARIES
        }

        // 7. Kids and Family
        if (g.contains("kid") || g.contains("child") || g.contains("family") ||
            g.contains("animation") || g.contains("cartoon") || n.contains("kid") ||
            n.contains("cartoon") || n.contains("toon") || n.contains("duck tv") ||
            n.contains("pbs kids")
        ) {
            return ChannelCategories.KIDS
        }

        // 8. News
        if (g.contains("news") || n.contains("news") || g.contains("information") ||
            n.contains("dw ") || n.contains("france 24") || n.contains("al jazeera") ||
            n.contains("euronews") || n.contains("sky news") || n.contains("bbc") ||
            n.contains("cnn") || n.contains("bloomberg")
        ) {
            return ChannelCategories.NEWS
        }

        // 9. International TV
        if (g.contains("international") || g.contains("world") || g.contains("global") ||
            n.contains("international") || n.contains("world") || n.contains("global") ||
            n.contains("arirang") || n.contains("tv5monde") || n.contains("trt world")
        ) {
            return ChannelCategories.INTERNATIONAL
        }

        // 10. Entertainment
        if (g.contains("entertainment") || g.contains("movie") || g.contains("series") ||
            g.contains("drama") || g.contains("comedy") || g.contains("music") ||
            n.contains("rakuten") || n.contains("film") || n.contains("cinema") ||
            n.contains("retro") || n.contains("comedy") || n.contains("drama")
        ) {
            return ChannelCategories.ENTERTAINMENT
        }

        // Default: International TV
        return ChannelCategories.INTERNATIONAL
    }

    private fun isValidStreamUrl(url: String): Boolean {
        val u = url.trim().lowercase(Locale.ROOT)
        return (u.startsWith("http://") || u.startsWith("https://")) &&
                (u.contains(".m3u8") || u.contains(".mpd") || u.contains("/hls/") ||
                        u.contains("/live") || u.contains("playlist") || u.contains("manifest") ||
                        u.contains(".mp4") || u.contains(".ts"))
    }

    private fun generateChannelId(url: String, name: String): String {
        val input = "$url|$name"
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            "ch_" + (input.hashCode().toLong() and 0xFFFFFFFFL).toString(16)
        }
    }
}
