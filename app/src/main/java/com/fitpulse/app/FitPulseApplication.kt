package com.fitpulse.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FitPulseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val workoutChannel = NotificationChannel(
                WORKOUT_CHANNEL_ID,
                getString(R.string.notification_channel_workout),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_workout_desc)
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                REST_ALERT_CHANNEL_ID,
                "Rest & Routine Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when rest timer ends or next exercise begins."
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(workoutChannel, alertChannel))
        }
    }

    companion object {
        const val WORKOUT_CHANNEL_ID = "channel_fitpulse_active_workout"
        const val REST_ALERT_CHANNEL_ID = "channel_fitpulse_rest_alert"
    }
}
