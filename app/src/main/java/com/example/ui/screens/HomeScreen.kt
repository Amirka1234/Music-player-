package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Playlist
import com.example.model.PodcastShow
import com.example.model.Track
import com.example.util.TrackCoverImage
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersiveLavenderAccent
import com.example.ui.theme.ImmersiveLavenderLight
import com.example.ui.theme.ImmersivePillInactive
import com.example.ui.theme.ImmersivePurpleDeep
import com.example.ui.theme.ImmersiveSurfaceDark
import com.example.ui.theme.ImmersiveTextPrimary
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    featuredTracks: List<Track>,
    playlists: List<Playlist>,
    currentPlayingTrack: Track?,
    isPlaying: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    onOpenCloudSync: () -> Unit,
    onOpenTrackMenu: (Track) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Доброе утро"
            in 12..17 -> "Добрый день"
            else -> "Добрый вечер"
        }
    }

    var selectedQuickFilter by remember { mutableStateOf("Все") }
    val quickFilters = remember { listOf("Все", "Потоки & Радио", "Плейлисты") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Header with Greeting & Theme Toggle & Cloud Pill
        item(key = "header", contentType = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ImmersiveSurfaceDark,
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clickable { onOpenCloudSync() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = "Синхронизация",
                                tint = ImmersiveLavenderAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Облако активно",
                                fontSize = 11.sp,
                                color = ImmersiveLavenderLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .background(ImmersiveSurfaceDark, CircleShape)
                            .border(1.dp, ImmersiveCardBorder, CircleShape)
                            .size(42.dp)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Сменить тему",
                            tint = ImmersiveLavenderAccent
                        )
                    }
                }
            }
        }

        // Quick Category Filter Chips
        item(key = "filters", contentType = "filters") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                items(
                    items = quickFilters,
                    key = { it },
                    contentType = { "filter_chip" }
                ) { filter ->
                    val isSelected = selectedQuickFilter == filter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ImmersiveLavenderAccent else ImmersivePillInactive,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else ImmersiveCardBorder),
                        modifier = Modifier.clickable { selectedQuickFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) ImmersivePurpleDeep else ImmersiveLavenderLight,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Quick Access 2-Column Grid
        item(key = "quick_access", contentType = "quick_access") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val quickItems = featuredTracks.take(4)
                val rows = quickItems.chunked(2)
                for (row in rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (track in row) {
                            QuickAccessCard(
                                track = track,
                                isCurrentlyPlaying = currentPlayingTrack?.id == track.id && isPlaying,
                                onClick = { onPlayTrack(track, featuredTracks) },
                                onLongClick = { onOpenTrackMenu(track) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Section: 24/7 Internet Streams & Radio
        if (selectedQuickFilter == "Все" || selectedQuickFilter == "Потоки & Радио") {
            item(key = "section_streams", contentType = "section_streams") {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Онлайн-радио и аудиопотоки")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    items(
                        items = featuredTracks,
                        key = { it.id },
                        contentType = { "stream_card" }
                    ) { track ->
                        StreamCard(
                            track = track,
                            isCurrentlyPlaying = currentPlayingTrack?.id == track.id && isPlaying,
                            onClick = { onPlayTrack(track, featuredTracks) },
                            onMenuClick = { onOpenTrackMenu(track) }
                        )
                    }
                }
            }
        }

        // Section: Top Playlists
        if (selectedQuickFilter == "Все" || selectedQuickFilter == "Плейлисты") {
            item(key = "section_playlists", contentType = "section_playlists") {
                Spacer(modifier = Modifier.height(28.dp))
                SectionHeader(title = "Рекомендуемые плейлисты")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    items(
                        items = playlists,
                        key = { it.id },
                        contentType = { "playlist_card" }
                    ) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onSelectPlaylist(playlist) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp
        ),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickAccessCard(
    track: Track,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
        border = BorderStroke(1.dp, ImmersiveCardBorder),
        modifier = modifier
            .height(58.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            TrackCoverImage(
                track = track,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCurrentlyPlaying) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            if (isCurrentlyPlaying) {
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = "Playing",
                    tint = ImmersiveLavenderAccent,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StreamCard(
    track: Track,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
        border = BorderStroke(1.dp, ImmersiveCardBorder),
        modifier = Modifier
            .width(155.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box {
                TrackCoverImage(
                    track = track,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(135.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                // Play badge button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(38.dp)
                        .background(ImmersiveLavenderAccent, CircleShape)
                        .shadow(8.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Воспроизвести",
                        tint = ImmersivePurpleDeep,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Context menu 3-dots button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(30.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .clickable { onMenuClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Меню",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentlyPlaying) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
        border = BorderStroke(1.dp, ImmersiveCardBorder),
        modifier = Modifier
            .width(155.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            val cover = playlist.coverRes ?: R.drawable.cover_cyberpunk_1787235201442
            Image(
                painter = painterResource(id = cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(135.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = playlist.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = playlist.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PodcastShowCard(
    show: PodcastShow,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
        border = BorderStroke(1.dp, ImmersiveCardBorder),
        modifier = Modifier
            .width(155.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            val cover = show.coverRes ?: R.drawable.cover_cyberpunk_1787235201442
            Image(
                painter = painterResource(id = cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(135.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = show.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${show.host} • ${show.episodes.size} эп.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
