package com.fitpulse.app.domain.repository

import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.WorkoutRoutine
import com.fitpulse.app.domain.model.WorkoutSession
import com.fitpulse.app.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getExerciseById(id: Long): Flow<Exercise?>
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun insertExercises(exercises: List<Exercise>)

    fun getAllRoutines(): Flow<List<WorkoutRoutine>>
    fun getRoutineById(id: Long): Flow<WorkoutRoutine?>
    suspend fun insertRoutine(routine: WorkoutRoutine): Long
    suspend fun deleteRoutine(routineId: Long)

    fun getAllSessions(): Flow<List<WorkoutSession>>
    fun getSessionById(sessionId: Long): Flow<WorkoutSession?>
    suspend fun startWorkoutSession(title: String, routineId: Long?): Long
    suspend fun finishWorkoutSession(sessionId: Long, durationSeconds: Long, notes: String)
    suspend fun deleteSession(sessionId: Long)

    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>>
    suspend fun insertSet(set: WorkoutSet): Long
    suspend fun updateSet(set: WorkoutSet)
    suspend fun deleteSet(setId: Long)
    suspend fun getRecentSetsForExercise(exerciseId: Long, limit: Int = 10): List<WorkoutSet>
}
