package com.example.model

data class PodcastShow(
    val id: String,
    val title: String,
    val host: String,
    val description: String,
    val coverRes: Int? = null,
    val coverUrl: String = "",
    val category: String,
    val episodes: List<PodcastEpisode> = emptyList()
)

data class PodcastEpisode(
    val id: String,
    val showId: String,
    val showTitle: String,
    val title: String,
    val description: String,
    val durationMs: Long,
    val audioUrl: String,
    val publishedDate: String,
    val coverRes: Int? = null,
    val progressMs: Long = 0L,
    val isCompleted: Boolean = false
) {
    fun toTrack(): Track {
        return Track(
            id = id,
            title = title,
            artist = showTitle,
            album = "Podcast Episode",
            durationMs = durationMs,
            streamUrl = audioUrl,
            coverDrawableRes = coverRes,
            source = TrackSource.PODCAST,
            genre = "Podcasts",
            lyrics = "Episode Notes:\n$description"
        )
    }
}
