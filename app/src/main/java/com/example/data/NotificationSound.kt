package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_sounds")
data class NotificationSound(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uriString: String,
    val displayName: String,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
