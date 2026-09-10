package com.fitpulse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitpulse.app.data.local.entity.YogaSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface YogaDao {

    @Query("SELECT * FROM yoga_sessions ORDER BY startedAtTimestamp DESC")
    fun getAllYogaSessions(): Flow<List<YogaSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYogaSession(session: YogaSessionEntity): Long

    @Query("DELETE FROM yoga_sessions WHERE id = :id")
    suspend fun deleteYogaSession(id: Long)
}
