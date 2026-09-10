package com.fitpulse.app.data.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fitpulse.app.FitPulseApplication
import com.fitpulse.app.domain.repository.DailyMetricRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dailyMetricRepository: DailyMetricRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayEpoch = LocalDate.now().toEpochDay()
            val todayMetric = dailyMetricRepository.getDailyMetricForDay(todayEpoch).first()
            val streak = dailyMetricRepository.getStreakInfo().first()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val summaryTitle = if (todayMetric?.isGoalMet == true) {
                "🔥 Daily Goal Met! Current Streak: ${streak.currentStreakDays} Days"
            } else {
                "💪 Keep the Momentum! Streak: ${streak.currentStreakDays} Days"
            }

            val summaryBody = "Today: ${todayMetric?.steps ?: 0} steps, ${todayMetric?.activeMinutes ?: 0} active mins, ${todayMetric?.completedWorkoutsCount ?: 0} workouts."

            val notification = NotificationCompat.Builder(context, FitPulseApplication.REST_ALERT_CHANNEL_ID)
                .setContentTitle(summaryTitle)
                .setContentText(summaryBody)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(SUMMARY_NOTIFICATION_ID, notification)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val SUMMARY_NOTIFICATION_ID = 2001
        const val WORK_NAME = "fitpulse_daily_summary_worker"
    }
}
