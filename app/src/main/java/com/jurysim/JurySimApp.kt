package com.jurysim

import androidx.compose.runtime.Composable
import com.jurysim.ui.navigation.JurySimNavGraph
import com.jurysim.ui.theme.JurySimulatorTheme

@Composable
fun JurySimApp() {
    JurySimulatorTheme {
        JurySimNavGraph()
    }
}
