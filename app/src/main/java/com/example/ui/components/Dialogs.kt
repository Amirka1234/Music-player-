package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.Track
import com.example.util.TrackCoverImage
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurfaceDark,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                "Создать плейлист",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название плейлиста") },
                    placeholder = { Text("например, Мой плейлист 2026") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Описание (необязательно)") },
                    placeholder = { Text("например, Музыка для отдыха и тренировок") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, desc) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent),
                modifier = Modifier.testTag("playlist_create_confirm_button")
            ) {
                Text("Создать", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = ImmersiveLavenderLight)
            }
        }
    )
}

@Composable
fun AddCustomStreamDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://") }
    var genre by remember { mutableStateOf("Интернет-радио") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurfaceDark,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Radio, contentDescription = null, tint = ImmersiveLavenderAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Подключить поток", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Введите прямую ссылку на интернет-поток MP3, AAC или Icecast:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название станции / потока") },
                    placeholder = { Text("например, Chillhop Radio") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("stream_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL потока") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("stream_url_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Жанр") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, url, genre) },
                enabled = title.isNotBlank() && url.length > 8,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent),
                modifier = Modifier.testTag("stream_connect_confirm_button")
            ) {
                Text("Подключить", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = ImmersiveLavenderLight)
            }
        }
    )
}

@Composable
fun SleepTimerDialog(
    currentRemainingSeconds: Int?,
    onDismiss: () -> Unit,
    onSetTimer: (Int) -> Unit
) {
    val options = listOf(
        15 to "15 минут",
        30 to "30 минут",
        45 to "45 минут",
        60 to "1 час",
        90 to "1.5 часа"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurfaceDark,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = ImmersiveLavenderAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Таймер сна", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                if (currentRemainingSeconds != null) {
                    val mins = currentRemainingSeconds / 60
                    val secs = currentRemainingSeconds % 60
                    Text(
                        "Таймер активен: осталось %02d:%02d".format(mins, secs),
                        color = ImmersiveLavenderAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    Text(
                        "Автоматически выключить музыку, когда вы уснёте:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                options.forEach { (mins, label) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ImmersivePillInactive,
                        border = BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSetTimer(mins) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (currentRemainingSeconds != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onSetTimer(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Выключить таймер")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = ImmersiveLavenderLight)
            }
        }
    )
}

@Composable
fun LockScreenPreviewOverlay(
    track: Track?,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val currentDate = remember { SimpleDateFormat("EEEE, d MMMM", Locale("ru")).format(Date()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ImmersiveDarkBg,
                            ImmersivePurpleDark,
                            ImmersiveDarkBg
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -50) {
                            onDismiss()
                        }
                    }
                }
        ) {
            // Top lock icon & Dismiss button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Экран блокировки", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Разблокировать", tint = Color.White)
                }
            }

            // Big Clock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 90.dp)
            ) {
                Text(
                    text = currentTime,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = currentDate,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Lock Screen Spotify Music Player Widget
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark.copy(alpha = 0.94f)),
                border = BorderStroke(1.dp, ImmersiveCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("lock_screen_player_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TrackCoverImage(
                            track = track,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track?.title ?: "Spotify Music",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track?.artist ?: "Нажмите воспроизведение",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(ImmersiveLavenderAccent, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPlaying) "Сейчас играет" else "Пауза",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveLavenderAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Media Controls
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = onPrev,
                            modifier = Modifier.size(44.dp).testTag("lock_screen_prev_button")
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Предыдущий",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier
                                .size(56.dp)
                                .background(ImmersiveLavenderAccent, CircleShape)
                                .testTag("lock_screen_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Пауза" else "Воспроизведение",
                                tint = ImmersivePurpleDeep,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = onNext,
                            modifier = Modifier.size(44.dp).testTag("lock_screen_next_button")
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Следующий",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Bottom swipe up hint
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
                    .clickable { onDismiss() }
            ) {
                Icon(
                    Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Проведите вверх для разблокировки",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EditTrackDialog(
    track: Track,
    onDismiss: () -> Unit,
    onConfirm: (newTitle: String, newArtist: String, newAlbum: String, newGenre: String, newLyrics: String) -> Unit
) {
    var title by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist) }
    var album by remember { mutableStateOf(track.album) }
    var genre by remember { mutableStateOf(track.genre) }
    var lyrics by remember { mutableStateOf(track.lyrics) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurfaceDark,
        title = {
            Text(
                "Изменить метаданные",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название трека") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Исполнитель") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Альбом") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Жанр") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lyrics,
                    onValueChange = { lyrics = it },
                    label = { Text("Текст песни") },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, artist, album, genre, lyrics) },
                enabled = title.isNotBlank() && artist.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent)
            ) {
                Text("Сохранить", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = ImmersiveLavenderLight)
            }
        }
    )
}

