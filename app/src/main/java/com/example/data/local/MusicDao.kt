package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("SELECT trackId FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY addedOrder ASC")
    fun getTrackIdsForPlaylist(playlistId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(ref: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)

    @Query("SELECT * FROM saved_tracks WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteTracks(): Flow<List<SavedTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrack(track: SavedTrackEntity)

    @Query("UPDATE saved_tracks SET isFavorite = :isFav WHERE id = :trackId")
    suspend fun updateFavorite(trackId: String, isFav: Boolean)

    @Query("SELECT * FROM podcast_progress WHERE episodeId = :episodeId")
    suspend fun getPodcastProgress(episodeId: String): PodcastProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePodcastProgress(progress: PodcastProgressEntity)

    @Query("DELETE FROM saved_tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("DELETE FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun removeTrackFromAllPlaylists(trackId: String)

    @Query("SELECT * FROM playlist_tracks")
    fun getAllPlaylistTrackRefs(): Flow<List<PlaylistTrackCrossRef>>

    @Query("SELECT * FROM saved_tracks")
    fun getAllSavedTracks(): Flow<List<SavedTrackEntity>>

    @Query("SELECT * FROM custom_podcasts ORDER BY addedAt DESC")
    fun getAllCustomPodcasts(): Flow<List<CustomPodcastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPodcast(podcast: CustomPodcastEntity)

    @Query("DELETE FROM custom_podcasts WHERE id = :podcastId")
    suspend fun deleteCustomPodcast(podcastId: String)
}
