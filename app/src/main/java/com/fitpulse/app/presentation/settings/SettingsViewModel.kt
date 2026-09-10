package com.fitpulse.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.data.datastore.UserPreferences
import com.fitpulse.app.data.datastore.UserPreferencesDataStore
import com.fitpulse.app.data.datastore.WeightUnit
import com.fitpulse.app.domain.usecase.ExportUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val exportJsonResult: String? = null,
    val exportCsvResult: String? = null,
    val isImportSuccessful: Boolean? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore,
    private val exportUserDataUseCase: ExportUserDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesDataStore.userPreferencesFlow.collect { prefs ->
                _state.value = _state.value.copy(preferences = prefs)
            }
        }
    }

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            preferencesDataStore.updateWeightUnit(unit)
        }
    }

    fun setDefaultRestSeconds(seconds: Int) {
        viewModelScope.launch {
            preferencesDataStore.updateDefaultRestSeconds(seconds)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateHapticFeedback(enabled)
        }
    }

    fun setSoundAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateSoundAlerts(enabled)
        }
    }

    fun exportDataJson() {
        viewModelScope.launch {
            val json = exportUserDataUseCase.exportJson()
            _state.value = _state.value.copy(
                exportJsonResult = json,
                message = "Export complete: ${json.length} characters generated."
            )
        }
    }

    fun exportHistoryCsv() {
        viewModelScope.launch {
            val csv = exportUserDataUseCase.exportWorkoutHistoryCsv()
            _state.value = _state.value.copy(
                exportCsvResult = csv,
                message = "CSV export complete."
            )
        }
    }

    fun importDataJson(jsonString: String) {
        viewModelScope.launch {
            val success = exportUserDataUseCase.importJson(jsonString)
            _state.value = _state.value.copy(
                isImportSuccessful = success,
                message = if (success) "Data imported successfully!" else "Failed to parse import JSON."
            )
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
