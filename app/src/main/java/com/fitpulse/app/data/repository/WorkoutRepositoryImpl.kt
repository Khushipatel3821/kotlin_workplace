package com.fitpulse.app.data.repository

import com.fitpulse.app.data.local.dao.WorkoutDao
import com.fitpulse.app.data.local.entity.ExerciseEntity
import com.fitpulse.app.data.local.entity.RoutineExerciseCrossRef
import com.fitpulse.app.data.local.entity.WorkoutRoutineEntity
import com.fitpulse.app.data.local.entity.WorkoutSessionEntity
import com.fitpulse.app.data.local.entity.WorkoutSetEntity
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.WorkoutRoutine
import com.fitpulse.app.domain.model.WorkoutSession
import com.fitpulse.app.domain.model.WorkoutSet
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.usecase.EstimateCaloricBurnUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val estimateCaloricBurnUseCase: EstimateCaloricBurnUseCase
) : WorkoutRepository {

    override fun getAllExercises(): Flow<List<Exercise>> {
        return workoutDao.getAllExercises().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExerciseById(id: Long): Flow<Exercise?> {
        return workoutDao.getExerciseById(id).map { it?.toDomain() }
    }

    override suspend fun insertExercise(exercise: Exercise): Long {
        return workoutDao.insertExercise(ExerciseEntity.fromDomain(exercise))
    }

    override suspend fun insertExercises(exercises: List<Exercise>) {
        workoutDao.insertExercises(exercises.map { ExerciseEntity.fromDomain(it) })
    }

    override fun getAllRoutines(): Flow<List<WorkoutRoutine>> {
        return workoutDao.getAllRoutinesWithExercises().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getRoutineById(id: Long): Flow<WorkoutRoutine?> {
        return workoutDao.getRoutineWithExercisesById(routineId = id).map { it?.toDomain() }
    }

    override suspend fun insertRoutine(routine: WorkoutRoutine): Long {
        val routineEntity = WorkoutRoutineEntity(
            id = routine.id,
            name = routine.name,
            description = routine.description,
            targetMuscles = routine.targetMuscles,
            estimatedDurationMinutes = routine.estimatedDurationMinutes,
            createdAtTimestamp = routine.createdAtTimestamp
        )
        val routineId = workoutDao.insertRoutine(routineEntity)

        // Insert cross references
        val crossRefs = routine.exercises.mapIndexed { index, exercise ->
            RoutineExerciseCrossRef(
                routineId = routineId,
                exerciseId = exercise.id,
                orderIndex = index
            )
        }
        workoutDao.insertRoutineCrossRefs(crossRefs)
        return routineId
    }

    override suspend fun deleteRoutine(routineId: Long) {
        workoutDao.deleteRoutineCrossRefs(routineId)
        workoutDao.deleteRoutine(routineId)
    }

    override fun getAllSessions(): Flow<List<WorkoutSession>> {
        return workoutDao.getAllSessionsWithSets().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSessionById(sessionId: Long): Flow<WorkoutSession?> {
        return workoutDao.getSessionWithSetsById(sessionId).map { it?.toDomain() }
    }

    override suspend fun startWorkoutSession(title: String, routineId: Long?): Long {
        val session = WorkoutSessionEntity(
            id = 0,
            routineId = routineId,
            title = title,
            startedAtTimestamp = System.currentTimeMillis(),
            endedAtTimestamp = null,
            durationSeconds = 0,
            totalVolumeKg = 0f,
            totalReps = 0,
            estimatedCaloriesBurned = 0,
            notes = ""
        )
        return workoutDao.insertSession(session)
    }

    override suspend fun finishWorkoutSession(sessionId: Long, durationSeconds: Long, notes: String) {
        val sessionWithSets = workoutDao.getSessionWithSetsById(sessionId).first() ?: return
        val sets = sessionWithSets.sets.filter { it.isCompleted }

        val totalVolume = sets.sumOf { (it.weightKg * it.completedReps).toDouble() }.toFloat()
        val totalReps = sets.sumOf { it.completedReps }
        val avgRpe = if (sets.isNotEmpty()) {
            sets.mapNotNull { it.rpe }.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 7.0f
        } else 7.0f

        val estimatedCalories = estimateCaloricBurnUseCase(
            durationSeconds = durationSeconds,
            averageRpe = avgRpe
        )

        val updatedEntity = sessionWithSets.session.copy(
            endedAtTimestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            totalVolumeKg = totalVolume,
            totalReps = totalReps,
            estimatedCaloriesBurned = estimatedCalories,
            notes = notes
        )
        workoutDao.updateSession(updatedEntity)
    }

    override suspend fun deleteSession(sessionId: Long) {
        workoutDao.deleteSession(sessionId)
    }

    override fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>> {
        return workoutDao.getSetsForSession(sessionId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertSet(set: WorkoutSet): Long {
        return workoutDao.insertSet(WorkoutSetEntity.fromDomain(set))
    }

    override suspend fun updateSet(set: WorkoutSet) {
        workoutDao.updateSet(WorkoutSetEntity.fromDomain(set))
    }

    override suspend fun deleteSet(setId: Long) {
        workoutDao.deleteSet(setId)
    }

    override suspend fun getRecentSetsForExercise(exerciseId: Long, limit: Int): List<WorkoutSet> {
        return workoutDao.getRecentCompletedSetsForExercise(exerciseId, limit).map { it.toDomain() }
    }
}
