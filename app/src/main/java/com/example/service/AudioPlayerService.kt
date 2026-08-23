package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.example.MainActivity
import com.example.MusicApplication
import com.example.R
import com.example.model.Track
import com.example.util.ArtworkUtils
import com.example.widget.MusicAppWidgetProvider

class AudioPlayerService : Service() {

    companion object {
        const val CHANNEL_ID = "spotify_music_playback_channel"
        const val NOTIFICATION_ID = 101

        const val ACTION_PLAY = "com.example.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.ACTION_PAUSE"
        const val ACTION_TOGGLE_PLAY = "com.example.ACTION_TOGGLE_PLAY"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREV = "com.example.ACTION_PREV"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, AudioPlayerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val playerManager = MusicApplication.instance.playerManager

        // Setup MediaSession for system media player in quick settings / lockscreen
        mediaSession = MediaSessionCompat(this, "SpotifyMusicSession").apply {
            isActive = true
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    if (!playerManager.isPlaying.value) playerManager.togglePlayPause()
                }

                override fun onPause() {
                    if (playerManager.isPlaying.value) playerManager.togglePlayPause()
                }

                override fun onSkipToNext() {
                    playerManager.skipToNext()
                }

                override fun onSkipToPrevious() {
                    playerManager.skipToPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    playerManager.seekTo(pos)
                }
            })
        }

        val initialTrack = playerManager.currentTrack.value
        val isPlaying = playerManager.isPlaying.value

        val initialNotification = if (initialTrack != null) {
            buildNotification(initialTrack, isPlaying)
        } else {
            buildIdleNotification()
        }

        startForegroundWithCompat(initialNotification)

        playerManager.onTrackStateChanged = { track, playing ->
            val notification = if (track != null) {
                buildNotification(track, playing)
            } else {
                buildIdleNotification()
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.notify(NOTIFICATION_ID, notification)

            // Also notify widget
            MusicAppWidgetProvider.updateWidgets(this, track, playing)
        }
    }

    private fun startForegroundWithCompat(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun buildIdleNotification(): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_fg_1787235167749)
            .setContentTitle("Spotify Music")
            .setContentText("Готов к воспроизведению")
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playerManager = MusicApplication.instance.playerManager
        when (intent?.action) {
            ACTION_TOGGLE_PLAY -> playerManager.togglePlayPause()
            ACTION_PLAY -> if (!playerManager.isPlaying.value) playerManager.togglePlayPause()
            ACTION_PAUSE -> if (playerManager.isPlaying.value) playerManager.togglePlayPause()
            ACTION_NEXT -> playerManager.skipToNext()
            ACTION_PREV -> playerManager.skipToPrevious()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Музыкальный плеер",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Фоновое воспроизведение и управление плеером"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(track: Track, isPlaying: Boolean): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(this, AudioPlayerService::class.java).apply { action = ACTION_PREV }
        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, AudioPlayerService::class.java).apply { action = ACTION_TOGGLE_PLAY }
        val togglePendingIntent = PendingIntent.getService(
            this,
            2,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, AudioPlayerService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Пауза" else "Воспроизвести"

        // Load album cover from metadata / URI / resource
        val artworkBitmap = ArtworkUtils.getTrackArtworkBitmap(this, track)

        val playerManager = MusicApplication.instance.playerManager

        // Update MediaSession with track metadata
        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.album.ifEmpty { "Spotify Music" })
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.durationMs)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artworkBitmap)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, artworkBitmap)
                .build()
        )

        // Update PlaybackState
        val playbackStateInt = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(playbackStateInt, playerManager.currentPosition.value, 1.0f)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO
                )
                .build()
        )

        // MediaStyle notification creates the rich Android system player in shade/lockscreen
        val mediaStyle = MediaNotificationCompat.MediaStyle()
            .setMediaSession(mediaSession?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(prevPendingIntent)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_fg_1787235167749)
            .setLargeIcon(artworkBitmap)
            .setContentTitle(track.title)
            .setContentText(track.artist)
            .setSubText(track.album.ifEmpty { "Spotify Music" })
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setStyle(mediaStyle)
            .addAction(android.R.drawable.ic_media_previous, "Назад", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Вперед", nextPendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        mediaSession = null
    }
}

