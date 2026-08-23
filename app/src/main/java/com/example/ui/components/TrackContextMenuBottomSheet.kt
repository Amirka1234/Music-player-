package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersiveDarkBg
import com.example.ui.theme.ImmersiveLavenderAccent
import com.example.ui.theme.ImmersiveSurfaceDark
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.util.TrackCoverImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackContextMenuBottomSheet(
    track: Track,
    isInQueue: Boolean = false,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveFromQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGoToArtist: (String) -> Unit,
    onGoToAlbum: (String) -> Unit,
    onEditTrack: () -> Unit,
    onDeleteTrack: () -> Unit,
    onShowLyrics: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ImmersiveDarkBg,
        dragHandle = null,
        modifier = Modifier.testTag("track_context_menu_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Drag indicator handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ImmersiveTextMuted.copy(alpha = 0.4f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Track Header Preview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ImmersiveSurfaceDark,
                border = BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    TrackCoverImage(
                        track = track,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track.artist,
                            color = ImmersiveLavenderAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (track.album.isNotEmpty()) {
                            Text(
                                text = track.album,
                                color = ImmersiveTextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Context Menu Actions List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Play Next
                ContextMenuActionItem(
                    icon = Icons.Default.PlaylistPlay,
                    label = "Воспроизвести далее",
                    onClick = {
                        onDismiss()
                        onPlayNext()
                    }
                )

                // Add to Queue / Remove from Queue
                if (isInQueue) {
                    ContextMenuActionItem(
                        icon = Icons.Default.RemoveCircleOutline,
                        label = "Удалить из очереди",
                        iconTint = Color(0xFFEF5350),
                        onClick = {
                            onDismiss()
                            onRemoveFromQueue()
                        }
                    )
                } else {
                    ContextMenuActionItem(
                        icon = Icons.Default.QueueMusic,
                        label = "Добавить в очередь",
                        onClick = {
                            onDismiss()
                            onAddToQueue()
                        }
                    )
                }

                // Add to Playlist
                ContextMenuActionItem(
                    icon = Icons.Default.PlaylistAdd,
                    label = "Добавить в плейлист",
                    onClick = {
                        onDismiss()
                        onAddToPlaylist()
                    }
                )

                // Favorite Toggle
                ContextMenuActionItem(
                    icon = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = if (track.isFavorite) "Удалить из любимых" else "Добавить в любимые треки",
                    iconTint = if (track.isFavorite) ImmersiveLavenderAccent else ImmersiveTextSecondary,
                    onClick = {
                        onDismiss()
                        onToggleFavorite()
                    }
                )

                // View lyrics
                ContextMenuActionItem(
                    icon = Icons.Default.Lyrics,
                    label = "Посмотреть текст песни",
                    onClick = {
                        onDismiss()
                        onShowLyrics()
                    }
                )

                Divider(
                    color = ImmersiveCardBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Go to Artist
                ContextMenuActionItem(
                    icon = Icons.Default.Person,
                    label = "Перейти к исполнителю (${track.artist})",
                    onClick = {
                        onDismiss()
                        onGoToArtist(track.artist)
                    }
                )

                // Go to Album
                if (track.album.isNotEmpty()) {
                    ContextMenuActionItem(
                        icon = Icons.Default.Album,
                        label = "Перейти к альбому (${track.album})",
                        onClick = {
                            onDismiss()
                            onGoToAlbum(track.album)
                        }
                    )
                }

                Divider(
                    color = ImmersiveCardBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Edit metadata
                ContextMenuActionItem(
                    icon = Icons.Default.Edit,
                    label = "Изменить метаданные (название, артист)",
                    onClick = {
                        onDismiss()
                        onEditTrack()
                    }
                )

                // Delete Track
                ContextMenuActionItem(
                    icon = Icons.Default.Delete,
                    label = "Удалить трек",
                    iconTint = Color(0xFFEF5350),
                    textColor = Color(0xFFEF5350),
                    onClick = {
                        onDismiss()
                        onDeleteTrack()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ContextMenuActionItem(
    icon: ImageVector,
    label: String,
    iconTint: Color = ImmersiveTextSecondary,
    textColor: Color = ImmersiveTextPrimary,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
