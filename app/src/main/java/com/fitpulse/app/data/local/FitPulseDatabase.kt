package com.fitpulse.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fitpulse.app.data.local.dao.DailyMetricDao
import com.fitpulse.app.data.local.dao.WorkoutDao
import com.fitpulse.app.data.local.dao.YogaDao
import com.fitpulse.app.data.local.entity.DailyMetricLogEntity
import com.fitpulse.app.data.local.entity.ExerciseEntity
import com.fitpulse.app.data.local.entity.RoutineExerciseCrossRef
import com.fitpulse.app.data.local.entity.WorkoutRoutineEntity
import com.fitpulse.app.data.local.entity.WorkoutSessionEntity
import com.fitpulse.app.data.local.entity.WorkoutSetEntity
import com.fitpulse.app.data.local.entity.YogaSessionEntity
import com.fitpulse.app.domain.model.ExerciseCategory
import com.fitpulse.app.domain.model.MuscleGroup

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutRoutineEntity::class,
        RoutineExerciseCrossRef::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class,
        YogaSessionEntity::class,
        DailyMetricLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(FitPulseTypeConverters::class)
abstract class FitPulseDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun yogaDao(): YogaDao
    abstract fun dailyMetricDao(): DailyMetricDao

    companion object {
        const val DATABASE_NAME = "fitpulse_offline.db"

        val INITIAL_EXERCISES = listOf(
            ExerciseEntity(
                id = 1,
                name = "Barbell Bench Press",
                primaryMuscle = MuscleGroup.CHEST,
                secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                category = ExerciseCategory.BARBELL,
                instructions = "Lie on bench, grip bar slightly wider than shoulder width. Lower bar to mid-chest with control, then press upward explosively.",
                defaultRestSeconds = 120,
                isCustom = false
            ),
            ExerciseEntity(
                id = 2,
                name = "Barbell Back Squat",
                primaryMuscle = MuscleGroup.LEGS,
                secondaryMuscles = listOf(MuscleGroup.CORE),
                category = ExerciseCategory.BARBELL,
                instructions = "Bar rested on upper traps. Descend by breaking at hips and knees until thighs are parallel with floor. Drive through mid-foot.",
                defaultRestSeconds = 150,
                isCustom = false
            ),
            ExerciseEntity(
                id = 3,
                name = "Conventional Deadlift",
                primaryMuscle = MuscleGroup.BACK,
                secondaryMuscles = listOf(MuscleGroup.LEGS, MuscleGroup.CORE),
                category = ExerciseCategory.BARBELL,
                instructions = "Stand with bar over mid-foot. Hinge at hips, grip bar. Push the floor away and lock out hips with neutral spine.",
                defaultRestSeconds = 180,
                isCustom = false
            ),
            ExerciseEntity(
                id = 4,
                name = "Overhead Barbell Press",
                primaryMuscle = MuscleGroup.SHOULDERS,
                secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CORE),
                category = ExerciseCategory.BARBELL,
                instructions = "Press bar vertically from anterior deltoids overhead until elbows lockout. Keep glutes and core braced.",
                defaultRestSeconds = 120,
                isCustom = false
            ),
            ExerciseEntity(
                id = 5,
                name = "Barbell Bent-Over Row",
                primaryMuscle = MuscleGroup.BACK,
                secondaryMuscles = listOf(MuscleGroup.BICEPS),
                category = ExerciseCategory.BARBELL,
                instructions = "Hinge forward at hips at 45 degree angle. Pull bar toward lower rib cage, driving elbows back.",
                defaultRestSeconds = 90,
                isCustom = false
            ),
            ExerciseEntity(
                id = 6,
                name = "Incline Dumbbell Press",
                primaryMuscle = MuscleGroup.CHEST,
                secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
                category = ExerciseCategory.DUMBBELL,
                instructions = "Set bench to 30 degrees. Press dumbbells upward with controlled eccentric tempo.",
                defaultRestSeconds = 90,
                isCustom = false
            ),
            ExerciseEntity(
                id = 7,
                name = "Bodyweight Pull-Up",
                primaryMuscle = MuscleGroup.BACK,
                secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.CORE),
                category = ExerciseCategory.BODYWEIGHT,
                instructions = "Overhand grip slightly wider than shoulders. Pull chin over bar by depressing scapula.",
                defaultRestSeconds = 90,
                isCustom = false
            ),
            ExerciseEntity(
                id = 8,
                name = "Standing Dumbbell Bicep Curl",
                primaryMuscle = MuscleGroup.BICEPS,
                secondaryMuscles = emptyList(),
                category = ExerciseCategory.DUMBBELL,
                instructions = "Keep elbows tucked to torso. Supinate wrists as you curl dumbbells to shoulder level.",
                defaultRestSeconds = 60,
                isCustom = false
            ),
            ExerciseEntity(
                id = 9,
                name = "Tricep Rope Pushdown",
                primaryMuscle = MuscleGroup.TRICEPS,
                secondaryMuscles = emptyList(),
                category = ExerciseCategory.CABLE,
                instructions = "Extend arms downward from cable pulley, spreading rope at full contraction.",
                defaultRestSeconds = 60,
                isCustom = false
            ),
            ExerciseEntity(
                id = 10,
                name = "Romanian Deadlift (RDL)",
                primaryMuscle = MuscleGroup.LEGS,
                secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.CORE),
                category = ExerciseCategory.BARBELL,
                instructions = "Slight knee bend. Push hips backward while keeping bar close to shins until deep hamstring stretch.",
                defaultRestSeconds = 120,
                isCustom = false
            )
        )
    }
}
