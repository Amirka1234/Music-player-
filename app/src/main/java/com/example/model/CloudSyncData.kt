package com.example.model

data class CloudSyncData(
    val accountEmail: String = "vladislavandrusinik6@gmail.com",
    val syncDeviceId: String = "Pixel 8 Pro (Sync Master)",
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val isAutoSyncEnabled: Boolean = true,
    val syncedPlaylistsCount: Int = 4,
    val syncedFavoritesCount: Int = 12,
    val cloudStatus: String = "Synchronized with Cloud"
)
