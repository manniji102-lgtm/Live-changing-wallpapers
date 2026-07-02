package com.example.ui

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaRepository
import com.example.data.NotificationSound
import com.example.data.WallpaperVideo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)

    val wallpapers: StateFlow<List<WallpaperVideo>> = repository.allWallpapers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sounds: StateFlow<List<NotificationSound>> = repository.allSounds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addWallpaper(uri: Uri) {
        viewModelScope.launch {
            try {
                // Query file name
                val displayName = getFileNameFromUri(uri) ?: "Video Wallpaper"
                repository.addWallpaper(uri, displayName)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to add wallpaper", e)
            }
        }
    }

    fun toggleWallpaperActive(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleWallpaperActive(id, isActive)
        }
    }

    fun deleteWallpaper(id: Int) {
        viewModelScope.launch {
            repository.deleteWallpaper(id)
        }
    }

    fun addSound(uri: Uri) {
        viewModelScope.launch {
            try {
                // Query file name
                val displayName = getFileNameFromUri(uri) ?: "Notification Sound"
                repository.addSound(uri, displayName)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to add sound", e)
            }
        }
    }

    fun toggleSoundActive(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleSoundActive(id, isActive)
        }
    }

    fun deleteSound(id: Int) {
        viewModelScope.launch {
            repository.deleteSound(id)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        val context = getApplication<Application>().applicationContext
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = cursor.getString(index)
                }
            }
        }
        return name
    }
}
