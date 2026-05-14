package com.charles.jurysim

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.charles.jurysim.ui.adaptive.AppWindowSize
import com.charles.jurysim.ui.adaptive.LocalAppWindowSize
import com.charles.jurysim.ui.ads.GlobalBannerAd
import com.charles.jurysim.ui.navigation.JurySimNavGraph
import com.charles.jurysim.ui.theme.JurySimulatorTheme

/**
 * Root composable for the app — wraps the navigation graph in the Material
 * theme. Replaces the previous top-level `JurySimApp()` composable; the name
 * `JurySimApp` is now reserved for the [Application] subclass.
 */
@Composable
fun JurySimAppRoot(
    startDestination: String,
    windowSize: AppWindowSize
) {
    JurySimulatorTheme {
        CompositionLocalProvider(LocalAppWindowSize provides windowSize) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    GlobalBannerAd()
                }
            ) { innerPadding ->
                JurySimNavGraph(
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}
