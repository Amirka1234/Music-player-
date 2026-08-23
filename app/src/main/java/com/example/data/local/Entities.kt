package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverRes: Int? = null,
    val coverUrl: String = "",
    val isCloudSynced: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val addedOrder: Int = 0
)

@Entity(tableName = "saved_tracks")
data class SavedTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val streamUrl: String,
    val localUri: String,
    val coverDrawableRes: Int? = null,
    val coverUrl: String = "",
    val source: String = "STREAM",
    val isFavorite: Boolean = false,
    val genre: String = "Various",
    val lyrics: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "podcast_progress")
data class PodcastProgressEntity(
    @PrimaryKey val episodeId: String,
    val showId: String,
    val progressMs: Long,
    val isCompleted: Boolean,
    val lastListenedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_podcasts")
data class CustomPodcastEntity(
    @PrimaryKey val id: String,
    val title: String,
    val host: String,
    val url: String,
    val description: String = "",
    val coverRes: Int? = null,
    val category: String = "Подкаст",
    val addedAt: Long = System.currentTimeMillis()
)

