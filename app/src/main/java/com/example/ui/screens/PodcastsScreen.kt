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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PodcastEpisode
import com.example.model.PodcastShow
import com.example.model.Track
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersiveLavenderAccent
import com.example.ui.theme.ImmersiveLavenderLight
import com.example.ui.theme.ImmersivePillInactive
import com.example.ui.theme.ImmersivePurpleDeep
import com.example.ui.theme.ImmersiveSurfaceDark
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary

@Composable
fun PodcastsScreen(
    shows: List<PodcastShow>,
    currentPlayingTrack: Track?,
    isPlaying: Boolean,
    playbackSpeed: Float,
    onPlayEpisode: (PodcastEpisode) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onOpenAddPodcast: () -> Unit,
    onDeletePodcast: (PodcastShow) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedShowId by remember(shows) { mutableStateOf(shows.firstOrNull()?.id ?: "") }
    val selectedShow = shows.firstOrNull { it.id == selectedShowId } ?: shows.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("podcasts_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Screen Title & Actions
        item(key = "podcast_header", contentType = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Подкасты",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Добавление по ссылке и потоки",
                        style = MaterialTheme.typography.bodySmall,
                        color = ImmersiveTextMuted
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speed indicator badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ImmersiveSurfaceDark,
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier.clickable {
                            val speeds = listOf(0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f)
                            val currIdx = speeds.indexOfFirst { kotlin.math.abs(it - playbackSpeed) < 0.05f }
                            val nextSpeed = speeds[(if (currIdx >= 0) currIdx + 1 else 0) % speeds.size]
                            onSetSpeed(nextSpeed)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = ImmersiveLavenderAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${playbackSpeed}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ImmersiveLavenderAccent)
                        }
                    }

                    // Add podcast button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ImmersiveLavenderAccent,
                        modifier = Modifier
                            .clickable { onOpenAddPodcast() }
                            .testTag("add_podcast_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить", tint = ImmersivePurpleDeep, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("По ссылке", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ImmersivePurpleDeep)
                        }
                    }
                }
            }
        }

        if (shows.isEmpty()) {
            // Empty State
            item(key = "podcast_empty_state", contentType = "empty_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = ImmersiveSurfaceDark,
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(ImmersiveLavenderAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = ImmersiveLavenderAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Нет добавленных подкастов",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Добавьте любой подкаст или аудиопоток по прямой ссылке (MP3, AAC, HLS, интернет-радио).",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onOpenAddPodcast,
                                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("empty_state_add_podcast_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ImmersivePurpleDeep)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Добавить подкаст по ссылке", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Show selector tabs
            item(key = "podcast_tabs", contentType = "tabs") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(
                        items = shows,
                        key = { it.id },
                        contentType = { "podcast_chip" }
                    ) { show ->
                        val isSelected = selectedShowId == show.id
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) ImmersiveLavenderAccent else ImmersivePillInactive,
                            border = BorderStroke(1.dp, if (isSelected) Color.Transparent else ImmersiveCardBorder),
                            modifier = Modifier.clickable { selectedShowId = show.id }
                        ) {
                            Text(
                                text = show.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) ImmersivePurpleDeep else ImmersiveLavenderLight,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Show Detail Banner
            if (selectedShow != null) {
                item(key = "selected_show_banner_${selectedShow.id}", contentType = "banner") {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val cover = selectedShow.coverRes ?: R.drawable.cover_podcast_1787235232773
                            Image(
                                painter = painterResource(id = cover),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedShow.category.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveLavenderAccent,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = selectedShow.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Автор: ${selectedShow.host}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedShow.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { onDeletePodcast(selectedShow) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Удалить подкаст",
                                    tint = Color(0xFFEF5350)
                                )
                            }
                        }
                    }
                }

                item(key = "episodes_header_${selectedShow.id}", contentType = "section_header") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Выпуски (${selectedShow.episodes.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(
                    items = selectedShow.episodes,
                    key = { it.id },
                    contentType = { "episode_card" }
                ) { ep ->
                    val isCurrentEpPlaying = currentPlayingTrack?.id == ep.id && isPlaying

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onPlayEpisode(ep) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = ep.publishedDate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveLavenderAccent
                                )
                                if (ep.durationMs > 0) {
                                    Text(
                                        text = "${ep.durationMs / 60000} мин",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "Прямой поток",
                                        fontSize = 11.sp,
                                        color = ImmersiveLavenderAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = ep.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isCurrentEpPlaying) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = ep.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(ImmersiveLavenderAccent, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Слушать выпуск",
                                            tint = ImmersivePurpleDeep,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isCurrentEpPlaying) "Воспроизводится" else "Слушать поток",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentEpPlaying) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Icon(
                                    Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (isCurrentEpPlaying) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

