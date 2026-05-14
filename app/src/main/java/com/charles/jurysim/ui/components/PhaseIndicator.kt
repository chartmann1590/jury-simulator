package com.charles.jurysim.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.charles.jurysim.data.model.TrialPhase

@Composable
fun PhaseIndicator(currentPhase: TrialPhase) {
    val phaseText = when (currentPhase) {
        TrialPhase.SETUP -> "Setup"
        TrialPhase.MODEL_SELECTION -> "Model Selection"
        TrialPhase.INTRO -> "Case Introduction"
        TrialPhase.VOIR_DIRE -> "Jury Selection"
        TrialPhase.OPENING_STATEMENTS -> "Opening Statements"
        TrialPhase.WITNESS_TESTIMONY -> "Witness Testimony"
        TrialPhase.EVIDENCE_PRESENTATION -> "Evidence"
        TrialPhase.CLOSING_ARGUMENTS -> "Closing Arguments"
        TrialPhase.DELIBERATION -> "Jury Deliberation"
        TrialPhase.VERDICT -> "Verdict"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = phaseText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
