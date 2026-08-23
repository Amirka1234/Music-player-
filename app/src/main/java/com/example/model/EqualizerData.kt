package com.example.model

enum class EqPreset(val displayName: String) {
    FLAT("Обычный"),
    BASS_BOOST("Усиление баса"),
    ROCK("Рок"),
    POP("Поп"),
    JAZZ("Джаз"),
    ELECTRONIC("Электроника"),
    CLASSICAL("Классика"),
    VOCAL("Вокал"),
    CUSTOM("Свой")
}

data class EqualizerState(
    val isEnabled: Boolean = true,
    val preset: EqPreset = EqPreset.BASS_BOOST,
    // 5 Bands in dB (-10 to +10 dB) or millibels (-1000 to +1000)
    val band60Hz: Float = 6.0f,     // Sub-bass
    val band230Hz: Float = 3.5f,    // Bass
    val band910Hz: Float = 0.0f,    // Mids
    val band3600Hz: Float = 2.0f,   // High-mids
    val band14000Hz: Float = 4.5f,  // Treble
    val bassBoostStrength: Float = 0.7f, // 0.0 to 1.0
    val virtualizerStrength: Float = 0.5f, // 0.0 to 1.0 (3D Surround)
    val loudnessGain: Float = 0.2f // 0.0 to 1.0
) {
    companion object {
        fun getPresetValues(preset: EqPreset): EqualizerState {
            return when (preset) {
                EqPreset.FLAT -> EqualizerState(preset = EqPreset.FLAT, band60Hz = 0f, band230Hz = 0f, band910Hz = 0f, band3600Hz = 0f, band14000Hz = 0f, bassBoostStrength = 0f, virtualizerStrength = 0f)
                EqPreset.BASS_BOOST -> EqualizerState(preset = EqPreset.BASS_BOOST, band60Hz = 7f, band230Hz = 5f, band910Hz = 1f, band3600Hz = 2f, band14000Hz = 3f, bassBoostStrength = 0.85f, virtualizerStrength = 0.4f)
                EqPreset.ROCK -> EqualizerState(preset = EqPreset.ROCK, band60Hz = 5f, band230Hz = 3f, band910Hz = -1f, band3600Hz = 4f, band14000Hz = 6f, bassBoostStrength = 0.6f, virtualizerStrength = 0.5f)
                EqPreset.POP -> EqualizerState(preset = EqPreset.POP, band60Hz = 2f, band230Hz = 4f, band910Hz = 5f, band3600Hz = 3f, band14000Hz = 2f, bassBoostStrength = 0.4f, virtualizerStrength = 0.3f)
                EqPreset.JAZZ -> EqualizerState(preset = EqPreset.JAZZ, band60Hz = 3f, band230Hz = 2f, band910Hz = -2f, band3600Hz = 3f, band14000Hz = 5f, bassBoostStrength = 0.3f, virtualizerStrength = 0.6f)
                EqPreset.ELECTRONIC -> EqualizerState(preset = EqPreset.ELECTRONIC, band60Hz = 6.5f, band230Hz = 4.5f, band910Hz = 0f, band3600Hz = 3.5f, band14000Hz = 5.5f, bassBoostStrength = 0.8f, virtualizerStrength = 0.7f)
                EqPreset.CLASSICAL -> EqualizerState(preset = EqPreset.CLASSICAL, band60Hz = 4f, band230Hz = 3f, band910Hz = 2f, band3600Hz = 4f, band14000Hz = 5f, bassBoostStrength = 0.2f, virtualizerStrength = 0.8f)
                EqPreset.VOCAL -> EqualizerState(preset = EqPreset.VOCAL, band60Hz = -2f, band230Hz = 1f, band910Hz = 6f, band3600Hz = 5f, band14000Hz = 2f, bassBoostStrength = 0.1f, virtualizerStrength = 0.2f)
                EqPreset.CUSTOM -> EqualizerState(preset = EqPreset.CUSTOM)
            }
        }
    }
}
