package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.example.model.Track
import com.example.model.TrackSource
import com.example.player.RepeatMode
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersiveDarkBg
import com.example.ui.theme.ImmersiveLavenderAccent
import com.example.ui.theme.ImmersiveLavenderLight
import com.example.ui.theme.ImmersivePillInactive
import com.example.ui.theme.ImmersivePurpleDark
import com.example.ui.theme.ImmersivePurpleDeep
import com.example.ui.theme.ImmersiveSurfaceDark
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.ImmersiveTrackInactive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPlayerSheet(
    track: Track?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    sleepTimerSeconds: Int?,
    visualizerAmplitudes: List<Float>,
    onDismiss: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenLockScreenPreview: () -> Unit
) {
    if (track == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var showMenu by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    val effectivePosition = if (isDraggingSlider) {
        (sliderValue * duration.coerceAtLeast(1L)).toLong()
    } else {
        currentPosition
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ImmersiveDarkBg,
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF322845),
                            ImmersiveDarkBg,
                            Color(0xFF141218)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("full_player_collapse_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = ImmersiveTextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (track.source) {
                                TrackSource.STREAM -> "STREAMING AUDIO"
                                TrackSource.PODCAST -> "PLAYING PODCAST"
                                TrackSource.LOCAL -> "DEVICE AUDIO FILE"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ImmersiveLavenderAccent,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = track.album.ifEmpty { "Spotify Music" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ImmersiveTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = ImmersiveTextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(ImmersiveSurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Equalizer & Sound FX", color = ImmersiveTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onDismiss()
                                    onOpenEqualizer()
                                },
                                leadingIcon = { Icon(Icons.Default.Equalizer, null, tint = ImmersiveLavenderAccent) }
                            )
                            DropdownMenuItem(
                                text = { Text("Sleep Timer", color = ImmersiveTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onOpenSleepTimer()
                                },
                                leadingIcon = { Icon(Icons.Default.Timer, null, tint = ImmersiveLavenderAccent) }
                            )
                            DropdownMenuItem(
                                text = { Text("Preview Lock Screen Widget", color = ImmersiveTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onOpenLockScreenPreview()
                                },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = AccentCyan) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Album Artwork
                val coverRes = track.coverDrawableRes ?: R.drawable.cover_cyberpunk_1787235201442
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                    border = BorderStroke(1.dp, ImmersiveCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = ImmersiveLavenderAccent, spotColor = ImmersiveLavenderAccent)
                ) {
                    Image(
                        painter = painterResource(id = coverRes),
                        contentDescription = "Album Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Track Title & Favorite Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary,
                                fontSize = 22.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ImmersiveTextSecondary,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("full_player_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (track.isFavorite) ImmersiveLavenderAccent else ImmersiveTextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Equalizer Visualizer Bars Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    val barCount = visualizerAmplitudes.size
                    val spacing = 4.dp.toPx()
                    val totalSpacing = spacing * (barCount - 1)
                    val barWidth = (size.width - totalSpacing) / barCount
                    val maxHeight = size.height

                    for (i in 0 until barCount) {
                        val amplitude = if (isPlaying) visualizerAmplitudes.getOrElse(i) { 0.2f } else 0.08f
                        val barHeight = (maxHeight * amplitude).coerceAtLeast(4.dp.toPx())
                        val left = i * (barWidth + spacing)
                        val top = maxHeight - barHeight

                        drawRoundRect(
                            color = if (isPlaying) ImmersiveLavenderAccent else ImmersiveTrackInactive,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrubber Slider
                val progressFraction = if (duration > 0) {
                    (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Slider(
                    value = if (isDraggingSlider) sliderValue else progressFraction,
                    onValueChange = {
                        isDraggingSlider = true
                        sliderValue = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        val targetMs = (sliderValue * duration.coerceAtLeast(1L)).toLong()
                        onSeek(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = ImmersiveLavenderLight,
                        activeTrackColor = ImmersiveLavenderAccent,
                        inactiveTrackColor = ImmersiveTrackInactive
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_player_seek_slider")
                )

                // Timestamp counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(effectivePosition),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ImmersiveTextMuted,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = if (duration > 0) formatTime(duration) else "LIVE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (duration > 0) ImmersiveTextMuted else ImmersiveLavenderAccent,
                            fontWeight = if (duration > 0) FontWeight.Normal else FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.testTag("full_player_shuffle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) ImmersiveLavenderAccent else ImmersiveTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Prev Button
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier.testTag("full_player_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = ImmersiveTextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Play/Pause Big Button
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(66.dp)
                            .background(ImmersiveLavenderLight, CircleShape)
                            .shadow(16.dp, CircleShape, spotColor = ImmersiveLavenderAccent)
                            .testTag("full_player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = ImmersivePurpleDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Next Button
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.testTag("full_player_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = ImmersiveTextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repeat Button
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.testTag("full_player_repeat_button")
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat Mode",
                            tint = if (repeatMode != RepeatMode.OFF) ImmersiveLavenderAccent else ImmersiveTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Utility Row: Speed, Equalizer, Sleep Timer, Lyrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Playback Speed
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ImmersivePillInactive,
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = ImmersiveLavenderAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${playbackSpeed}x", color = ImmersiveLavenderLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Equalizer Button
                    IconButton(
                        onClick = {
                            onDismiss()
                            onOpenEqualizer()
                        },
                        modifier = Modifier.testTag("full_player_eq_shortcut")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = ImmersiveLavenderAccent
                        )
                    }

                    // Sleep Timer
                    IconButton(onClick = onOpenSleepTimer) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerSeconds != null) ImmersiveLavenderAccent else ImmersiveTextMuted
                        )
                    }

                    // Lock Screen Widget Preview
                    IconButton(onClick = onOpenLockScreenPreview) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Screen",
                            tint = ImmersiveTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Lyrics / Show Notes Card
                if (track.lyrics.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLyrics = !showLyrics }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (track.source == TrackSource.PODCAST) "Episode Notes" else "Lyrics & Notes",
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveTextPrimary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (showLyrics) "Hide" else "Show",
                                    color = ImmersiveLavenderAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            AnimatedVisibility(
                                visible = showLyrics,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Text(
                                    text = track.lyrics,
                                    color = ImmersiveTextSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
