package com.fitpulse.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fitpulse_user_prefs")

enum class WeightUnit {
    KG,
    LBS
}

data class UserPreferences(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val userBodyweightKg: Float = 75f,
    val defaultRestSeconds: Int = 90,
    val autoStartRestTimer: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val soundAlertsEnabled: Boolean = true,
    val motionRepSensitivity: Float = 1.0f,
    val dailyStepGoal: Int = 10000,
    val dailyActiveMinutesGoal: Int = 45,
    val dailyCalorieGoal: Int = 500
)

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val USER_BODYWEIGHT = floatPreferencesKey("user_bodyweight")
        val DEFAULT_REST_SECONDS = intPreferencesKey("default_rest_seconds")
        val AUTO_START_REST_TIMER = booleanPreferencesKey("auto_start_rest_timer")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val SOUND_ALERTS_ENABLED = booleanPreferencesKey("sound_alerts_enabled")
        val MOTION_REP_SENSITIVITY = floatPreferencesKey("motion_rep_sensitivity")
        val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        val DAILY_ACTIVE_MIN_GOAL = intPreferencesKey("daily_active_min_goal")
        val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val weightUnitStr = preferences[PreferencesKeys.WEIGHT_UNIT] ?: WeightUnit.KG.name
        val weightUnit = try {
            WeightUnit.valueOf(weightUnitStr)
        } catch (e: Exception) {
            WeightUnit.KG
        }

        UserPreferences(
            weightUnit = weightUnit,
            userBodyweightKg = preferences[PreferencesKeys.USER_BODYWEIGHT] ?: 75f,
            defaultRestSeconds = preferences[PreferencesKeys.DEFAULT_REST_SECONDS] ?: 90,
            autoStartRestTimer = preferences[PreferencesKeys.AUTO_START_REST_TIMER] ?: true,
            hapticFeedbackEnabled = preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true,
            soundAlertsEnabled = preferences[PreferencesKeys.SOUND_ALERTS_ENABLED] ?: true,
            motionRepSensitivity = preferences[PreferencesKeys.MOTION_REP_SENSITIVITY] ?: 1.0f,
            dailyStepGoal = preferences[PreferencesKeys.DAILY_STEP_GOAL] ?: 10000,
            dailyActiveMinutesGoal = preferences[PreferencesKeys.DAILY_ACTIVE_MIN_GOAL] ?: 45,
            dailyCalorieGoal = preferences[PreferencesKeys.DAILY_CALORIE_GOAL] ?: 500
        )
    }

    suspend fun updateWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit.name
        }
    }

    suspend fun updateUserBodyweight(weightKg: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_BODYWEIGHT] = weightKg
        }
    }

    suspend fun updateDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REST_SECONDS] = seconds
        }
    }

    suspend fun updateAutoStartRestTimer(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_START_REST_TIMER] = enabled
        }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun updateSoundAlerts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun updateGoals(stepGoal: Int, activeMinutesGoal: Int, calorieGoal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_STEP_GOAL] = stepGoal
            preferences[PreferencesKeys.DAILY_ACTIVE_MIN_GOAL] = activeMinutesGoal
            preferences[PreferencesKeys.DAILY_CALORIE_GOAL] = calorieGoal
        }
    }
}
