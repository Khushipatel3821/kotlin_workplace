package com.fitpulse.app.data.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.fitpulse.app.FitPulseApplication
import com.fitpulse.app.R
import com.fitpulse.app.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WorkoutForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var isResting = false
    private var restSecondsRemaining = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val workoutTitle = intent.getStringExtra(EXTRA_WORKOUT_TITLE) ?: "Active Workout"
                _activeWorkoutTitle.value = workoutTitle
                _isWorkoutActive.value = true
                startForegroundWithNotification(workoutTitle)
                startTimerLoop()
            }
            ACTION_START_REST -> {
                val restDuration = intent.getIntExtra(EXTRA_REST_DURATION, 90)
                startRestTimer(restDuration)
            }
            ACTION_STOP_REST -> {
                stopRestTimer()
            }
            ACTION_STOP -> {
                stopWorkoutService()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification(title: String) {
        val notification = buildNotification(title, "Workout in progress • 00:00", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && _isWorkoutActive.value) {
                delay(1000)
                _elapsedSeconds.value += 1

                if (isResting && restSecondsRemaining > 0) {
                    restSecondsRemaining -= 1
                    _restTimeRemaining.value = restSecondsRemaining
                    if (restSecondsRemaining == 0) {
                        isResting = false
                    }
                }

                updateNotification()
            }
        }
    }

    private fun startRestTimer(seconds: Int) {
        isResting = true
        restSecondsRemaining = seconds
        _restTimeRemaining.value = seconds
        updateNotification()
    }

    private fun stopRestTimer() {
        isResting = false
        restSecondsRemaining = 0
        _restTimeRemaining.value = 0
        updateNotification()
    }

    private fun updateNotification() {
        val title = _activeWorkoutTitle.value
        val formattedTime = formatTime(_elapsedSeconds.value)
        val content = if (isResting && restSecondsRemaining > 0) {
            "Resting: ${restSecondsRemaining}s remaining • Total: $formattedTime"
        } else {
            "Session in progress • $formattedTime"
        }

        val notification = buildNotification(title, content, isResting)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(title: String, content: String, isRest: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, WorkoutForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FitPulseApplication.WORKOUT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Finish", pendingStop)
            .build()
    }

    private fun stopWorkoutService() {
        _isWorkoutActive.value = false
        _elapsedSeconds.value = 0
        _restTimeRemaining.value = 0
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.fitpulse.action.START_WORKOUT"
        const val ACTION_START_REST = "com.fitpulse.action.START_REST"
        const val ACTION_STOP_REST = "com.fitpulse.action.STOP_REST"
        const val ACTION_STOP = "com.fitpulse.action.STOP_WORKOUT"

        const val EXTRA_WORKOUT_TITLE = "extra_workout_title"
        const val EXTRA_REST_DURATION = "extra_rest_duration"

        private val _isWorkoutActive = MutableStateFlow(false)
        val isWorkoutActive = _isWorkoutActive.asStateFlow()

        private val _activeWorkoutTitle = MutableStateFlow("Workout")
        val activeWorkoutTitle = _activeWorkoutTitle.asStateFlow()

        private val _elapsedSeconds = MutableStateFlow(0L)
        val elapsedSeconds = _elapsedSeconds.asStateFlow()

        private val _restTimeRemaining = MutableStateFlow(0)
        val restTimeRemaining = _restTimeRemaining.asStateFlow()

        fun startWorkout(context: Context, title: String) {
            val intent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_WORKOUT_TITLE, title)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startRest(context: Context, durationSeconds: Int) {
            val intent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = ACTION_START_REST
                putExtra(EXTRA_REST_DURATION, durationSeconds)
            }
            context.startService(intent)
        }

        fun stopRest(context: Context) {
            val intent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = ACTION_STOP_REST
            }
            context.startService(intent)
        }

        fun stopWorkout(context: Context) {
            val intent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
