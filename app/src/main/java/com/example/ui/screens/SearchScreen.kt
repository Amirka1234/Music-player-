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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.model.Track
import com.example.model.TrackSource
import com.example.util.TrackCoverImage
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersiveLavenderAccent
import com.example.ui.theme.ImmersiveLavenderLight
import com.example.ui.theme.ImmersivePillInactive
import com.example.ui.theme.ImmersivePurpleDeep
import com.example.ui.theme.ImmersiveSurfaceDark
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

data class BrowseCategory(
    val title: String,
    val color1: Color,
    val color2: Color,
    val genreFilter: String
)

@Composable
fun SearchScreen(
    searchQuery: String,
    selectedFilter: String,
    allTracks: List<Track>,
    currentPlayingTrack: Track?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onPlayTrack: (Track) -> Unit,
    onOpenAddCustomStream: () -> Unit,
    onOpenTrackMenu: (Track) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filters = listOf("Все", "Потоки & Радио", "Подкасты", "Локальные")

    val categories = listOf(
        BrowseCategory("Лоу-фай и Чилл", Color(0xFF2C2456), Color(0xFF5B3E96), "Lo-Fi"),
        BrowseCategory("Синтвейв 80-х", Color(0xFF6B21A8), Color(0xFFA855F7), "Synthwave"),
        BrowseCategory("Электроника & EDM", Color(0xFF1E3A8A), Color(0xFF3B82F6), "Electronic"),
        BrowseCategory("Акустика и Фолк", Color(0xFF9A3412), Color(0xFFF97316), "Acoustic"),
        BrowseCategory("Технологии & Подкасты", Color(0xFF831843), Color(0xFFEC4899), "Podcasts"),
        BrowseCategory("Глубокий Фокус", Color(0xFF134E4A), Color(0xFF14B8A6), "Focus"),
        BrowseCategory("Джаз и Блюз", Color(0xFF312E81), Color(0xFF6366F1), "Jazz"),
        BrowseCategory("Рок и Метал", Color(0xFF881337), Color(0xFFE11D48), "Rock")
    )

    val filteredTracks = allTracks.filter { track ->
        val matchesQuery = searchQuery.isBlank() ||
                track.title.contains(searchQuery, ignoreCase = true) ||
                track.artist.contains(searchQuery, ignoreCase = true) ||
                track.genre.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Потоки & Радио", "Streams" -> track.source == TrackSource.STREAM
            "Подкасты", "Podcasts" -> track.source == TrackSource.PODCAST
            "Локальные", "Local" -> track.source == TrackSource.LOCAL
            else -> true
        }

        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Search Screen Header Title
        item {
            Text(
                text = "Поиск и рекомендации",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp)
            )
        }

        // Search Bar Input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Треки, артисты, жанры или подкасты...",
                        color = ImmersiveTextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = ImmersiveLavenderAccent
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = ImmersiveTextMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImmersiveLavenderAccent,
                    unfocusedBorderColor = ImmersiveCardBorder,
                    focusedContainerColor = ImmersiveSurfaceDark,
                    unfocusedContainerColor = ImmersiveSurfaceDark,
                    cursorColor = ImmersiveLavenderAccent,
                    focusedTextColor = ImmersiveTextPrimary,
                    unfocusedTextColor = ImmersiveTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_text_field")
            )
        }

        // Filter Pills
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ImmersiveLavenderAccent else ImmersivePillInactive,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else ImmersiveCardBorder),
                        modifier = Modifier.clickable { onFilterChange(filter) }
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

        // Connect Custom Stream Action Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                border = BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpenAddCustomStream() }
                    .testTag("connect_custom_stream_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ImmersiveLavenderAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Radio, contentDescription = null, tint = ImmersivePurpleDeep)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Подключить интернет-радио",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Text(
                            "Добавьте URL любого прямого аудиопотока",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Icon(Icons.Default.Add, contentDescription = null, tint = ImmersiveLavenderAccent)
                }
            }
        }

        // Show Results if searching, or Browse Categories if not searching
        if (searchQuery.isNotEmpty() || (selectedFilter != "Все" && selectedFilter != "All")) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Результаты (${filteredTracks.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(filteredTracks) { track ->
                TrackSearchItem(
                    track = track,
                    isCurrentlyPlaying = currentPlayingTrack?.id == track.id && isPlaying,
                    onClick = { onPlayTrack(track) },
                    onMenuClick = { onOpenTrackMenu(track) }
                )
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Все жанры и категории",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val rows = categories.chunked(2)
                    for (row in rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (cat in row) {
                                CategoryBrowseCard(
                                    category = cat,
                                    onClick = { onQueryChange(cat.genreFilter) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackSearchItem(
    track: Track,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit = {}
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
            TrackCoverImage(
                track = track,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

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
                    text = "${track.artist} • ${track.genre}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню трека",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Воспроизвести",
                    tint = if (isCurrentlyPlaying) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryBrowseCard(
    category: BrowseCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ImmersiveCardBorder),
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(category.color1, category.color2)
                    )
                )
                .padding(14.dp)
        ) {
            Text(
                text = category.title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}
