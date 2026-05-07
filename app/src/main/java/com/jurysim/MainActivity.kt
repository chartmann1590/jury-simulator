package com.jurysim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import com.jurysim.data.repository.PreferencesRepository
import com.jurysim.ui.adaptive.mapWidthSizeClass
import com.jurysim.ui.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Resolve the start destination synchronously from DataStore. This is
        // a one-shot read on the splash frame; for a single Boolean key it's
        // negligible (~10ms cold) and avoids a flicker between Onboarding and
        // Home which would happen with an asynchronous redirect.
        val modelReady = runBlocking {
            PreferencesRepository(applicationContext).modelReady.first()
        }
        val startDestination = if (modelReady) Screen.Home.route else Screen.Onboarding.route

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                JurySimAppRoot(
                    startDestination = startDestination,
                    windowSize = mapWidthSizeClass(windowSizeClass.widthSizeClass)
                )
            }
        }
    }
}
