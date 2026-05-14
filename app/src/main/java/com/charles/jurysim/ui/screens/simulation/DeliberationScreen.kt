package com.charles.jurysim.ui.screens.simulation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.charles.jurysim.data.model.AIJuror
import com.charles.jurysim.data.model.TrialPhase
import com.charles.jurysim.data.model.VoteChoice
import com.charles.jurysim.ui.audio.ChatTtsPlayer
import com.charles.jurysim.ui.adaptive.AdaptiveCenteredContent
import com.charles.jurysim.ui.components.ChatBubble
import com.charles.jurysim.ui.components.LoadingIndicator
import com.charles.jurysim.ui.components.PhaseIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliberationScreen(
    viewModel: SimulationViewModel,
    onVerdictReady: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var userInput by remember { mutableStateOf("") }
    var isJurorListExpanded by remember { mutableStateOf(false) }
    var showVotingDialog by remember { mutableStateOf(false) }
    var showJurorProfile by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val ttsPlayer = remember { ChatTtsPlayer(context) }

    DisposableEffect(Unit) {
        onDispose { ttsPlayer.release() }
    }

    // Get current conversation based on selected juror
    val currentMessages = if (state.currentJurorChatId == -1) {
        state.messages
    } else {
        state.jurorConversations[state.currentJurorChatId] ?: emptyList()
    }

    val currentJuror = state.aiJurors.find { it.id == state.currentJurorChatId }

    if (showJurorProfile && currentJuror != null) {
        // ... (Juror Profile Dialog code remains same)
        AlertDialog(
            onDismissRequest = { showJurorProfile = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentJuror.name.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(currentJuror.name, style = MaterialTheme.typography.titleMedium)
                        Text(currentJuror.occupation, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Age: ${currentJuror.age}", style = MaterialTheme.typography.bodyMedium)
                    Text("Personality: ${currentJuror.personality}", style = MaterialTheme.typography.bodyMedium)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text("Hidden Factor:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(currentJuror.hiddenBias, style = MaterialTheme.typography.bodyMedium)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Initial Leaning: ${currentJuror.initialLeaning.name.replace("LEANING_", "").replace("_", " ")}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showJurorProfile = false }) {
                    Text("Close")
                }
            }
        )
    }

    LaunchedEffect(currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(currentMessages.size - 1)
            }
        }
    }

    LaunchedEffect(state.currentPhase) {
        if (state.currentPhase == TrialPhase.VERDICT) {
            onVerdictReady()
        }
    }

    // Voting Dialog
    if (showVotingDialog && !state.isLoading) {
        VotingDialog(
            state = state,
            onVote = { vote ->
                viewModel.submitUserVote(vote)
                showVotingDialog = false
            },
            onDismiss = { showVotingDialog = false }
        )
    }

    // Notebook Overlay
    if (state.showNotebook) {
        com.charles.jurysim.ui.components.NotebookScreen(
            facts = state.facts,
            generalNotes = state.notes,
            onClose = { viewModel.toggleNotebook() },
            onUpdateFactNotes = { id, notes -> viewModel.updateFactNotes(id, notes) },
            onUpdateGeneralNotes = { notes -> viewModel.updateNotes(notes) }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { viewModel.toggleNotebook() }) {
                    Icon(Icons.Default.Edit, contentDescription = "Open Notebook")
                }
            },
            bottomBar = {
                // Input area (moved from Column to Scaffold bottomBar to stay fixed)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .imePadding(), // Ensure it moves up with keyboard
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    if (currentJuror != null) "Talk to ${currentJuror.name}..."
                                    else "Share your thoughts with the jury..."
                                )
                            },
                            enabled = !state.isLoading,
                            shape = MaterialTheme.shapes.large,
                            maxLines = 4
                        )

                        FloatingActionButton(
                            onClick = {
                                if (userInput.isNotBlank()) {
                                    if (state.currentJurorChatId == -1) {
                                        viewModel.contributeToDeliberation(userInput)
                                    } else {
                                        viewModel.chatWithJuror(state.currentJurorChatId, userInput)
                                    }
                                    userInput = ""
                                }
                            },
                            shape = CircleShape,
                            containerColor = if (!state.isLoading && userInput.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (!state.isLoading && userInput.isNotBlank())
                                    MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            AdaptiveCenteredContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    PhaseIndicator(currentPhase = state.currentPhase)

                // Juror Selection Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column {
                        Surface(
                            onClick = { isJurorListExpanded = !isJurorListExpanded },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Jurors",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentJuror != null) "Chatting with ${currentJuror.name}" else "Group Discussion",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                if (currentJuror != null) {
                                    IconButton(onClick = { showJurorProfile = true }) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "Profile",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Icon(
                                    if (isJurorListExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isJurorListExpanded) "Collapse" else "Expand"
                                )
                            }
                        }

                        AnimatedVisibility(visible = isJurorListExpanded) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                // Group chat option
                                JurorChip(
                                    name = "Group Discussion",
                                    isSelected = state.currentJurorChatId == -1,
                                    onClick = { viewModel.setCurrentJurorChat(-1) }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Individual Jurors:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    items(state.aiJurors) { juror ->
                                        JurorChip(
                                            name = juror.name.split(" ").first(),
                                            isSelected = state.currentJurorChatId == juror.id,
                                            onClick = { viewModel.setCurrentJurorChat(juror.id) },
                                            subtitle = juror.occupation
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons row (Vote)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.startVoting()
                            showVotingDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading && !state.isVotingPhase,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Vote (${state.currentVotingRound}/${state.maxVotingRounds})",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentMessages) { message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ChatBubble(
                                message = message,
                                showPlayButton = state.ttsEnabled && !message.isUser,
                                onPlayClick = { selected ->
                                    ttsPlayer.speak(
                                        selected,
                                        judgeGender = state.judgeGender,
                                        judgeVoiceSeed = state.judgeVoiceSeed
                                    )
                                }
                            )
                        }
                    }

                    if (state.isLoading) {
                        item {
                            LoadingIndicator(
                                if (state.isVotingPhase) "Collecting votes from jurors..."
                                else if (currentJuror != null) "${currentJuror.name} is thinking..."
                                else "Jurors are discussing..."
                            )
                        }
                    }
                }

                // Error display
                    AnimatedVisibility(
                        visible = state.error != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Error: ${state.error}",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.retryLastOperation() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JurorChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null
                 else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EvidenceCard(evidence: com.charles.jurysim.data.model.Evidence) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = evidence.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = evidence.description,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Significance: ${evidence.significance}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VotingDialog(
    state: com.charles.jurysim.data.model.SimulationState,
    onVote: (VoteChoice) -> Unit,
    onDismiss: () -> Unit
) {
    val guiltyVotes = state.lastVoteResults.values.count { it == VoteChoice.GUILTY }
    val notGuiltyVotes = state.lastVoteResults.values.count { it == VoteChoice.NOT_GUILTY }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cast Your Vote - Round ${state.currentVotingRound}")
        },
        text = {
            Column {
                Text(
                    "Other jurors have voted:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$guiltyVotes",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text("Guilty", style = MaterialTheme.typography.labelMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$notGuiltyVotes",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Not Guilty", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "A unanimous verdict is required. If not reached after ${state.maxVotingRounds} rounds, it will be a mistrial.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onVote(VoteChoice.NOT_GUILTY) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Not Guilty")
                }
                Button(
                    onClick = { onVote(VoteChoice.GUILTY) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Guilty")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
