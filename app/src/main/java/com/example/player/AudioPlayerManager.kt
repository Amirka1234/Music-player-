package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import com.example.model.EqPreset
import com.example.model.EqualizerState
import com.example.model.Track
import com.example.model.TrackSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

class AudioPlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var hardwareEqualizer: Equalizer? = null
    private var hardwareBassBoost: BassBoost? = null
    private var hardwareVirtualizer: Virtualizer? = null

    private var progressJob: Job? = null
    private var sleepCountDown: CountDownTimer? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _playlist = MutableStateFlow<List<Track>>(emptyList())
    val playlist: StateFlow<List<Track>> = _playlist.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _equalizerState = MutableStateFlow(EqualizerState.getPresetValues(EqPreset.BASS_BOOST))
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    private val _sleepTimerSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerSeconds: StateFlow<Int?> = _sleepTimerSeconds.asStateFlow()

    // 16-band live visualizer amplitude heights for sleek animated equalizer bars
    private val _visualizerAmplitudes = MutableStateFlow<List<Float>>(List(16) { 0.15f })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    // Callback for service/widget notification updates
    var onTrackStateChanged: ((Track?, Boolean) -> Unit)? = null

    fun playTrack(track: Track, newPlaylist: List<Track> = emptyList()) {
        if (newPlaylist.isNotEmpty()) {
            _playlist.value = newPlaylist
        } else if (!_playlist.value.any { it.id == track.id }) {
            _playlist.value = listOf(track)
        }

        _currentTrack.value = track
        _isBuffering.value = true

        releasePlayer()

        try {
            val player = MediaPlayer()
            mediaPlayer = player

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            val uriToPlay: Uri = if (track.localUri.isNotEmpty()) {
                Uri.parse(track.localUri)
            } else if (track.streamUrl.isNotEmpty()) {
                Uri.parse(track.streamUrl)
            } else {
                Uri.parse("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
            }

            player.setDataSource(context, uriToPlay)
            player.setOnPreparedListener { mp ->
                _isBuffering.value = false
                _duration.value = mp.duration.toLong().coerceAtLeast(0L)
                attachAudioEffects(mp.audioSessionId)
                applySpeed(_playbackSpeed.value)
                mp.start()
                _isPlaying.value = true
                startProgressTracker()
                onTrackStateChanged?.invoke(track, true)
            }

            player.setOnCompletionListener {
                handleTrackCompletion()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayerManager", "MediaPlayer error: what=$what, extra=$extra")
                _isBuffering.value = false
                _isPlaying.value = false
                // fallback gracefully
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to start player", e)
            _isBuffering.value = false
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player == null) {
            val track = _currentTrack.value ?: _playlist.value.firstOrNull()
            if (track != null) {
                playTrack(track)
            }
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            onTrackStateChanged?.invoke(_currentTrack.value, false)
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracker()
            onTrackStateChanged?.invoke(_currentTrack.value, true)
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            try {
                player.seekTo(positionMs.toInt())
                _currentPosition.value = positionMs
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun skipToNext() {
        val current = _currentTrack.value ?: return
        val list = _playlist.value
        if (list.isEmpty()) return

        val currentIndex = list.indexOfFirst { it.id == current.id }
        val nextIndex = if (_isShuffle.value) {
            Random.nextInt(list.size)
        } else {
            if (currentIndex + 1 < list.size) currentIndex + 1 else 0
        }

        val nextTrack = list.getOrNull(nextIndex) ?: list.first()
        playTrack(nextTrack, list)
    }

    fun skipToPrevious() {
        // If track has played more than 3 seconds, restart current track
        if (_currentPosition.value > 3000L) {
            seekTo(0)
            return
        }

        val current = _currentTrack.value ?: return
        val list = _playlist.value
        if (list.isEmpty()) return

        val currentIndex = list.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        val prevTrack = list.getOrNull(prevIndex) ?: list.last()
        playTrack(prevTrack, list)
    }

    fun playNext(track: Track) {
        val list = _playlist.value.toMutableList()
        val current = _currentTrack.value
        if (current == null) {
            playTrack(track, listOf(track))
            return
        }
        val currentIdx = list.indexOfFirst { it.id == current.id }
        list.removeAll { it.id == track.id }
        val insertIdx = if (currentIdx != -1) (currentIdx + 1).coerceAtMost(list.size) else 0
        list.add(insertIdx, track)
        _playlist.value = list
    }

    fun addToQueue(track: Track) {
        val list = _playlist.value.toMutableList()
        if (!list.any { it.id == track.id }) {
            list.add(track)
            _playlist.value = list
        }
        if (_currentTrack.value == null) {
            playTrack(track, list)
        }
    }

    fun removeFromQueue(trackId: String) {
        val list = _playlist.value.toMutableList()
        val removed = list.removeAll { it.id == trackId }
        if (removed) {
            _playlist.value = list
            if (_currentTrack.value?.id == trackId) {
                if (list.isNotEmpty()) {
                    skipToNext()
                } else {
                    releasePlayer()
                    _currentTrack.value = null
                    _isPlaying.value = false
                }
            }
        }
    }

    fun updateTrackInQueue(updatedTrack: Track) {
        val list = _playlist.value.map { if (it.id == updatedTrack.id) updatedTrack else it }
        _playlist.value = list
        if (_currentTrack.value?.id == updatedTrack.id) {
            _currentTrack.value = updatedTrack
            onTrackStateChanged?.invoke(updatedTrack, _isPlaying.value)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applySpeed(speed)
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let { player ->
                try {
                    val params = player.playbackParams
                    params.speed = speed
                    player.playbackParams = params
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepCountDown?.cancel()
        if (minutes <= 0) {
            _sleepTimerSeconds.value = null
            return
        }

        val totalMs = minutes * 60 * 1000L
        _sleepTimerSeconds.value = minutes * 60

        sleepCountDown = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _sleepTimerSeconds.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _sleepTimerSeconds.value = null
                pause()
            }
        }.start()
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                onTrackStateChanged?.invoke(_currentTrack.value, false)
            }
        }
    }

    // Hardware & DSP Equalizer integration
    private fun attachAudioEffects(audioSessionId: Int) {
        if (audioSessionId == 0) return
        try {
            hardwareEqualizer?.release()
            hardwareBassBoost?.release()
            hardwareVirtualizer?.release()

            hardwareEqualizer = Equalizer(0, audioSessionId).apply {
                enabled = _equalizerState.value.isEnabled
            }
            hardwareBassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _equalizerState.value.isEnabled
            }
            hardwareVirtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _equalizerState.value.isEnabled
            }

            applyEqualizerSettings(_equalizerState.value)
        } catch (e: Exception) {
            Log.w("AudioPlayerManager", "AudioFX init non-fatal note: ${e.message}")
        }
    }

    fun applyEqualizerSettings(state: EqualizerState) {
        _equalizerState.value = state
        try {
            hardwareEqualizer?.let { eq ->
                eq.enabled = state.isEnabled
                if (state.isEnabled) {
                    val numBands = eq.numberOfBands.toInt()
                    val minBandLevel = eq.bandLevelRange[0]
                    val maxBandLevel = eq.bandLevelRange[1]
                    val bandLevels = listOf(state.band60Hz, state.band230Hz, state.band910Hz, state.band3600Hz, state.band14000Hz)

                    for (i in 0 until numBands) {
                        val levelGain = bandLevels.getOrElse(i) { 0f }
                        // Map -10..+10 dB to minBandLevel..maxBandLevel (typically -1500 to +1500 mB)
                        val levelMilliBels = (levelGain * 100).toInt().coerceIn(minBandLevel.toInt(), maxBandLevel.toInt()).toShort()
                        eq.setBandLevel(i.toShort(), levelMilliBels)
                    }
                }
            }

            hardwareBassBoost?.let { bb ->
                bb.enabled = state.isEnabled
                if (state.isEnabled && bb.strengthSupported) {
                    bb.setStrength((state.bassBoostStrength * 1000).toInt().coerceIn(0, 1000).toShort())
                }
            }

            hardwareVirtualizer?.let { virt ->
                virt.enabled = state.isEnabled
                if (state.isEnabled && virt.strengthSupported) {
                    virt.setStrength((state.virtualizerStrength * 1000).toInt().coerceIn(0, 1000).toShort())
                }
            }
        } catch (e: Exception) {
            Log.w("AudioPlayerManager", "Equalizer update: ${e.message}")
        }
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _isPlaying.value = true
            }
            RepeatMode.ALL -> {
                skipToNext()
            }
            RepeatMode.OFF -> {
                val list = _playlist.value
                val current = _currentTrack.value
                val currentIndex = list.indexOfFirst { it.id == current?.id }
                if (currentIndex >= 0 && currentIndex < list.size - 1) {
                    skipToNext()
                } else {
                    _isPlaying.value = false
                    _currentPosition.value = 0L
                }
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition.toLong()
                        _duration.value = player.duration.toLong().coerceAtLeast(0L)

                        // Update live dynamic amplitudes for visualizer
                        val baseLevels = listOf(
                            _equalizerState.value.band60Hz,
                            _equalizerState.value.band230Hz,
                            _equalizerState.value.band910Hz,
                            _equalizerState.value.band3600Hz,
                            _equalizerState.value.band14000Hz
                        )

                        _visualizerAmplitudes.value = List(16) { index ->
                            val base = (baseLevels[index % baseLevels.size] + 10f) / 20f
                            val jitter = (Random.nextFloat() * 0.4f)
                            (base * 0.6f + jitter).coerceIn(0.1f, 1.0f)
                        }
                    }
                }
                delay(200)
            }
        }
    }

    private fun releasePlayer() {
        progressJob?.cancel()
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }

    fun release() {
        releasePlayer()
        sleepCountDown?.cancel()
        try {
            hardwareEqualizer?.release()
            hardwareBassBoost?.release()
            hardwareVirtualizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
