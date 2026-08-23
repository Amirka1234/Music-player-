package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.R
import com.example.data.local.CustomPodcastEntity
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao
) {

    // Default built-in curated online music streams & radio
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

    // User-added custom podcasts from Room database (no hardcoded pre-packaged podcasts)
    val customPodcastsFlow: Flow<List<PodcastShow>> = musicDao.getAllCustomPodcasts().map { entities ->
        entities.map { entity ->
            val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(entity.addedAt))
            PodcastShow(
                id = entity.id,
                title = entity.title,
                host = entity.host.ifBlank { "Пользовательский поток" },
                description = entity.description.ifBlank { "Аудиоподкаст добавлен по прямой ссылке" },
                coverRes = entity.coverRes ?: R.drawable.cover_podcast_1787235232773,
                category = entity.category.ifBlank { "Подкаст" },
                episodes = listOf(
                    PodcastEpisode(
                        id = "ep_${entity.id}",
                        showId = entity.id,
                        showTitle = entity.title,
                        title = entity.title,
                        description = entity.description.ifBlank { "Прямой поток: ${entity.url}" },
                        durationMs = 0L,
                        audioUrl = entity.url,
                        publishedDate = dateStr,
                        coverRes = entity.coverRes ?: R.drawable.cover_podcast_1787235232773
                    )
                )
            )
        }
    }

    // Reactive flow of playlists from Room combined with playlist track cross-references
    val playlistsFlow: Flow<List<Playlist>> = combine(
        musicDao.getAllPlaylists(),
        musicDao.getAllPlaylistTrackRefs()
    ) { playlists, refs ->
        val refsByPlaylist = refs.groupBy { it.playlistId }
        playlists.map { entity ->
            val trackIds = refsByPlaylist[entity.id]?.map { it.trackId } ?: emptyList()
            Playlist(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                coverRes = entity.coverRes ?: R.drawable.cover_cyberpunk_1787235201442,
                coverUrl = entity.coverUrl,
                trackIds = trackIds,
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
                genre = entity.genre,
                lyrics = entity.lyrics
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
                MediaStore.Audio.Media.ALBUM_ID,
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
                val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Без названия"
                    val artist = cursor.getString(artistColumn) ?: "Неизвестный исполнитель"
                    val album = cursor.getString(albumColumn) ?: "Аудиофайл"
                    val duration = cursor.getLong(durationColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

                    val albumId = if (albumIdColumn != -1) cursor.getLong(albumIdColumn) else -1L
                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId).toString()
                    } else {
                        contentUri
                    }

                    tracks.add(
                        Track(
                            id = "local_$id",
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            localUri = contentUri,
                            coverUrl = albumArtUri,
                            coverDrawableRes = R.drawable.cover_chillhop_1787235245557,
                            source = TrackSource.LOCAL,
                            genre = "Локальный файл"
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
                    title = "Акустический закат.mp3",
                    artist = "Память устройства / Music",
                    album = "Внутренняя память",
                    durationMs = 210000L,
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    coverDrawableRes = R.drawable.cover_chillhop_1787235245557,
                    source = TrackSource.LOCAL,
                    genre = "Локальный MP3"
                )
            )
            tracks.add(
                Track(
                    id = "local_demo_2",
                    title = "Ночной город.mp3",
                    artist = "Память устройства / Downloads",
                    album = "Внутренняя память",
                    durationMs = 195000L,
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                    coverDrawableRes = R.drawable.cover_cyberpunk_1787235201442,
                    source = TrackSource.LOCAL,
                    genre = "Локальный MP3"
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
            description = description.ifEmpty { "Создано в Spotify Music" },
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

    suspend fun addTrackToPlaylist(playlistId: String, track: Track) = withContext(Dispatchers.IO) {
        saveOrUpdateTrack(track)
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef(playlistId, track.id))
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        musicDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun deleteTrack(trackId: String) = withContext(Dispatchers.IO) {
        musicDao.deleteTrack(trackId)
        musicDao.removeTrackFromAllPlaylists(trackId)
    }

    suspend fun addCustomPodcast(title: String, host: String, url: String, category: String = "Подкаст", description: String = ""): String = withContext(Dispatchers.IO) {
        val id = "podcast_${UUID.randomUUID()}"
        val entity = CustomPodcastEntity(
            id = id,
            title = title,
            host = host.ifBlank { "Ведущий подкаста" },
            url = url,
            description = description.ifBlank { "Аудиопоток: $url" },
            coverRes = R.drawable.cover_podcast_1787235232773,
            category = category.ifBlank { "Подкаст" },
            addedAt = System.currentTimeMillis()
        )
        musicDao.insertCustomPodcast(entity)
        id
    }

    suspend fun deleteCustomPodcast(podcastId: String) = withContext(Dispatchers.IO) {
        musicDao.deleteCustomPodcast(podcastId)
    }

    suspend fun saveOrUpdateTrack(track: Track) = withContext(Dispatchers.IO) {
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
            isFavorite = track.isFavorite,
            genre = track.genre,
            lyrics = track.lyrics,
            addedAt = System.currentTimeMillis()
        )
        musicDao.saveTrack(entity)
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
            lyrics = track.lyrics,
            addedAt = System.currentTimeMillis()
        )
        musicDao.saveTrack(entity)
        newFav
    }

    suspend fun initializeDefaultPlaylistsIfEmpty() = withContext(Dispatchers.IO) {
        // Pre-populate with default clean music playlists
        for (tr in defaultFeaturedTracks) {
            saveOrUpdateTrack(tr)
        }

        val defaultPlaylists = listOf(
            PlaylistEntity(
                id = "pl_top_hits",
                title = "Today's Top Hits 2026",
                description = "Лучшие электронные и синтвейв треки прямо сейчас.",
                coverRes = R.drawable.cover_cyberpunk_1787235201442,
                isCloudSynced = true
            ),
            PlaylistEntity(
                id = "pl_chill_lofi",
                title = "Lo-Fi Beats & Relax",
                description = "Уютные биты для отдыха, кофе и атмосферных вечеров.",
                coverRes = R.drawable.cover_lofi_1787235216505,
                isCloudSynced = true
            ),
            PlaylistEntity(
                id = "pl_deep_focus",
                title = "Deep Focus & Study",
                description = "Акустические мелодии для концентрации и продуктивной работы.",
                coverRes = R.drawable.cover_chillhop_1787235245557,
                isCloudSynced = true
            )
        )

        for (pl in defaultPlaylists) {
            musicDao.insertPlaylist(pl)
        }

        // Add music tracks to playlists (pure music streams only)
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_top_hits", "stream_synth_2", 0))
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_top_hits", "stream_edm_4", 1))
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_chill_lofi", "stream_lofi_1", 0))
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_chill_lofi", "stream_chill_3", 1))
        musicDao.insertPlaylistTrack(PlaylistTrackCrossRef("pl_deep_focus", "stream_chill_3", 0))
    }
}

