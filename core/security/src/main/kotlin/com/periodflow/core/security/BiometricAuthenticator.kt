package com.periodflow.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.periodflow.core.domain.repository.AppAuthenticator
import com.periodflow.core.domain.repository.AuthResult
import com.periodflow.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BiometricAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsRepository: UserPreferencesRepository
) : AppAuthenticator {

    override val isAuthenticationEnabled: Flow<Boolean> = prefsRepository.userPreferences
        .map { it.isBiometricEnabled }

    override val isBiometricAvailable: Boolean
        get() {
            val biometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
        }

    override suspend fun authenticate(activity: FragmentActivity): AuthResult = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication Required")
            .setSubtitle("Please authenticate to access the app")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        if (continuation.isActive) continuation.resume(AuthResult.Cancelled)
                    } else if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS || errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE) {
                         if (continuation.isActive) continuation.resume(AuthResult.NoBiometric)
                    } else {
                        if (continuation.isActive) continuation.resume(AuthResult.Error(errString.toString()))
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (continuation.isActive) continuation.resume(AuthResult.Success)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }

        biometricPrompt.authenticate(promptInfo)
    }

    override suspend fun setAuthEnabled(enabled: Boolean) {
        prefsRepository.setBiometricEnabled(enabled)
    }
}
