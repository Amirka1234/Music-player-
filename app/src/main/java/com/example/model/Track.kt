package com.example.model

enum class TrackSource {
    LOCAL,
    STREAM,
    PODCAST
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val streamUrl: String = "",
    val localUri: String = "",
    val coverDrawableRes: Int? = null,
    val coverUrl: String = "",
    val source: TrackSource = TrackSource.STREAM,
    val isFavorite: Boolean = false,
    val genre: String = "Various",
    val lyrics: String = ""
)
