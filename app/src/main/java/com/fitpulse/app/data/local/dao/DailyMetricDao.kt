package com.fitpulse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitpulse.app.data.local.entity.DailyMetricLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMetricDao {

    @Query("SELECT * FROM daily_metric_logs WHERE dateEpochDay = :epochDay")
    fun getMetricForDay(epochDay: Long): Flow<DailyMetricLogEntity?>

    @Query("SELECT * FROM daily_metric_logs WHERE dateEpochDay = :epochDay")
    suspend fun getMetricForDaySync(epochDay: Long): DailyMetricLogEntity?

    @Query("SELECT * FROM daily_metric_logs ORDER BY dateEpochDay DESC LIMIT :limit")
    fun getRecentMetrics(limit: Int): Flow<List<DailyMetricLogEntity>>

    @Query("SELECT * FROM daily_metric_logs ORDER BY dateEpochDay DESC")
    suspend fun getAllMetricsSync(): List<DailyMetricLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetric(metric: DailyMetricLogEntity)

    @Update
    suspend fun updateMetric(metric: DailyMetricLogEntity)
}
