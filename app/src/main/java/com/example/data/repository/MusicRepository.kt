package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.R
import com.example.data.local.MusicDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistTrackCrossRef
import com.example.data.local.SavedTrackEntity
import com.example.model.CloudSyncData
import com.example.model.Playlist
import com.example.model.PodcastEpisode
import com.example.model.PodcastShow
import com.example.model.Track
import com.example.model.TrackSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao
) {

    // Default built-in curated online streams & sample tracks with working audio URLs
    val defaultFeaturedTracks: List<Track> = listOf(
        Track(
            id = "stream_lofi_1",
            title = "Midnight Coffee Lofi",
            artist = "Aesthetic Beats",
            album = "Chill Lounge Vol. 1",
            durationMs = 184000L,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            coverDrawableRes = R.drawable.cover_lofi_1787235216505,
            source = TrackSource.STREAM,
            genre = "Lo-Fi Beats",
            lyrics = "Late night rain tapping on the window pane\nWarm mug in hand, fading away the pain\nSmooth vinyl spinning slow in rhythm\nLost in the quiet of this chill season..."
        ),
        Track(
            id = "stream_synth_2",
            title = "Cyber Neon Nights",
            artist = "Retrowave 2088",
            album = "Grid Runner OST",
            durationMs = 212000L,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            coverDrawableRes = R.drawable.cover_cyberpunk_1787235201442,
            source = TrackSource.STREAM,
            genre = "Synthwave",
            lyrics = "Cruising down the highway in the purple neon light\nDigital horizon shining bright tonight\nBassline pulsing through the circuit board\nNever looking back, future is restored!"
        ),
        Track(
            id = "stream_chill_3",
            title = "Sunset Acoustic Groove",
            artist = "Solaris Duo",
            album = "Golden Hour Sessions",
            durationMs = 196000L,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            coverDrawableRes = R.drawable.cover_chillhop_1787235245557,
            source = TrackSource.STREAM,
            genre = "Acoustic Chill",
            lyrics = "Breeze through the palm trees, golden sunset glow\nStr платы of melody moving soft and slow\nEvery single worry left behind the shore\nJust good vibes and rhythm, nothing more..."
        ),
        Track(
            id = "stream_edm_4",
            title = "Pulse Electro Horizon",
            artist = "Quantum Bass",
            album = "Electric Dreams",
            durationMs = 245000L,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            coverDrawableRes = R.drawable.cover_cyberpunk_1787235201442,
            source = TrackSource.STREAM,
            genre = "Electronic",
            lyrics = "Drop the beat, feel the bass ignite\nEnergy electrifying throughout the night\nFeel the subwoofers rumble inside your chest\nSpotify Music player performing at its best!"
        ),
        Track(
            id = "stream_radio_5",
            title = "Chillout Global Web Radio",
            artist = "Live Stream Station",
            album = "24/7 Internet Radio",
            durationMs = 0L, // live stream
            streamUrl = "https://stream.zeno.fm/f3wvbbqmdg8uv",
            coverDrawableRes = R.drawable.cover_lofi_1787235216505,
            source = TrackSource.STREAM,
            genre = "Live Stream Radio",
            lyrics = "[Live Internet Radio Broadcast]\nDirect continuous audio stream from high-bandwidth web radio server."
        )
    )

    // Curated Podcasts
    val podcastShows: List<PodcastShow> = listOf(
        PodcastShow(
            id = "show_tech_pulse",
            title = "Tech Wave & AI Frontier",
            host = "Alex Morgan & Elena Rostova",
            description = "Deep dives into cutting edge tech, Android development, AI agents, mobile architecture and modern software craftsmanship.",
            coverRes = R.drawable.cover_podcast_1787235232773,
            category = "Technology",
            episodes = listOf(
                PodcastEpisode(
                    id = "ep_tech_101",
                    showId = "show_tech_pulse",
                    showTitle = "Tech Wave & AI Frontier",
                    title = "EP 101: Modern Android Architecture & Jetpack Compose",
                    description = "In this episode, we discuss modern reactive UI in Compose, Room local databases, Audio streaming services and building delightful Material 3 audio applications.",
                    durationMs = 1800000L, // 30 mins
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                    publishedDate = "Aug 18, 2026",
                    coverRes = R.drawable.cover_podcast_1787235232773
                ),
                PodcastEpisode(
                    id = "ep_tech_102",
                    showId = "show_tech_pulse",
                    showTitle = "Tech Wave & AI Frontier",
                    title = "EP 102: The Future of Cloud Sync and Spatial Audio",
                    description = "How audio equalizers, 3D virtualizers and multi-device cloud synchronization are changing user experience on modern mobile devices.",
                    durationMs = 2100000L,
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                    publishedDate = "Aug 12, 2026",
                    coverRes = R.drawable.cover_podcast_1787235232773
                )
            )
        ),
        PodcastShow(
            id = "show_music_mastery",
            title = "Sound Design & Equalizer Masters",
            host = "Marcus Vane",
            description = "Exploring acoustic frequency bands, bass boost acoustics, mastering curves, and how equalizers transform audio perception.",
            coverRes = R.drawable.cover_chillhop_1787235245557,
            category = "Music Production",
            episodes = listOf(
                PodcastEpisode(
                    id = "ep_music_201",
                    showId = "show_music_mastery",
                    showTitle = "Sound Design & Equalizer Masters",
                    title = "EP 01: Mastering the 5-Band Graphic Equalizer",
                    description = "A practical guide to shaping 60Hz sub-bass, 230Hz punch, 910Hz vocal warmth, and sparkling 14kHz air.",
                    durationMs = 1500000L,
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                    publishedDate = "Aug 15, 2026",
                    coverRes = R.drawable.cover_chillhop_1787235245557
                )
            )
        )
    )

    // Flow of playlists from Room
    val playlistsFlow: Flow<List<Playlist>> = musicDao.getAllPlaylists().map { entities ->
        entities.map { entity ->
            Playlist(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                coverRes = entity.coverRes ?: R.drawable.cover_cyberpunk_1787235201442,
                coverUrl = entity.coverUrl,
                isCloudSynced = entity.isCloudSynced,
                createdAt = entity.createdAt
            )
        }
    }

    // Flow of favorite tracks from Room
    val favoriteTracksFlow: Flow<List<Track>> = musicDao.getFavoriteTracks().map { entities ->
        entities.map { entity ->
            Track(
                id = entity.id,
                title = entity.title,
                artist = entity.artist,
                album = entity.album,
                durationMs = entity.durationMs,
                streamUrl = entity.streamUrl,
                localUri = entity.localUri,
                coverDrawableRes = entity.coverDrawableRes ?: R.drawable.cover_cyberpunk_1787235201442,
                coverUrl = entity.coverUrl,
                source = when (entity.source) {
                    "LOCAL" -> TrackSource.LOCAL
                    "PODCAST" -> TrackSource.PODCAST
                    else -> TrackSource.STREAM
                },
                isFavorite = true,
                genre = entity.genre
            )
        }
    }

    // Query Device MP3 Files from MediaStore
    suspend fun scanDeviceMp3Files(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val queryUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            context.contentResolver.query(
                queryUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Local Music"
                    val duration = cursor.getLong(durationColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

                    tracks.add(
                        Track(
                            id = "local_$id",
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            localUri = contentUri,
                            coverDrawableRes = R.drawable.cover_chillhop_1787235245557,
                            source = TrackSource.LOCAL,
                            genre = "Device Audio"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If device has no MP3 files loaded or emulator without local storage media, provide sample local tracks for instant testing
        if (tracks.isEmpty()) {
            tracks.add(
                Track(
                    id = "local_demo_1",
                    title = "Device Audio - Acoustic Sunset.mp3",
                    artist = "Phone Storage / Music",
                    album = "Internal SD Card",
                    durationMs = 210000L,
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    coverDrawableRes = R.drawable.cover_chillhop_1787235245557,
                    source = TrackSource.LOCAL,
                    genre = "Local MP3 File"
                )
            )
            tracks.add(
                Track(
                    id = "local_demo_2",
                    title = "Device Audio - Night Drive.mp3",
                    artist = "Phone Storage / Downloads",
                    album = "Internal SD Card",
                    durationMs = 195000L,
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                    coverDrawableRes = R.drawable.cover_cyberpunk_1787235201442,
                    source = TrackSource.LOCAL,
                    genre = "Local MP3 File"
                )
            )
        }

        tracks
    }

    suspend fun createPlaylist(title: String, description: String = ""): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val playlist = PlaylistEntity(
            id = id,
            title = title,
            description = description.ifEmpty { "Created with Spotify Music" },
            coverRes = R.drawable.cover_cyberpunk_1787235201442,
            isCloudSynced = true,
            createdAt = System.currentTimeMillis()
        )
        musicDao.insertPlaylist(playlist)
        id
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef(playlistId, trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        musicDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTrackIdsForPlaylist(playlistId: String): Flow<List<String>> {
        return musicDao.getTrackIdsForPlaylist(playlistId)
    }

    suspend fun toggleFavorite(track: Track): Boolean = withContext(Dispatchers.IO) {
        val newFav = !track.isFavorite
        val entity = SavedTrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            streamUrl = track.streamUrl,
            localUri = track.localUri,
            coverDrawableRes = track.coverDrawableRes,
            coverUrl = track.coverUrl,
            source = track.source.name,
            isFavorite = newFav,
            genre = track.genre,
            addedAt = System.currentTimeMillis()
        )
        musicDao.saveTrack(entity)
        newFav
    }

    suspend fun initializeDefaultPlaylistsIfEmpty() = withContext(Dispatchers.IO) {
        // Pre-populate with default Spotify playlists for instant first-run experience
        val defaultPlaylists = listOf(
            PlaylistEntity(
                id = "pl_top_hits",
                title = "Today's Top Hits 2026",
                description = "The hottest streams, synthwave & electronic bangers right now.",
                coverRes = R.drawable.cover_cyberpunk_1787235201442,
                isCloudSynced = true
            ),
            PlaylistEntity(
                id = "pl_chill_lofi",
                title = "Lo-Fi Beats & Relax",
                description = "Cozy vibes, coffee shop acoustics, and relaxing ambient streams.",
                coverRes = R.drawable.cover_lofi_1787235216505,
                isCloudSynced = true
            ),
            PlaylistEntity(
                id = "pl_deep_focus",
                title = "Deep Focus & Study",
                description = "Acoustic instruments and calm beats for coding and concentration.",
                coverRes = R.drawable.cover_chillhop_1787235245557,
                isCloudSynced = true
            )
        )

        for (pl in defaultPlaylists) {
            musicDao.insertPlaylist(pl)
        }

        // Add some default tracks to playlists
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_top_hits", "stream_synth_2", 0))
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_top_hits", "stream_edm_4", 1))
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_chill_lofi", "stream_lofi_1", 0))
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_chill_lofi", "stream_chill_3", 1))
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_deep_focus", "stream_chill_3", 0))
    }
}
