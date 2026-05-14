package com.charles.jurysim.ui.screens.simulation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.charles.jurysim.ui.adaptive.AdaptiveCenteredContent
import com.charles.jurysim.ui.components.LoadingIndicator
import com.charles.jurysim.ui.components.PhaseIndicator
import com.charles.jurysim.ui.theme.GuiltyRed
import com.charles.jurysim.ui.theme.NotGuiltyGreen
import com.charles.jurysim.ui.screens.simulation.InfoCard

@Composable
fun VerdictScreen(
    viewModel: SimulationViewModel,
    onNewSimulation: () -> Unit,
    onMainMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "verdict")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        PhaseIndicator(currentPhase = state.currentPhase)

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .scale(scale),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    LoadingIndicator("Determining verdict...")
                }
            }
        } else {
            AdaptiveCenteredContent(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "VERDICT",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Determine verdict color and display
                val verdictValue = state.verdict
                val isMistrial = state.isMistrial || verdictValue == "MISTRIAL"
                val isGuilty = verdictValue?.contains("GUILTY", ignoreCase = true) == true && verdictValue.contains("NOT", ignoreCase = true) == false
                val verdictColor = when {
                    isMistrial -> Color(0xFFFF9800) // Orange for mistrial
                    isGuilty -> GuiltyRed
                    else -> NotGuiltyGreen
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(1000)) + scaleIn(animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = verdictColor.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (isMistrial) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = verdictColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.verdict ?: "PENDING",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = verdictColor
                            )

                            if (isMistrial) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "The jury could not reach a unanimous decision",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Show voting rounds info
                            if (state.currentVotingRound > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Reached after ${state.currentVotingRound} voting round(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Case Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                InfoCard(
                    icon = Icons.Default.Star,
                    title = "Case",
                    content = state.caseTitle
                )
                Spacer(modifier = Modifier.height(8.dp))

                InfoCard(
                    icon = Icons.Default.Person,
                    title = "Defendant",
                    content = state.defendantName
                )
                Spacer(modifier = Modifier.height(8.dp))

                InfoCard(
                    icon = Icons.Default.Warning,
                    title = "Charges",
                    content = state.charges
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Show reasoning if available
                state.messages.lastOrNull()?.let { lastMessage ->
                    if (!lastMessage.isUser) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMistrial) "Judge's Statement" else "Reasoning",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = lastMessage.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                    Button(
                        onClick = onNewSimulation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start New Case", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onMainMenu,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Return to Main Menu", style = MaterialTheme.typography.titleMedium)
                }

                // Extra bottom padding
                Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
