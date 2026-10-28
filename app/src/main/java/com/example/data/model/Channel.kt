package com.example.data.model

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val category: String = ChannelCategories.INTERNATIONAL,
    val country: String? = null,
    val language: String? = null,
    val groupTitle: String? = null,
    val isVerified: Boolean = false,
    val isFavorite: Boolean = false,
    val lastWatchedTime: Long? = null
)

object ChannelCategories {
    const val ALL = "All Channels"
    const val PAKISTAN = "Pakistan"
    const val INDIA = "India"
    const val SPORTS = "Cricket and Sports"
    const val NEWS = "News"
    const val DOCUMENTARIES = "Documentaries"
    const val WILDLIFE = "Wildlife and Animals"
    const val SCIENCE = "Science and Education"
    const val ENTERTAINMENT = "Entertainment"
    const val KIDS = "Kids and Family"
    const val INTERNATIONAL = "International TV"

    val ALL_CATEGORIES = listOf(
        ALL,
        PAKISTAN,
        INDIA,
        SPORTS,
        NEWS,
        DOCUMENTARIES,
        WILDLIFE,
        SCIENCE,
        ENTERTAINMENT,
        KIDS,
        INTERNATIONAL
    )
}

enum class StreamHealthStatus(val label: String) {
    ONLINE("Active Stream"),
    CONNECTING("Connecting"),
    BUFFERING("Buffering"),
    OFFLINE("Source Offline"),
    RESTRICTED("Geo-Restricted"),
    UNSUPPORTED("Unsupported Codec")
}
