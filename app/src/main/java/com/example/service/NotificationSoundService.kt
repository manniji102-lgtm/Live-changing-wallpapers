package com.example.service

import android.app.Notification
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.MediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NotificationSoundService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: MediaRepository
    private var lastTriggerTime = 0L

    override fun onCreate() {
        super.onCreate()
        repository = MediaRepository(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Filter out notifications from our own application
        if (sbn.packageName == packageName) {
            return
        }

        // Avoid playing sound for ongoing notifications
        val isOngoing = sbn.isOngoing
        if (isOngoing) return

        val currentTime = System.currentTimeMillis()
        // Rate-limiting / debounce of 2 seconds to optimize battery and prevent audio spam
        if (currentTime - lastTriggerTime < 2000) {
            return
        }
        lastTriggerTime = currentTime

        Log.d("NotificationSoundService", "Notification received from: ${sbn.packageName}. Cycling sound...")

        serviceScope.launch {
            val uri = repository.getAndCycleNextSoundUri()
            if (uri != null) {
                // 1. Play the sound directly
                playSound(uri)

                // 2. Try to update default system sound
                updateDefaultRingtone(uri)
            }
        }
    }

    private fun playSound(uri: Uri) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(this@NotificationSoundService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener { mp ->
                    mp.release()
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("NotificationSoundService", "Error playing sound directly", e)
        }
    }

    private fun updateDefaultRingtone(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                        this,
                        RingtoneManager.TYPE_NOTIFICATION,
                        uri
                    )
                    Log.d("NotificationSoundService", "Updated system default notification sound.")
                } catch (e: Exception) {
                    Log.e("NotificationSoundService", "Failed to set default ringtone", e)
                }
            }
        } else {
            try {
                RingtoneManager.setActualDefaultRingtoneUri(
                    this,
                    RingtoneManager.TYPE_NOTIFICATION,
                    uri
                )
            } catch (e: Exception) {
                Log.e("NotificationSoundService", "Failed to set default ringtone on older Android version", e)
            }
        }
    }
}
