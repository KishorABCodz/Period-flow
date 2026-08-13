package com.periodflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.lifecycle.lifecycleScope
import com.periodflow.app.navigation.PeriodFlowNavHost
import com.periodflow.core.domain.repository.UserPreferencesRepository
import com.periodflow.core.domain.repository.AppAuthenticator
import com.periodflow.core.domain.repository.AuthResult
import com.periodflow.core.ui.theme.PeriodFlowTheme
import com.periodflow.feature.onboarding.navigation.OnboardingRoute
import com.periodflow.feature.home.navigation.HomeRoute
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var prefsRepository: UserPreferencesRepository
    
    @Inject
    lateinit var authenticator: AppAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs by prefsRepository.userPreferences.collectAsStateWithLifecycle(
                initialValue = null,
            )
            
            val isAuthEnabled by authenticator.isAuthenticationEnabled.collectAsStateWithLifecycle(
                initialValue = false
            )
            
            var isAuthenticated by remember { mutableStateOf(false) }
            var authError by remember { mutableStateOf<String?>(null) }

            // Trigger auth when needed
            LaunchedEffect(isAuthEnabled, isAuthenticated) {
                if (isAuthEnabled && !isAuthenticated) {
                    when (val result = authenticator.authenticate(this@MainActivity)) {
                        is AuthResult.Success -> isAuthenticated = true
                        is AuthResult.Error -> authError = result.message
                        is AuthResult.Cancelled -> authError = "Authentication cancelled"
                        is AuthResult.NoBiometric -> isAuthenticated = true // bypass if no hardware
                    }
                }
            }

            val isDarkTheme = prefs?.isDarkMode

            PeriodFlowTheme(
                darkTheme = isDarkTheme ?: androidx.compose.foundation.isSystemInDarkTheme(),
                dynamicColor = isDarkTheme == null,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (prefs == null) {
                        // Loading state
                        return@Surface
                    }
                    
                    if (isAuthEnabled && !isAuthenticated) {
                        // Auth Block UI
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = authError ?: "App is locked")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {
                                    lifecycleScope.launch {
                                        when (val result = authenticator.authenticate(this@MainActivity)) {
                                            is AuthResult.Success -> isAuthenticated = true
                                            is AuthResult.Error -> authError = result.message
                                            is AuthResult.Cancelled -> authError = "Authentication cancelled"
                                            is AuthResult.NoBiometric -> isAuthenticated = true
                                        }
                                    }
                                }) {
                                    Text("Unlock")
                                }
                            }
                        }
                    } else {
                        // Main App UI
                        val startDest = if (prefs?.hasCompletedOnboarding == true) HomeRoute else OnboardingRoute
                        PeriodFlowNavHost(startDestination = startDest)
                    }
                }
            }
        }
    }
}
