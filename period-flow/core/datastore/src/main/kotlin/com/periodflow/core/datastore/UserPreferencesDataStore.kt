package com.periodflow.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.periodflow.core.domain.repository.UserPreferences
import com.periodflow.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "periodflow_preferences",
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private object Keys {
        val DEFAULT_CYCLE_LENGTH = intPreferencesKey("default_cycle_length")
        val DEFAULT_PERIOD_LENGTH = intPreferencesKey("default_period_length")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val IS_DARK_MODE = stringPreferencesKey("is_dark_mode") // "true", "false", or "system"
        val LAST_PERIOD_START_EPOCH_DAY = longPreferencesKey("last_period_start_epoch_day")
        val HEIGHT_CM = floatPreferencesKey("height_cm")
        val HAS_POLYCYSTIC_OVARIES = booleanPreferencesKey("has_polycystic_ovaries")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val ACNE_SEVERITY = stringPreferencesKey("acne_severity")
        val HIRSUTISM_SEVERITY = stringPreferencesKey("hirsutism_severity")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val IS_VOICE_MODE_ENABLED = booleanPreferencesKey("is_voice_mode_enabled")
    }

    override val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                defaultCycleLength = preferences[Keys.DEFAULT_CYCLE_LENGTH] ?: 28,
                defaultPeriodLength = preferences[Keys.DEFAULT_PERIOD_LENGTH] ?: 5,
                hasCompletedOnboarding = preferences[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
                isDarkMode = when (preferences[Keys.IS_DARK_MODE]) {
                    "true" -> true
                    "false" -> false
                    else -> null
                },
                lastPeriodStartEpochDay = preferences[Keys.LAST_PERIOD_START_EPOCH_DAY],
                heightCm = preferences[Keys.HEIGHT_CM],
                hasPolycysticOvaries = preferences[Keys.HAS_POLYCYSTIC_OVARIES],
                weightKg = preferences[Keys.WEIGHT_KG],
                acneSeverity = preferences[Keys.ACNE_SEVERITY],
                hirsutismSeverity = preferences[Keys.HIRSUTISM_SEVERITY],
                isBiometricEnabled = preferences[Keys.IS_BIOMETRIC_ENABLED] ?: false,
                isVoiceModeEnabled = preferences[Keys.IS_VOICE_MODE_ENABLED] ?: false,
            )
        }

    override suspend fun setDefaultCycleLength(days: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_CYCLE_LENGTH] = days }
    }

    override suspend fun setDefaultPeriodLength(days: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_PERIOD_LENGTH] = days }
    }

    override suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = true }
    }

    override suspend fun setDarkMode(isDark: Boolean?) {
        context.dataStore.edit {
            it[Keys.IS_DARK_MODE] = when (isDark) {
                true -> "true"
                false -> "false"
                null -> "system"
            }
        }
    }

    override suspend fun setLastPeriodStart(epochDay: Long) {
        context.dataStore.edit { it[Keys.LAST_PERIOD_START_EPOCH_DAY] = epochDay }
    }

    override suspend fun setHeightCm(height: Float?) {
        context.dataStore.edit { preferences ->
            if (height != null) {
                preferences[Keys.HEIGHT_CM] = height
            } else {
                preferences.remove(Keys.HEIGHT_CM)
            }
        }
    }

    override suspend fun setHasPolycysticOvaries(hasPcos: Boolean?) {
        context.dataStore.edit { preferences ->
            if (hasPcos != null) {
                preferences[Keys.HAS_POLYCYSTIC_OVARIES] = hasPcos
            } else {
                preferences.remove(Keys.HAS_POLYCYSTIC_OVARIES)
            }
        }
    }

    override suspend fun setWeightKg(weight: Float?) {
        context.dataStore.edit { preferences ->
            if (weight != null) {
                preferences[Keys.WEIGHT_KG] = weight
            } else {
                preferences.remove(Keys.WEIGHT_KG)
            }
        }
    }

    override suspend fun setAcneSeverity(severity: String?) {
        context.dataStore.edit { preferences ->
            if (severity != null) {
                preferences[Keys.ACNE_SEVERITY] = severity
            } else {
                preferences.remove(Keys.ACNE_SEVERITY)
            }
        }
    }

    override suspend fun setHirsutismSeverity(severity: String?) {
        context.dataStore.edit { preferences ->
            if (severity != null) {
                preferences[Keys.HIRSUTISM_SEVERITY] = severity
            } else {
                preferences.remove(Keys.HIRSUTISM_SEVERITY)
            }
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun setVoiceModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_VOICE_MODE_ENABLED] = enabled }
    }
}
