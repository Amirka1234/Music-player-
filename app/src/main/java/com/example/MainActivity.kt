package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.Track
import com.example.ui.components.AddCustomStreamDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.FullScreenPlayerSheet
import com.example.ui.components.LockScreenPreviewOverlay
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.SleepTimerDialog
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PodcastsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.ImmersiveLavenderAccent
import com.example.ui.theme.ImmersiveLavenderLight
import com.example.ui.theme.ImmersivePillInactive
import com.example.ui.theme.ImmersivePurpleDeep
import com.example.ui.theme.ImmersiveSurfaceDark
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpotifyDarkBg
import com.example.ui.theme.SpotifyGreen
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MusicViewModel) {
    val context = LocalContext.current

    // Request permissions launcher for audio and notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_MEDIA_AUDIO] == true ||
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
            viewModel.scanDeviceAudio()
        }
    }

    LaunchedEffect(Unit) {
        val neededPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    // State bindings from ViewModel
    val currentTab by viewModel.currentTab.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val isLockScreenPreviewActive by viewModel.isLockScreenPreviewActive.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val equalizerState by viewModel.equalizerState.collectAsState()
    val sleepTimerSeconds by viewModel.sleepTimerSeconds.collectAsState()
    val visualizerAmplitudes by viewModel.visualizerAmplitudes.collectAsState()

    val playlists by viewModel.playlists.collectAsState()
    val favoriteTracks by viewModel.favoriteTracks.collectAsState()
    val localTracks by viewModel.localTracks.collectAsState()
    val isScanningLocal by viewModel.isScanningLocal.collectAsState()
    val cloudSyncData by viewModel.cloudSyncData.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSearchFilter by viewModel.selectedSearchFilter.collectAsState()

    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()
    val showAddCustomStreamDialog by viewModel.showAddCustomStreamDialog.collectAsState()
    val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsState()
    val selectedPlaylistDetail by viewModel.selectedPlaylistForDetail.collectAsState()

    val allTracksCombined = (viewModel.featuredStreams + localTracks + favoriteTracks).distinctBy { it.id }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                // Spotify Floating Mini Player
                val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                MiniPlayerBar(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    progress = progress,
                    isBuffering = isBuffering,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onNext = { viewModel.skipToNext() },
                    onToggleFavorite = { currentTrack?.let { viewModel.toggleFavorite(it) } },
                    onExpand = { viewModel.setPlayerExpanded(true) }
                )

                // Bottom Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ImmersiveLavenderLight,
                        selectedTextColor = ImmersiveLavenderAccent,
                        indicatorColor = ImmersivePillInactive,
                        unselectedIconColor = ImmersiveTextMuted,
                        unselectedTextColor = ImmersiveTextMuted
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.HOME,
                        onClick = { viewModel.setTab(NavigationTab.HOME) },
                        icon = {
                            Icon(
                                if (currentTab == NavigationTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.SEARCH,
                        onClick = { viewModel.setTab(NavigationTab.SEARCH) },
                        icon = {
                            Icon(
                                if (currentTab == NavigationTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                contentDescription = "Search"
                            )
                        },
                        label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_item_search")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.LIBRARY,
                        onClick = { viewModel.setTab(NavigationTab.LIBRARY) },
                        icon = {
                            Icon(
                                if (currentTab == NavigationTab.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                contentDescription = "Your Library"
                            )
                        },
                        label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_item_library")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.PODCASTS,
                        onClick = { viewModel.setTab(NavigationTab.PODCASTS) },
                        icon = {
                            Icon(
                                if (currentTab == NavigationTab.PODCASTS) Icons.Filled.Mic else Icons.Outlined.Mic,
                                contentDescription = "Podcasts"
                            )
                        },
                        label = { Text("Podcasts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_item_podcasts")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.EQUALIZER,
                        onClick = { viewModel.setTab(NavigationTab.EQUALIZER) },
                        icon = {
                            Icon(
                                if (currentTab == NavigationTab.EQUALIZER) Icons.Filled.Equalizer else Icons.Outlined.Equalizer,
                                contentDescription = "Equalizer"
                            )
                        },
                        label = { Text("Equalizer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_item_equalizer")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Ambient glowing background overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Top-left lavender glow (#D0BCFF blur effect)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ImmersiveLavenderAccent.copy(alpha = 0.18f),
                            ImmersiveLavenderAccent.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.05f, height * 0.05f),
                        radius = width * 0.85f
                    ),
                    radius = width * 0.85f,
                    center = Offset(width * 0.05f, height * 0.05f)
                )

                // Bottom-right deep purple glow (#381E72 blur effect)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ImmersivePurpleDeep.copy(alpha = 0.35f),
                            ImmersivePurpleDeep.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.95f, height * 0.85f),
                        radius = width * 0.80f
                    ),
                    radius = width * 0.80f,
                    center = Offset(width * 0.95f, height * 0.85f)
                )
            }
            when (currentTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        featuredTracks = viewModel.featuredStreams,
                        playlists = playlists,
                        podcasts = viewModel.podcastShows,
                        currentPlayingTrack = currentTrack,
                        isPlaying = isPlaying,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { viewModel.toggleTheme() },
                        onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                        onSelectPlaylist = { playlist ->
                            viewModel.selectPlaylistDetail(playlist)
                            viewModel.setTab(NavigationTab.LIBRARY)
                        },
                        onOpenPodcast = { show ->
                            viewModel.setTab(NavigationTab.PODCASTS)
                        },
                        onOpenCloudSync = {
                            viewModel.setTab(NavigationTab.LIBRARY)
                        }
                    )
                }

                NavigationTab.SEARCH -> {
                    SearchScreen(
                        searchQuery = searchQuery,
                        selectedFilter = selectedSearchFilter,
                        allTracks = allTracksCombined,
                        currentPlayingTrack = currentTrack,
                        isPlaying = isPlaying,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onFilterChange = { viewModel.setSearchFilter(it) },
                        onPlayTrack = { track -> viewModel.playTrack(track, allTracksCombined) },
                        onOpenAddCustomStream = { viewModel.toggleAddCustomStreamDialog(true) }
                    )
                }

                NavigationTab.LIBRARY -> {
                    LibraryScreen(
                        playlists = playlists,
                        localTracks = localTracks,
                        favoriteTracks = favoriteTracks,
                        allTracks = allTracksCombined,
                        cloudSyncData = cloudSyncData,
                        isScanningLocal = isScanningLocal,
                        isSyncing = isSyncing,
                        currentPlayingTrack = currentTrack,
                        isPlaying = isPlaying,
                        selectedPlaylistDetail = selectedPlaylistDetail,
                        onSelectPlaylistDetail = { viewModel.selectPlaylistDetail(it) },
                        onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                        onOpenCreatePlaylist = { viewModel.toggleCreatePlaylistDialog(true) },
                        onScanDeviceAudio = { viewModel.scanDeviceAudio() },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onBackupCloud = { viewModel.backupToCloud() },
                        onRestoreCloud = { viewModel.restoreFromCloud() }
                    )
                }

                NavigationTab.PODCASTS -> {
                    PodcastsScreen(
                        shows = viewModel.podcastShows,
                        currentPlayingTrack = currentTrack,
                        isPlaying = isPlaying,
                        playbackSpeed = playbackSpeed,
                        onPlayEpisode = { ep ->
                            val epTrack = Track(
                                id = ep.id,
                                title = ep.title,
                                artist = ep.showTitle,
                                album = "Podcasts",
                                streamUrl = ep.audioUrl,
                                coverDrawableRes = ep.coverRes,
                                durationMs = ep.durationMs,
                                source = com.example.model.TrackSource.PODCAST,
                                genre = "Podcast",
                                lyrics = ep.description
                            )
                            viewModel.playTrack(epTrack)
                        },
                        onSetSpeed = { viewModel.setPlaybackSpeed(it) }
                    )
                }

                NavigationTab.EQUALIZER -> {
                    EqualizerScreen(
                        state = equalizerState,
                        onToggleEnable = { viewModel.setEqualizerEnabled(it) },
                        onSelectPreset = { viewModel.selectEqualizerPreset(it) },
                        onUpdateBands = { b60, b230, b910, b3600, b14000 ->
                            viewModel.updateEqualizerBands(b60, b230, b910, b3600, b14000)
                        },
                        onUpdateBassBoost = { viewModel.updateBassBoost(it) },
                        onUpdateVirtualizer = { viewModel.updateVirtualizer(it) }
                    )
                }
            }
        }
    }

    // Full Screen Player Sheet
    if (isPlayerExpanded && currentTrack != null) {
        FullScreenPlayerSheet(
            track = currentTrack,
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            playbackSpeed = playbackSpeed,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            sleepTimerSeconds = sleepTimerSeconds,
            visualizerAmplitudes = visualizerAmplitudes,
            onDismiss = { viewModel.setPlayerExpanded(false) },
            onTogglePlay = { viewModel.togglePlayPause() },
            onSeek = { viewModel.seekTo(it) },
            onNext = { viewModel.skipToNext() },
            onPrev = { viewModel.skipToPrevious() },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onToggleFavorite = { currentTrack?.let { viewModel.toggleFavorite(it) } },
            onSetSpeed = { viewModel.setPlaybackSpeed(it) },
            onOpenEqualizer = {
                viewModel.setTab(NavigationTab.EQUALIZER)
            },
            onOpenSleepTimer = { viewModel.toggleSleepTimerDialog(true) },
            onOpenLockScreenPreview = { viewModel.setLockScreenPreviewActive(true) }
        )
    }

    // Dialogs
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { viewModel.toggleCreatePlaylistDialog(false) },
            onConfirm = { name, desc -> viewModel.createPlaylist(name, desc) }
        )
    }

    if (showAddCustomStreamDialog) {
        AddCustomStreamDialog(
            onDismiss = { viewModel.toggleAddCustomStreamDialog(false) },
            onConfirm = { title, url, genre -> viewModel.addCustomStream(title, url, genre) }
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentRemainingSeconds = sleepTimerSeconds,
            onDismiss = { viewModel.toggleSleepTimerDialog(false) },
            onSetTimer = { mins -> viewModel.setSleepTimer(mins) }
        )
    }

    if (isLockScreenPreviewActive) {
        LockScreenPreviewOverlay(
            track = currentTrack,
            isPlaying = isPlaying,
            onTogglePlay = { viewModel.togglePlayPause() },
            onNext = { viewModel.skipToNext() },
            onPrev = { viewModel.skipToPrevious() },
            onDismiss = { viewModel.setLockScreenPreviewActive(false) }
        )
    }
}
