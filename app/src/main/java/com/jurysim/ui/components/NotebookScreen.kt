package com.jurysim.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jurysim.data.model.Fact
import com.jurysim.data.model.FactType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
    facts: List<Fact>,
    generalNotes: String,
    onClose: () -> Unit,
    onUpdateFactNotes: (String, String) -> Unit,
    onUpdateGeneralNotes: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("General", "All Facts", "People", "Evidence")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Juror Notebook") },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (selectedTab == 0) {
                // General Notes Tab
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    OutlinedTextField(
                        value = generalNotes,
                        onValueChange = onUpdateGeneralNotes,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = { Text("Write your general thoughts, theories, and questions here...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            } else {
                val filteredFacts = when (selectedTab) {
                    2 -> facts.filter { it.type == FactType.PERSON }
                    3 -> facts.filter { it.type == FactType.EVIDENCE }
                    else -> facts
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredFacts.isEmpty()) {
                        item {
                            Text(
                                "No facts recorded in this category yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(filteredFacts) { fact ->
                            FactCard(fact, onUpdateFactNotes)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FactCard(fact: Fact, onUpdateNotes: (String, String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(fact.userNotes) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Badge(
                    containerColor = when (fact.type) {
                        FactType.PERSON -> MaterialTheme.colorScheme.primary
                        FactType.EVIDENCE -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(fact.type.name.take(1))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(fact.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(fact.source, style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Notes")
                }
            }
            
            Text(fact.description, style = MaterialTheme.typography.bodyMedium)
            
            if (isExpanded || fact.userNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isExpanded) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { 
                            notes = it
                            onUpdateNotes(fact.id, it)
                        },
                        label = { Text("My Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        "My Notes: ${fact.userNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
