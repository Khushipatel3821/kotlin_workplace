package com.fitpulse.app.domain.repository

import com.fitpulse.app.domain.model.YogaPose
import com.fitpulse.app.domain.model.YogaRoutine
import com.fitpulse.app.domain.model.YogaSession
import kotlinx.coroutines.flow.Flow

interface YogaRepository {
    fun getAllPoses(): Flow<List<YogaPose>>
    fun getAllRoutines(): Flow<List<YogaRoutine>>
    fun getRoutineById(id: Long): Flow<YogaRoutine?>
    fun getAllYogaSessions(): Flow<List<YogaSession>>
    suspend fun saveYogaSession(session: YogaSession): Long
    suspend fun insertPoses(poses: List<YogaPose>)
    suspend fun insertRoutines(routines: List<YogaRoutine>)
}
