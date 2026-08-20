package com.example.model

data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val coverRes: Int? = null,
    val coverUrl: String = "",
    val trackIds: List<String> = emptyList(),
    val isCloudSynced: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
