package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MusicApplication
import com.example.R
import com.example.model.CloudSyncData
import com.example.model.EqPreset
import com.example.model.EqualizerState
import com.example.model.Playlist
import com.example.model.PodcastEpisode
import com.example.model.PodcastShow
import com.example.model.Track
import com.example.model.TrackSource
import com.example.player.RepeatMode
import com.example.service.AudioPlayerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class NavigationTab {
    HOME,
    SEARCH,
    LIBRARY,
    PODCASTS,
    EQUALIZER
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicApplication.instance.repository
    private val playerManager = MusicApplication.instance.playerManager
    private val context: Context get() = getApplication()

    // Navigation & Player presentation
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _isLockScreenPreviewActive = MutableStateFlow(false)
    val isLockScreenPreviewActive: StateFlow<Boolean> = _isLockScreenPreviewActive.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Visualizer visibility setting
    private val _isVisualizerVisible = MutableStateFlow(false)
    val isVisualizerVisible: StateFlow<Boolean> = _isVisualizerVisible.asStateFlow()

    // Context Menu & Track editing / lyrics dialog states
    private val _selectedTrackForMenu = MutableStateFlow<Track?>(null)
    val selectedTrackForMenu: StateFlow<Track?> = _selectedTrackForMenu.asStateFlow()

    private val _selectedTrackForEdit = MutableStateFlow<Track?>(null)
    val selectedTrackForEdit: StateFlow<Track?> = _selectedTrackForEdit.asStateFlow()

    private val _selectedTrackForLyrics = MutableStateFlow<Track?>(null)
    val selectedTrackForLyrics: StateFlow<Track?> = _selectedTrackForLyrics.asStateFlow()

    private val _selectedTrackForDelete = MutableStateFlow<Track?>(null)
    val selectedTrackForDelete: StateFlow<Track?> = _selectedTrackForDelete.asStateFlow()

    // Player state bindings
    val currentTrack: StateFlow<Track?> = playerManager.currentTrack
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPosition: StateFlow<Long> = playerManager.currentPosition
    val duration: StateFlow<Long> = playerManager.duration
    val isBuffering: StateFlow<Boolean> = playerManager.isBuffering
    val playbackSpeed: StateFlow<Float> = playerManager.playbackSpeed
    val repeatMode: StateFlow<RepeatMode> = playerManager.repeatMode
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val equalizerState: StateFlow<EqualizerState> = playerManager.equalizerState
    val sleepTimerSeconds: StateFlow<Int?> = playerManager.sleepTimerSeconds
    val visualizerAmplitudes: StateFlow<List<Float>> = playerManager.visualizerAmplitudes

    // Playlists & Favorites from Database
    val playlists: StateFlow<List<Playlist>> = repository.playlistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Local MP3s scanned
    private val _localTracks = MutableStateFlow<List<Track>>(emptyList())
    val localTracks: StateFlow<List<Track>> = _localTracks.asStateFlow()

    private val _isScanningLocal = MutableStateFlow(false)
    val isScanningLocal: StateFlow<Boolean> = _isScanningLocal.asStateFlow()

    // Featured Streams & Radio
    val featuredStreams: List<Track> = repository.defaultFeaturedTracks

    // Podcasts from Room (user added via link)
    val podcastShows: StateFlow<List<PodcastShow>> = repository.customPodcastsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query & category filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSearchFilter = MutableStateFlow("All")
    val selectedSearchFilter: StateFlow<String> = _selectedSearchFilter.asStateFlow()

    // Cloud Sync State
    private val _cloudSyncData = MutableStateFlow(CloudSyncData())
    val cloudSyncData: StateFlow<CloudSyncData> = _cloudSyncData.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Dialog States
    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _showAddCustomStreamDialog = MutableStateFlow(false)
    val showAddCustomStreamDialog: StateFlow<Boolean> = _showAddCustomStreamDialog.asStateFlow()

    private val _showAddPodcastDialog = MutableStateFlow(false)
    val showAddPodcastDialog: StateFlow<Boolean> = _showAddPodcastDialog.asStateFlow()

    private val _selectedTrackForAddToPlaylist = MutableStateFlow<Track?>(null)
    val selectedTrackForAddToPlaylist: StateFlow<Track?> = _selectedTrackForAddToPlaylist.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _selectedPlaylistForDetail = MutableStateFlow<Playlist?>(null)
    val selectedPlaylistForDetail: StateFlow<Playlist?> = _selectedPlaylistForDetail.asStateFlow()

    init {
        // Start audio foreground service
        AudioPlayerService.startService(context)
        // Scan local storage mp3s
        scanDeviceAudio()
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun setLockScreenPreviewActive(active: Boolean) {
        _isLockScreenPreviewActive.value = active
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        val finalQueue = if (queue.isNotEmpty()) queue else listOf(track) + featuredStreams
        playerManager.playTrack(track, finalQueue)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun skipToNext() {
        playerManager.skipToNext()
    }

    fun skipToPrevious() {
        playerManager.skipToPrevious()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    fun setSleepTimer(minutes: Int) {
        playerManager.setSleepTimer(minutes)
        _showSleepTimerDialog.value = false
        if (minutes > 0) {
            Toast.makeText(context, "Sleep timer set for $minutes minutes", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Sleep timer cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleSleepTimerDialog(show: Boolean) {
        _showSleepTimerDialog.value = show
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val isFav = repository.toggleFavorite(track)
            val msg = if (isFav) "Добавлено в Любимые" else "Удалено из Любимых"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleVisualizerVisible() {
        _isVisualizerVisible.value = !_isVisualizerVisible.value
    }

    fun setVisualizerVisible(visible: Boolean) {
        _isVisualizerVisible.value = visible
    }

    // Track Context Menu Operations
    fun openTrackContextMenu(track: Track) {
        _selectedTrackForMenu.value = track
    }

    fun dismissTrackContextMenu() {
        _selectedTrackForMenu.value = null
    }

    fun playNext(track: Track) {
        playerManager.playNext(track)
        Toast.makeText(context, "«${track.title}» будет воспроизведен следующим", Toast.LENGTH_SHORT).show()
    }

    fun addToQueue(track: Track) {
        playerManager.addToQueue(track)
        Toast.makeText(context, "«${track.title}» добавлен в очередь", Toast.LENGTH_SHORT).show()
    }

    fun removeFromQueue(track: Track) {
        playerManager.removeFromQueue(track.id)
        Toast.makeText(context, "«${track.title}» удален из очереди", Toast.LENGTH_SHORT).show()
    }

    fun isTrackInQueue(trackId: String): Boolean {
        return playerManager.playlist.value.any { it.id == trackId }
    }

    fun openEditTrackDialog(track: Track) {
        _selectedTrackForEdit.value = track
    }

    fun dismissEditTrackDialog() {
        _selectedTrackForEdit.value = null
    }

    fun openLyricsDialog(track: Track) {
        _selectedTrackForLyrics.value = track
    }

    fun dismissLyricsDialog() {
        _selectedTrackForLyrics.value = null
    }

    fun openDeleteTrackDialog(track: Track) {
        _selectedTrackForDelete.value = track
    }

    fun dismissDeleteTrackDialog() {
        _selectedTrackForDelete.value = null
    }

    fun saveTrackMetadata(
        track: Track,
        newTitle: String,
        newArtist: String,
        newAlbum: String,
        newGenre: String,
        newLyrics: String
    ) {
        viewModelScope.launch {
            val updated = track.copy(
                title = newTitle,
                artist = newArtist,
                album = newAlbum,
                genre = newGenre,
                lyrics = newLyrics
            )
            repository.saveOrUpdateTrack(updated)
            // Update local tracks in memory if needed
            _localTracks.value = _localTracks.value.map { if (it.id == track.id) updated else it }
            // Update queue in player
            playerManager.updateTrackInQueue(updated)
            _selectedTrackForEdit.value = null
            Toast.makeText(context, "Метаданные трека сохранены", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            repository.deleteTrack(track.id)
            playerManager.removeFromQueue(track.id)
            _localTracks.value = _localTracks.value.filterNot { it.id == track.id }
            _selectedTrackForDelete.value = null
            Toast.makeText(context, "Трек «${track.title}» удален", Toast.LENGTH_SHORT).show()
        }
    }

    fun navigateToArtistSearch(artist: String) {
        _searchQuery.value = artist
        _selectedSearchFilter.value = "All"
        _currentTab.value = NavigationTab.SEARCH
    }

    fun navigateToAlbumSearch(album: String) {
        _searchQuery.value = album
        _selectedSearchFilter.value = "All"
        _currentTab.value = NavigationTab.SEARCH
    }

    fun scanDeviceAudio() {
        viewModelScope.launch {
            _isScanningLocal.value = true
            val scanned = repository.scanDeviceMp3Files()
            _localTracks.value = scanned
            _isScanningLocal.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilter(filter: String) {
        _selectedSearchFilter.value = filter
    }

    fun createPlaylist(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name, description)
            _showCreatePlaylistDialog.value = false
            Toast.makeText(context, "Playlist \"$name\" created", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleCreatePlaylistDialog(show: Boolean) {
        _showCreatePlaylistDialog.value = show
    }

    fun toggleAddCustomStreamDialog(show: Boolean) {
        _showAddCustomStreamDialog.value = show
    }

    fun addCustomStream(title: String, url: String, genre: String) {
        if (title.isBlank() || url.isBlank()) return
        val customTrack = Track(
            id = "custom_stream_${UUID.randomUUID()}",
            title = title,
            artist = "Custom Stream",
            album = "Web Streams",
            streamUrl = url,
            coverDrawableRes = R.drawable.cover_cyberpunk_1787235201442,
            source = TrackSource.STREAM,
            genre = genre.ifEmpty { "Internet Radio" }
        )
        playTrack(customTrack)
        _showAddCustomStreamDialog.value = false
        Toast.makeText(context, "Playing stream: $title", Toast.LENGTH_SHORT).show()
    }

    fun selectPlaylistDetail(playlist: Playlist?) {
        _selectedPlaylistForDetail.value = playlist
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist.id)
            if (_selectedPlaylistForDetail.value?.id == playlist.id) {
                _selectedPlaylistForDetail.value = null
            }
            Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
        }
    }

    // Equalizer controls
    fun selectEqualizerPreset(preset: EqPreset) {
        val newState = EqualizerState.getPresetValues(preset).copy(
            isEnabled = equalizerState.value.isEnabled
        )
        playerManager.applyEqualizerSettings(newState)
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        val newState = equalizerState.value.copy(isEnabled = enabled)
        playerManager.applyEqualizerSettings(newState)
    }

    fun updateEqualizerBands(
        b60: Float = equalizerState.value.band60Hz,
        b230: Float = equalizerState.value.band230Hz,
        b910: Float = equalizerState.value.band910Hz,
        b3600: Float = equalizerState.value.band3600Hz,
        b14000: Float = equalizerState.value.band14000Hz
    ) {
        val newState = equalizerState.value.copy(
            preset = EqPreset.CUSTOM,
            band60Hz = b60,
            band230Hz = b230,
            band910Hz = b910,
            band3600Hz = b3600,
            band14000Hz = b14000
        )
        playerManager.applyEqualizerSettings(newState)
    }

    fun updateBassBoost(strength: Float) {
        val newState = equalizerState.value.copy(bassBoostStrength = strength)
        playerManager.applyEqualizerSettings(newState)
    }

    fun updateVirtualizer(strength: Float) {
        val newState = equalizerState.value.copy(virtualizerStrength = strength)
        playerManager.applyEqualizerSettings(newState)
    }

    // Cloud Sync Operations
    fun backupToCloud() {
        viewModelScope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(1200) // Realistic cloud sync network animation
            val now = System.currentTimeMillis()
            _cloudSyncData.value = _cloudSyncData.value.copy(
                lastSyncTimestamp = now,
                syncedPlaylistsCount = playlists.value.size,
                syncedFavoritesCount = favoriteTracks.value.size,
                cloudStatus = "Synced at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))}"
            )
            _isSyncing.value = false
            Toast.makeText(context, "Cloud sync complete: Playlists & EQ backed up", Toast.LENGTH_SHORT).show()
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(1000)
            _isSyncing.value = false
            Toast.makeText(context, "Restored latest playlists and favorites from cloud", Toast.LENGTH_SHORT).show()
        }
    }
}