@Composable
fun TrackLyricsDialog(
    track: Track,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurfaceDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Текст песни",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${track.title} • ${track.artist}",
                        fontSize = 12.sp,
                        color = ImmersiveLavenderAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = ImmersiveTextMuted)
                }
            }
        },
        text = {
            val lyricsText = if (track.lyrics.isNotBlank()) {
                track.lyrics
            } else {
                "Текст для этого трека пока не добавлен.\n\nВы можете добавить текст песни через контекстное меню трека (кнопка 'Изменить')."
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(ImmersiveDarkBg, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = lyricsText,
                            color = ImmersiveTextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent)
            ) {
                Text("Понятно", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ConfirmDeleteTrackDialog(
    track: Track,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurfaceDark,
        title = {
            Text(
                "Удалить трек?",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                "Вы уверены, что хотите удалить «${track.title}» из медиатеки и очереди воспроизведения?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
            ) {
                Text("Удалить", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = ImmersiveLavenderLight)
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    track: Track,
    playlists: List<com.example.model.Playlist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (playlistId: String) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurfaceDark,
        title = {
            Column {
                Text(
                    text = "Добавить в плейлист",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                Text(
                    text = "${track.title} • ${track.artist}",
                    color = ImmersiveLavenderAccent,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Button to create a new playlist
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ImmersiveLavenderAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ImmersiveLavenderAccent.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onCreateNewPlaylist()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ImmersiveLavenderAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = ImmersivePurpleDeep, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Создать новый плейлист",
                            fontWeight = FontWeight.SemiBold,
                            color = ImmersiveLavenderLight,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Нет доступных плейлистов.\nСоздайте первый плейлист выше!",
                            textAlign = TextAlign.Center,
                            color = ImmersiveTextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Text(
                        "Ваши плейлисты:",
                        color = ImmersiveTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = playlists,
                            key = { it.id },
                            contentType = { "playlist_picker_item" }
                        ) { playlist ->
                            val alreadyIn = playlist.trackIds.contains(track.id)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (alreadyIn) ImmersiveSurfaceDark else ImmersivePillInactive,
                                border = BorderStroke(1.dp, if (alreadyIn) ImmersiveLavenderAccent.copy(alpha = 0.5f) else ImmersiveCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !alreadyIn) {
                                        onSelectPlaylist(playlist.id)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = playlist.title,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (alreadyIn) ImmersiveLavenderAccent else ImmersiveTextPrimary,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${playlist.trackIds.size} треков",
                                            color = ImmersiveTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (alreadyIn) {
                                        Text(
                                            "Уже добавлен",
                                            color = ImmersiveLavenderAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = ImmersiveLavenderLight)
            }
        }
    )
}

@Composable
fun AddPodcastDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, host: String, url: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Подкаст") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurfaceDark,
        title = {
            Text(
                "Добавить подкаст по ссылке",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Введите прямую ссылку на аудиопоток или эпизод подкаста (MP3, AAC, HLS, Shoutcast):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название подкаста / выпуска") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("podcast_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Автор / Ведущий") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL потока / аудиофайла") },
                    placeholder = { Text("https://example.com/podcast.mp3") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("podcast_url_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория (напр. IT, Наука, Музыка)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveLavenderAccent,
                        focusedLabelColor = ImmersiveLavenderAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, host, url, category) },
                enabled = title.isNotBlank() && url.length > 8,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavenderAccent),
                modifier = Modifier.testTag("podcast_add_confirm_button")
            ) {
                Text("Добавить", color = ImmersivePurpleDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = ImmersiveLavenderLight)
            }
        }
    )
}


