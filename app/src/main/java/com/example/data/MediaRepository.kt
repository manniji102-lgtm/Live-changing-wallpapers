package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow

class MediaRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val appDao = database.appDao()

    private val prefs = context.getSharedPreferences("media_prefs", Context.MODE_PRIVATE)

    val allWallpapers: Flow<List<WallpaperVideo>> = appDao.getAllWallpapersFlow()
    val allSounds: Flow<List<NotificationSound>> = appDao.getAllSoundsFlow()

    // Add a wallpaper
    suspend fun addWallpaper(uri: Uri, displayName: String) {
        val wallpaper = WallpaperVideo(
            uriString = uri.toString(),
            displayName = displayName
        )
        appDao.insertWallpaper(wallpaper)
    }

    // Toggle wallpaper active status
    suspend fun toggleWallpaperActive(id: Int, isActive: Boolean) {
        appDao.updateWallpaperActiveStatus(id, isActive)
    }

    // Delete a wallpaper
    suspend fun deleteWallpaper(id: Int) {
        appDao.deleteWallpaperById(id)
    }

    // Add a sound
    suspend fun addSound(uri: Uri, displayName: String) {
        val sound = NotificationSound(
            uriString = uri.toString(),
            displayName = displayName
        )
        appDao.insertSound(sound)
    }

    // Toggle sound active status
    suspend fun toggleSoundActive(id: Int, isActive: Boolean) {
        appDao.updateSoundActiveStatus(id, isActive)
    }

    // Delete a sound
    suspend fun deleteSound(id: Int) {
        appDao.deleteSoundById(id)
    }

    // Get and cycle next active wallpaper Uri
    suspend fun getAndCycleNextWallpaperUri(): Uri? {
        val activeWallpapers = appDao.getActiveWallpapers()
        if (activeWallpapers.isEmpty()) {
            Log.d("MediaRepository", "No active wallpapers configured.")
            return null
        }

        val lastIndex = prefs.getInt(KEY_WALLPAPER_INDEX, -1)
        val nextIndex = if (activeWallpapers.size <= 1) {
            0
        } else {
            (lastIndex + 1) % activeWallpapers.size
        }

        prefs.edit().putInt(KEY_WALLPAPER_INDEX, nextIndex).apply()
        val nextWallpaper = activeWallpapers[nextIndex]
        Log.d("MediaRepository", "Cycling to wallpaper at index $nextIndex: ${nextWallpaper.displayName}")
        return Uri.parse(nextWallpaper.uriString)
    }

    // Get the current wallpaper Uri (without cycling, e.g. for initial setup)
    suspend fun getCurrentWallpaperUri(): Uri? {
        val activeWallpapers = appDao.getActiveWallpapers()
        if (activeWallpapers.isEmpty()) return null

        var currentIndex = prefs.getInt(KEY_WALLPAPER_INDEX, 0)
        if (currentIndex >= activeWallpapers.size || currentIndex < 0) {
            currentIndex = 0
            prefs.edit().putInt(KEY_WALLPAPER_INDEX, 0).apply()
        }
        return Uri.parse(activeWallpapers[currentIndex].uriString)
    }

    // Get and cycle next active sound Uri
    suspend fun getAndCycleNextSoundUri(): Uri? {
        val activeSounds = appDao.getActiveSounds()
        if (activeSounds.isEmpty()) {
            Log.d("MediaRepository", "No active notification sounds configured.")
            return null
        }

        val lastIndex = prefs.getInt(KEY_SOUND_INDEX, -1)
        val nextIndex = if (activeSounds.size <= 1) {
            0
        } else {
            (lastIndex + 1) % activeSounds.size
        }

        prefs.edit().putInt(KEY_SOUND_INDEX, nextIndex).apply()
        val nextSound = activeSounds[nextIndex]
        Log.d("MediaRepository", "Cycling to sound at index $nextIndex: ${nextSound.displayName}")
        return Uri.parse(nextSound.uriString)
    }

    companion object {
        private const val KEY_WALLPAPER_INDEX = "current_wallpaper_index"
        private const val KEY_SOUND_INDEX = "current_sound_index"
    }
}
