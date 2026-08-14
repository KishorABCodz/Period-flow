package com.periodflow.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodflow.core.ai.voice.GemmaModelManager
import com.periodflow.core.domain.repository.DataSeeder
import com.periodflow.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceModelStatus(
    val isDownloaded: Boolean = false,
    val fileSizeBytes: Long = 0L,
    val configuredUrl: String? = null,
)

data class SettingsUiState(
    val defaultCycleLength: Int = 28,
    val defaultPeriodLength: Int = 5,
    val isDarkMode: Boolean? = null,
    val heightCm: Float? = null,
    val hasPolycysticOvaries: Boolean? = null,
    val isBiometricEnabled: Boolean = false,
    val voiceModel: VoiceModelStatus = VoiceModelStatus(),
    // Dev tools state
    val hasData: Boolean = false,
    val isSeedingData: Boolean = false,
    val isClearingData: Boolean = false,
    val devToolsMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val dataSeeder: DataSeeder,
    private val gemmaModelManager: GemmaModelManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepository.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        defaultCycleLength = prefs.defaultCycleLength,
                        defaultPeriodLength = prefs.defaultPeriodLength,
                        isDarkMode = prefs.isDarkMode,
                        heightCm = prefs.heightCm,
                        hasPolycysticOvaries = prefs.hasPolycysticOvaries,
                        isBiometricEnabled = prefs.isBiometricEnabled,
                    )
                }
            }
        }
        checkHasData()
        refreshVoiceModelStatus()
    }

    fun refreshVoiceModelStatus() {
        val file = gemmaModelManager.modelFile
        _uiState.update {
            it.copy(
                voiceModel = VoiceModelStatus(
                    isDownloaded = gemmaModelManager.isModelPresent(),
                    fileSizeBytes = if (file.exists()) file.length() else 0L,
                    configuredUrl = gemmaModelManager.configuredUrl,
                )
            )
        }
    }

    fun removeVoiceModel() {
        viewModelScope.launch {
            gemmaModelManager.deleteModel()
            refreshVoiceModelStatus()
        }
    }

    fun onCycleLengthChanged(days: Int) {
        viewModelScope.launch {
            prefsRepository.setDefaultCycleLength(days)
        }
    }

    fun onPeriodLengthChanged(days: Int) {
        viewModelScope.launch {
            prefsRepository.setDefaultPeriodLength(days)
        }
    }

    fun onDarkModeChanged(isDark: Boolean?) {
        viewModelScope.launch {
            prefsRepository.setDarkMode(isDark)
        }
    }

    fun onHeightChanged(height: Float?) {
        viewModelScope.launch {
            prefsRepository.setHeightCm(height)
        }
    }

    fun onPcosStatusChanged(hasPcos: Boolean?) {
        viewModelScope.launch {
            prefsRepository.setHasPolycysticOvaries(hasPcos)
        }
    }

    fun onBiometricToggle(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setBiometricEnabled(enabled)
        }
    }

    // ──── Dev Tools ────

    private fun checkHasData() {
        viewModelScope.launch {
            _uiState.update { it.copy(hasData = dataSeeder.hasData()) }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSeedingData = true, devToolsMessage = null) }
            try {
                dataSeeder.seedSampleData()
                _uiState.update {
                    it.copy(
                        isSeedingData = false,
                        hasData = true,
                        devToolsMessage = "✅ 6 cycles + daily logs seeded!",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSeedingData = false,
                        devToolsMessage = "❌ ${e.message}",
                    )
                }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingData = true, devToolsMessage = null) }
            try {
                dataSeeder.clearAllData()
                _uiState.update {
                    it.copy(
                        isClearingData = false,
                        hasData = false,
                        devToolsMessage = "🧹 All data cleared!",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isClearingData = false,
                        devToolsMessage = "❌ ${e.message}",
                    )
                }
            }
        }
    }

    fun dismissDevToolsMessage() {
        _uiState.update { it.copy(devToolsMessage = null) }
    }
}
