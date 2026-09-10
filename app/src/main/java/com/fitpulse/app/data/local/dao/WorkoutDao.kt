package com.fitpulse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fitpulse.app.data.local.entity.ExerciseEntity
import com.fitpulse.app.data.local.entity.RoutineExerciseCrossRef
import com.fitpulse.app.data.local.entity.WorkoutRoutineEntity
import com.fitpulse.app.data.local.entity.WorkoutSessionEntity
import com.fitpulse.app.data.local.entity.WorkoutSetEntity
import com.fitpulse.app.data.local.relation.RoutineWithExercises
import com.fitpulse.app.data.local.relation.WorkoutSessionWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // --- Exercises ---
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun getExerciseById(id: Long): Flow<ExerciseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    // --- Routines ---
    @Transaction
    @Query("SELECT * FROM workout_routines ORDER BY createdAtTimestamp DESC")
    fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_routines WHERE id = :routineId")
    fun getRoutineWithExercisesById(routineId: Long): Flow<RoutineWithExercises?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineCrossRefs(crossRefs: List<RoutineExerciseCrossRef>)

    @Query("DELETE FROM workout_routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)

    @Query("DELETE FROM routine_exercise_cross_ref WHERE routineId = :routineId")
    suspend fun deleteRoutineCrossRefs(routineId: Long)

    // --- Sessions ---
    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY startedAtTimestamp DESC")
    fun getAllSessionsWithSets(): Flow<List<WorkoutSessionWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionWithSetsById(sessionId: Long): Flow<WorkoutSessionWithSets?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    // --- Sets ---
    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteSet(setId: Long)

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND isCompleted = 1 ORDER BY completedAtTimestamp DESC LIMIT :limit")
    suspend fun getRecentCompletedSetsForExercise(exerciseId: Long, limit: Int): List<WorkoutSetEntity>
}
