package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.example.data.MediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VideoWallpaperService : WallpaperService() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var repository: MediaRepository

    override fun onCreate() {
        super.onCreate()
        repository = MediaRepository(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        private var currentVideoUri: Uri? = null
        private var screenReceiver: BroadcastReceiver? = null

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            registerScreenReceiver()
            loadCurrentWallpaper()
        }

        override fun onDestroy() {
            super.onDestroy()
            unregisterScreenReceiver()
            releaseMediaPlayer()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.d("VideoWallpaper", "onVisibilityChanged: $visible")
            if (visible) {
                mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d("VideoWallpaper", "onSurfaceCreated")
            currentVideoUri?.let { uri ->
                playVideo(holder, uri)
            } ?: loadCurrentWallpaper()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d("VideoWallpaper", "onSurfaceDestroyed")
            releaseMediaPlayer()
        }

        private fun registerScreenReceiver() {
            screenReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_SCREEN_ON) {
                        Log.d("VideoWallpaper", "Screen powered on, cycling wallpaper...")
                        cycleWallpaper()
                    }
                }
            }
            registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        }

        private fun unregisterScreenReceiver() {
            screenReceiver?.let {
                try {
                    unregisterReceiver(it)
                } catch (e: Exception) {
                    Log.e("VideoWallpaper", "Failed to unregister screen receiver", e)
                }
            }
            screenReceiver = null
        }

        private fun loadCurrentWallpaper() {
            serviceScope.launch {
                val uri = repository.getCurrentWallpaperUri()
                if (uri != null) {
                    currentVideoUri = uri
                    val holder = surfaceHolder
                    if (holder != null) {
                        playVideo(holder, uri)
                    }
                } else {
                    Log.d("VideoWallpaper", "No current wallpaper URI found.")
                }
            }
        }

        private fun cycleWallpaper() {
            serviceScope.launch {
                val uri = repository.getAndCycleNextWallpaperUri()
                if (uri != null) {
                    currentVideoUri = uri
                    val holder = surfaceHolder
                    if (holder != null) {
                        playVideo(holder, uri)
                    }
                }
            }
        }

        private fun playVideo(holder: SurfaceHolder, uri: Uri) {
            releaseMediaPlayer()
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@VideoWallpaperService, uri)
                    setDisplay(holder)
                    isLooping = true
                    setVolume(0f, 0f) // Keep wallpaper silent to be non-intrusive
                    setOnPreparedListener { mp ->
                        // Start playing automatically once ready
                        mp.start()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("VideoWallpaper", "MediaPlayer Error: what=$what, extra=$extra")
                        // Return true to signal that error was handled and prevent crash
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("VideoWallpaper", "Failed to initialize MediaPlayer for URI: $uri", e)
            }
        }

        private fun releaseMediaPlayer() {
            mediaPlayer?.apply {
                try {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                } catch (e: Exception) {
                    Log.e("VideoWallpaper", "Error releasing MediaPlayer", e)
                }
            }
            mediaPlayer = null
        }
    }
}
