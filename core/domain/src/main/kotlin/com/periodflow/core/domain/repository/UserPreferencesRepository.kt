package com.periodflow.core.domain.repository

import kotlinx.coroutines.flow.Flow

data class UserPreferences(
    val defaultCycleLength: Int = 28,
    val defaultPeriodLength: Int = 5,
    val hasCompletedOnboarding: Boolean = false,
    val isDarkMode: Boolean? = null, // null = follow system
    val lastPeriodStartEpochDay: Long? = null,
    val heightCm: Float? = null,
    val hasPolycysticOvaries: Boolean? = null,
    val weightKg: Float? = null,
    val acneSeverity: String? = null,
    val hirsutismSeverity: String? = null,
    val isBiometricEnabled: Boolean = false,
)

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setDefaultCycleLength(days: Int)
    suspend fun setDefaultPeriodLength(days: Int)
    suspend fun setOnboardingCompleted()
    suspend fun setDarkMode(isDark: Boolean?)
    suspend fun setLastPeriodStart(epochDay: Long)
    suspend fun setHeightCm(height: Float?)
    suspend fun setHasPolycysticOvaries(hasPcos: Boolean?)
    suspend fun setWeightKg(weight: Float?)
    suspend fun setAcneSeverity(severity: String?)
    suspend fun setHirsutismSeverity(severity: String?)
    suspend fun setBiometricEnabled(enabled: Boolean)
}
