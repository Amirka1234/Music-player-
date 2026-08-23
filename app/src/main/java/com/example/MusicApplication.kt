package com.example

import android.app.Application
import com.example.data.local.MusicDatabase
import com.example.data.repository.MusicRepository
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicApplication : Application() {

    lateinit var database: MusicDatabase
        private set

    lateinit var repository: MusicRepository
        private set

    lateinit var playerManager: AudioPlayerManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = MusicDatabase.getDatabase(this)
        repository = MusicRepository(this, database.musicDao())
        playerManager = AudioPlayerManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.initializeDefaultPlaylistsIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        lateinit var instance: MusicApplication
            private set
    }
}
