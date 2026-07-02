package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Wallpaper Queries
    @Query("SELECT * FROM wallpaper_videos ORDER BY addedTimestamp DESC")
    fun getAllWallpapersFlow(): Flow<List<WallpaperVideo>>

    @Query("SELECT * FROM wallpaper_videos WHERE isActive = 1 ORDER BY id ASC")
    suspend fun getActiveWallpapers(): List<WallpaperVideo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: WallpaperVideo)

    @Query("DELETE FROM wallpaper_videos WHERE id = :id")
    suspend fun deleteWallpaperById(id: Int)

    @Query("UPDATE wallpaper_videos SET isActive = :isActive WHERE id = :id")
    suspend fun updateWallpaperActiveStatus(id: Int, isActive: Boolean)


    // Notification Sound Queries
    @Query("SELECT * FROM notification_sounds ORDER BY addedTimestamp DESC")
    fun getAllSoundsFlow(): Flow<List<NotificationSound>>

    @Query("SELECT * FROM notification_sounds WHERE isActive = 1 ORDER BY id ASC")
    suspend fun getActiveSounds(): List<NotificationSound>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: NotificationSound)

    @Query("DELETE FROM notification_sounds WHERE id = :id")
    suspend fun deleteSoundById(id: Int)

    @Query("UPDATE notification_sounds SET isActive = :isActive WHERE id = :id")
    suspend fun updateSoundActiveStatus(id: Int, isActive: Boolean)
}
