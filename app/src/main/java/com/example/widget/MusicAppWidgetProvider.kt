package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.MusicApplication
import com.example.R
import com.example.model.Track
import com.example.service.AudioPlayerService

class MusicAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val playerManager = MusicApplication.instance.playerManager
        val track = playerManager.currentTrack.value
        val isPlaying = playerManager.isPlaying.value

        for (appWidgetId in appWidgetIds) {
            val views = buildRemoteViews(context, track, isPlaying)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val playerManager = MusicApplication.instance.playerManager
        when (intent.action) {
            AudioPlayerService.ACTION_TOGGLE_PLAY -> playerManager.togglePlayPause()
            AudioPlayerService.ACTION_NEXT -> playerManager.skipToNext()
            AudioPlayerService.ACTION_PREV -> playerManager.skipToPrevious()
        }
        updateWidgets(context, playerManager.currentTrack.value, playerManager.isPlaying.value)
    }

    companion object {
        fun updateWidgets(context: Context, track: Track?, isPlaying: Boolean) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                val views = buildRemoteViews(context, track, isPlaying)
                appWidgetManager.updateAppWidget(appWidgetIds, views)
            }
        }

        private fun buildRemoteViews(context: Context, track: Track?, isPlaying: Boolean): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_music_player)

            if (track != null) {
                views.setTextViewText(R.id.widget_title, track.title)
                views.setTextViewText(R.id.widget_artist, track.artist)
                views.setTextViewText(R.id.widget_status, if (isPlaying) "● Now Playing" else "❚❚ Paused")
                track.coverDrawableRes?.let {
                    views.setImageViewResource(R.id.widget_album_art, it)
                }
            } else {
                views.setTextViewText(R.id.widget_title, "Spotify Music")
                views.setTextViewText(R.id.widget_artist, "Tap to start playback")
                views.setTextViewText(R.id.widget_status, "Ready to stream")
                views.setImageViewResource(R.id.widget_album_art, R.drawable.cover_cyberpunk_1787235201442)
            }

            val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playIcon)

            // Click pending intents
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPending = PendingIntent.getActivity(
                context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPending)

            val toggleIntent = Intent(context, MusicAppWidgetProvider::class.java).apply {
                action = AudioPlayerService.ACTION_TOGGLE_PLAY
            }
            val togglePending = PendingIntent.getBroadcast(
                context, 1, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, togglePending)

            val prevIntent = Intent(context, MusicAppWidgetProvider::class.java).apply {
                action = AudioPlayerService.ACTION_PREV
            }
            val prevPending = PendingIntent.getBroadcast(
                context, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPending)

            val nextIntent = Intent(context, MusicAppWidgetProvider::class.java).apply {
                action = AudioPlayerService.ACTION_NEXT
            }
            val nextPending = PendingIntent.getBroadcast(
                context, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

            return views
        }
    }
}
