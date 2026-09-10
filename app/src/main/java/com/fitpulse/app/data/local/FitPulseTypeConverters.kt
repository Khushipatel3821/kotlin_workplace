package com.fitpulse.app.data.local

import androidx.room.TypeConverter
import com.fitpulse.app.domain.model.ExerciseCategory
import com.fitpulse.app.domain.model.MuscleGroup
import com.fitpulse.app.domain.model.SetType
import com.fitpulse.app.domain.model.YogaDifficulty

class FitPulseTypeConverters {

    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup?): String? = value?.name

    @TypeConverter
    fun toMuscleGroup(value: String?): MuscleGroup? = value?.let { MuscleGroup.valueOf(it) }

    @TypeConverter
    fun fromMuscleGroupList(list: List<MuscleGroup>?): String? {
        return list?.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toMuscleGroupList(data: String?): List<MuscleGroup> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").mapNotNull { name ->
            try {
                MuscleGroup.valueOf(name)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromExerciseCategory(value: ExerciseCategory?): String? = value?.name

    @TypeConverter
    fun toExerciseCategory(value: String?): ExerciseCategory? = value?.let { ExerciseCategory.valueOf(it) }

    @TypeConverter
    fun fromSetType(value: SetType?): String? = value?.name

    @TypeConverter
    fun toSetType(value: String?): SetType? = value?.let { SetType.valueOf(it) }

    @TypeConverter
    fun fromYogaDifficulty(value: YogaDifficulty?): String? = value?.name

    @TypeConverter
    fun toYogaDifficulty(value: String?): YogaDifficulty? = value?.let { YogaDifficulty.valueOf(it) }
}
