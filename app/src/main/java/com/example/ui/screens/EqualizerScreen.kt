package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqPreset
import com.example.model.EqualizerState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersiveLavenderAccent
import com.example.ui.theme.ImmersiveLavenderLight
import com.example.ui.theme.ImmersivePillInactive
import com.example.ui.theme.ImmersivePurpleDeep
import com.example.ui.theme.ImmersiveSurfaceDark

@Composable
fun EqualizerScreen(
    state: EqualizerState,
    onToggleEnable: (Boolean) -> Unit,
    onSelectPreset: (EqPreset) -> Unit,
    onUpdateBands: (Float, Float, Float, Float, Float) -> Unit,
    onUpdateBassBoost: (Float) -> Unit,
    onUpdateVirtualizer: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = EqPreset.values().toList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("equalizer_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Screen Header & Master Switch
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Эквалайзер",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = if (state.isEnabled) "Аудиодвижок активен" else "Эквалайзер отключен",
                        fontSize = 12.sp,
                        color = if (state.isEnabled) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = onToggleEnable,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ImmersiveLavenderAccent,
                        checkedTrackColor = ImmersiveLavenderAccent.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("eq_master_switch")
                )
            }
        }

        // Preset Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = state.preset == preset
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ImmersiveLavenderAccent else ImmersivePillInactive,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else ImmersiveCardBorder),
                        modifier = Modifier.clickable { onSelectPreset(preset) }
                    ) {
                        Text(
                            text = preset.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) ImmersivePurpleDeep else ImmersiveLavenderLight,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Live Frequency Curve Canvas
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                border = BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = ImmersiveLavenderAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Частотный спектр", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = state.preset.displayName,
                            color = ImmersiveLavenderAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val midY = height / 2

                        // Draw reference center grid line
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(0f, midY),
                            end = Offset(width, midY),
                            strokeWidth = 1.dp.toPx()
                        )

                        val bands = listOf(
                            state.band60Hz,
                            state.band230Hz,
                            state.band910Hz,
                            state.band3600Hz,
                            state.band14000Hz
                        )

                        val path = Path()
                        val stepX = width / (bands.size - 1)

                        for (i in bands.indices) {
                            val gain = bands[i] // -10..+10 dB
                            val normalizedY = midY - (gain / 10f) * (midY * 0.8f)
                            val x = i * stepX

                            if (i == 0) {
                                path.moveTo(x, normalizedY)
                            } else {
                                val prevX = (i - 1) * stepX
                                val prevY = midY - (bands[i - 1] / 10f) * (midY * 0.8f)
                                val controlX = (prevX + x) / 2
                                path.cubicTo(controlX, prevY, controlX, normalizedY, x, normalizedY)
                            }
                        }

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(listOf(ImmersiveLavenderAccent, AccentCyan, AccentPurple)),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        // 5-Band Graphic Equalizer Sliders
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                border = BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5-полосный графический эквалайзер",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    BandSliderRow(
                        frequencyLabel = "60 Гц (Низкий бас)",
                        currentDb = state.band60Hz,
                        enabled = state.isEnabled,
                        onValueChange = {
                            onUpdateBands(it, state.band230Hz, state.band910Hz, state.band3600Hz, state.band14000Hz)
                        }
                    )

                    BandSliderRow(
                        frequencyLabel = "230 Гц (Бас)",
                        currentDb = state.band230Hz,
                        enabled = state.isEnabled,
                        onValueChange = {
                            onUpdateBands(state.band60Hz, it, state.band910Hz, state.band3600Hz, state.band14000Hz)
                        }
                    )

                    BandSliderRow(
                        frequencyLabel = "910 Гц (Средние частоты)",
                        currentDb = state.band910Hz,
                        enabled = state.isEnabled,
                        onValueChange = {
                            onUpdateBands(state.band60Hz, state.band230Hz, it, state.band3600Hz, state.band14000Hz)
                        }
                    )

                    BandSliderRow(
                        frequencyLabel = "3.6 кГц (Высокие средние)",
                        currentDb = state.band3600Hz,
                        enabled = state.isEnabled,
                        onValueChange = {
                            onUpdateBands(state.band60Hz, state.band230Hz, state.band910Hz, it, state.band14000Hz)
                        }
                    )

                    BandSliderRow(
                        frequencyLabel = "14 кГц (Высокие частоты)",
                        currentDb = state.band14000Hz,
                        enabled = state.isEnabled,
                        onValueChange = {
                            onUpdateBands(state.band60Hz, state.band230Hz, state.band910Hz, state.band3600Hz, it)
                        }
                    )
                }
            }
        }

        // Bass Boost & 3D Spatial Virtualizer Effects
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceDark),
                border = BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Звуковые эффекты",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bass Boost Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speaker, contentDescription = null, tint = ImmersiveLavenderAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Усиление баса (Bass Boost)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("${(state.bassBoostStrength * 100).toInt()}%", fontWeight = FontWeight.Bold, color = ImmersiveLavenderAccent)
                    }

                    Slider(
                        value = state.bassBoostStrength,
                        onValueChange = onUpdateBassBoost,
                        valueRange = 0f..1f,
                        enabled = state.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersiveLavenderAccent,
                            activeTrackColor = ImmersiveLavenderAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("bass_boost_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3D Virtualizer Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SpatialAudio, contentDescription = null, tint = AccentCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("3D-звучание (Virtualizer)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("${(state.virtualizerStrength * 100).toInt()}%", fontWeight = FontWeight.Bold, color = AccentCyan)
                    }

                    Slider(
                        value = state.virtualizerStrength,
                        onValueChange = onUpdateVirtualizer,
                        valueRange = 0f..1f,
                        enabled = state.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("virtualizer_slider")
                    )
                }
            }
        }
    }
}

@Composable
private fun BandSliderRow(
    frequencyLabel: String,
    currentDb: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = frequencyLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (currentDb > 0) "+%.1f dB".format(currentDb) else "%.1f dB".format(currentDb),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentDb > 0) ImmersiveLavenderAccent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Slider(
            value = currentDb,
            onValueChange = onValueChange,
            valueRange = -10f..10f,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = ImmersiveLavenderAccent,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}
