package com.periodflow.core.domain.repository

import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.Flow

sealed interface AuthResult {
    data object Success : AuthResult
    data object Cancelled : AuthResult
    data class Error(val message: String) : AuthResult
    data object NoBiometric : AuthResult
}

interface AppAuthenticator {
    val isAuthenticationEnabled: Flow<Boolean>
    val isBiometricAvailable: Boolean
    suspend fun authenticate(activity: FragmentActivity): AuthResult
    suspend fun setAuthEnabled(enabled: Boolean)
}
