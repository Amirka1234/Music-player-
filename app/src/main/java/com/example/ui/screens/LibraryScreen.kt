package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CloudSyncData
import com.example.model.Playlist
import com.example.model.Track
import com.example.model.TrackSource
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersiveLavenderAccent
import com.example.ui.theme.ImmersiveLavenderLight
import com.example.ui.theme.ImmersivePillInactive
import com.example.ui.theme.ImmersivePurpleDeep
import com.example.ui.theme.ImmersiveSurfaceDark
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun LibraryScreen(
    playlists: List<Playlist>,
    localTracks: List<Track>,
    favoriteTracks: List<Track>,
    allTracks: List<Track>,
    cloudSyncData: CloudSyncData,
    isScanningLocal: Boolean,
    isSyncing: Boolean,
    currentPlayingTrack: Track?,
    isPlaying: Boolean,
    selectedPlaylistDetail: Playlist?,
    onSelectPlaylistDetail: (Playlist?) -> Unit,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onOpenCreatePlaylist: () -> Unit,
    onScanDeviceAudio: () -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onBackupCloud: () -> Unit,
    onRestoreCloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf("Playlists") }
    val sections = listOf("Playlists", "Local MP3s", "Favorites", "Cloud Sync")

    // If viewing a playlist detail
    if (selectedPlaylistDetail != null) {
        PlaylistDetailView(
            playlist = selectedPlaylistDetail,
            allTracks = allTracks,
            currentPlayingTrack = currentPlayingTrack,
            isPlaying = isPlaying,
            onBack = { onSelectPlaylistDetail(null) },
            onPlayTrack = onPlayTrack,
            onDelete = {
                onDeletePlaylist(selectedPlaylistDetail)
                onSelectPlaylistDetail(null)
            },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                if (selectedSection == "Playlists") {
                    IconButton(
                        onClick = onOpenCreatePlaylist,
                        modifier = Modifier
                            .size(38.dp)
                            .background(ImmersiveLavenderAccent, CircleShape)
                            .testTag("create_playlist_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = ImmersivePurpleDeep)
                    }
                } else if (selectedSection == "Local MP3s") {
                    IconButton(
                        onClick = onScanDeviceAudio,
                        modifier = Modifier.testTag("scan_device_audio_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan Audio", tint = ImmersiveLavenderAccent)
                    }
                }
            }
        }

        // Section Tabs
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(sections) { section ->
                    val isSelected = selectedSection == section
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ImmersiveLavenderAccent else ImmersivePillInactive,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else ImmersiveCardBorder),
                        modifier = Modifier.clickable { selectedSection = section }
                    ) {
                        Text(
                            text = section,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) ImmersivePurpleDeep else ImmersiveLavenderLight,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Section Content
        when (selectedSection) {
            "Playlists" -> {
                // Create Playlist Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onOpenCreatePlaylist() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(ImmersiveLavenderAccent, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ImmersivePurpleDeep)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    "Create Playlist",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Build custom mix of streams and songs",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                items(playlists) { playlist ->
                    PlaylistItemRow(
                        playlist = playlist,
                        onClick = { onSelectPlaylistDetail(playlist) }
                    )
                }
            }

            "Local MP3s" -> {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = ImmersiveLavenderAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Device Storage (${localTracks.size} tracks)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Internal phone memory and SD card MP3s",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isScanningLocal) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ImmersiveLavenderAccent)
                            } else {
                                Button(
                                    onClick = onScanDeviceAudio,
                                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Scan", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                items(localTracks) { track ->
                    TrackItemRow(
                        track = track,
                        isCurrentlyPlaying = currentPlayingTrack?.id == track.id && isPlaying,
                        onClick = { onPlayTrack(track, localTracks) }
                    )
                }
            }

            "Favorites" -> {
                if (favoriteTracks.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = ImmersiveLavenderAccent, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Favorite Tracks Yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                "Tap the heart icon on any song or stream to save it here.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    items(favoriteTracks) { track ->
                        TrackItemRow(
                            track = track,
                            isCurrentlyPlaying = currentPlayingTrack?.id == track.id && isPlaying,
                            onClick = { onPlayTrack(track, favoriteTracks) }
                        )
                    }
                }
            }

            "Cloud Sync" -> {
                item {
                    CloudSyncDashboard(
                        data = cloudSyncData,
                        isSyncing = isSyncing,
                        onBackup = onBackupCloud,
                        onRestore = onRestoreCloud
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistItemRow(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val cover = playlist.coverRes ?: R.drawable.cover_cyberpunk_1787235201442
            Image(
                painter = painterResource(id = cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.title,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (playlist.isCloudSynced) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "Synced",
                            tint = ImmersiveLavenderAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "Playlist • ${playlist.description}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Open",
                tint = ImmersiveLavenderAccent
            )
        }
    }
}

@Composable
private fun TrackItemRow(
    track: Track,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val cover = track.coverDrawableRes ?: R.drawable.cover_chillhop_1787235245557
            Image(
                painter = painterResource(id = cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCurrentlyPlaying) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.album.ifEmpty { "Local File" }}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = if (isCurrentlyPlaying) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: Playlist,
    allTracks: List<Track>,
    currentPlayingTrack: Track?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlistTracks = allTracks.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_view"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text("Playlist", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                val cover = playlist.coverRes ?: R.drawable.cover_cyberpunk_1787235201442
                Image(
                    painter = painterResource(id = cover),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = playlist.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = playlist.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        playlistTracks.firstOrNull()?.let { onPlayTrack(it, playlistTracks) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ImmersivePurpleDeep)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Playlist", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tracks in Playlist (${playlistTracks.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(playlistTracks) { track ->
            TrackItemRow(
                track = track,
                isCurrentlyPlaying = currentPlayingTrack?.id == track.id && isPlaying,
                onClick = { onPlayTrack(track, playlistTracks) }
            )
        }
    }
}

@Composable
private fun CloudSyncDashboard(
    data: CloudSyncData,
    isSyncing: Boolean,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    var autoSync by remember { mutableStateOf(data.isAutoSyncEnabled) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ImmersiveLavenderAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = ImmersivePurpleDeep)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cloud Account Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = data.accountEmail,
                            color = ImmersiveLavenderAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Connected Device", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(data.syncDeviceId, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Cloud Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(data.cloudStatus, color = ImmersiveLavenderAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = ImmersiveLavenderAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Real-Time Cloud Sync", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Switch(
                        checked = autoSync,
                        onCheckedChange = { autoSync = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ImmersiveLavenderAccent, checkedTrackColor = ImmersiveLavenderAccent.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Backup / Restore
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onBackup,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent),
                        modifier = Modifier.weight(1f).testTag("backup_to_cloud_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ImmersivePurpleDeep)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ImmersivePurpleDeep, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onRestore,
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f).testTag("restore_from_cloud_button")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = ImmersiveLavenderAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Pairing Sync Key Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = AccentCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Multi-Device Sync Pairing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Use this pairing code on your tablet, car, or desktop to sync all playlists, EQ settings, and playback position:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, ImmersiveCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SPOTIFY-SYNC-9842-X",
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveLavenderAccent,
                            letterSpacing = 2.sp,
                            fontSize = 14.sp
                        )
                        Text("ACTIVE", color = ImmersiveLavenderLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
