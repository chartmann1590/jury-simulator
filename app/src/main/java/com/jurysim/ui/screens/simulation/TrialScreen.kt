package com.jurysim.ui.screens.simulation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jurysim.data.model.TrialPhase
import com.jurysim.ui.components.ChatBubble
import com.jurysim.ui.components.LoadingIndicator
import com.jurysim.ui.components.NotebookScreen
import com.jurysim.ui.components.PhaseIndicator
import kotlinx.coroutines.launch

@Composable
fun TrialScreen(
    viewModel: SimulationViewModel,
    onTrialComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showQuestionDialog by remember { mutableStateOf(false) }
    var questionInput by remember { mutableStateOf("") }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    LaunchedEffect(state.currentPhase) {
        if (state.currentPhase == TrialPhase.DELIBERATION) {
            onTrialComplete()
        }
    }
    
    // Notebook Overlay
    if (state.showNotebook) {
        NotebookScreen(
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                PhaseIndicator(currentPhase = state.currentPhase)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages) { message ->
                        ChatBubble(message = message)
                    }

                    if (state.isLoading) {
                        item {
                            LoadingIndicator(getLoadingText(state.currentPhase))
                        }
                    }
                }

                if (state.error != null) {
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

                // Interaction Area
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    // Question Button during Witness Testimony
                    if (state.currentPhase == TrialPhase.WITNESS_TESTIMONY && !state.isLoading) {
                        Button(
                            onClick = { showQuestionDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Question to Judge")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Continue button
                    if (!state.isLoading && canContinue(state.currentPhase)) {
                        Button(
                            onClick = { viewModel.proceedToNextPhase() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(getButtonText(state.currentPhase, state.currentWitnessIndex, state.totalWitnesses))
                        }
                    }
                }
            }
        }
    }
    
    if (showQuestionDialog) {
        AlertDialog(
            onDismissRequest = { showQuestionDialog = false },
            title = { Text("Submit Question") },
            text = {
                Column {
                    Text("Ask a question to the current witness. The Judge will decide if it is permissible.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Where were you at 8 PM?") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (questionInput.isNotBlank()) {
                            viewModel.submitQuestion(questionInput)
                            questionInput = ""
                            showQuestionDialog = false
                        }
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuestionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun canContinue(phase: TrialPhase): Boolean {
    return when (phase) {
        TrialPhase.OPENING_STATEMENTS,
        TrialPhase.WITNESS_TESTIMONY,
        TrialPhase.EVIDENCE_PRESENTATION,
        TrialPhase.CLOSING_ARGUMENTS -> true
        else -> false
    }
}

private fun getButtonText(phase: TrialPhase, currentWitnessIndex: Int, totalWitnesses: Int): String {
    return when (phase) {
        TrialPhase.OPENING_STATEMENTS -> "Continue to Witness Testimony"
        TrialPhase.WITNESS_TESTIMONY -> {
            if (currentWitnessIndex < totalWitnesses) "Call Next Witness"
            else "Continue to Evidence"
        }
        TrialPhase.EVIDENCE_PRESENTATION -> "Continue to Closing Arguments"
        TrialPhase.CLOSING_ARGUMENTS -> "Proceed to Deliberation"
        else -> "Continue"
    }
}

private fun getLoadingText(phase: TrialPhase): String {
    return when (phase) {
        TrialPhase.OPENING_STATEMENTS -> "Preparing opening statements..."
        TrialPhase.WITNESS_TESTIMONY -> "Calling witness..."
        TrialPhase.EVIDENCE_PRESENTATION -> "Presenting evidence..."
        TrialPhase.CLOSING_ARGUMENTS -> "Preparing closing arguments..."
        else -> "Processing..."
    }
}
