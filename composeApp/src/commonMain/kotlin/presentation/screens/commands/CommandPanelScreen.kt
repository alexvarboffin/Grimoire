package presentation.screens.commands

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.CommandPreset
import presentation.components.TopBar
import java.io.File

@Composable
fun CommandPanelScreen(
    viewModel: CommandPanelViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var presetToEdit by remember { mutableStateOf<CommandPreset?>(null) }

    Scaffold(
        topBar = { TopBar(title = "Command Dashboard", onBackClick = onBack) }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Sidebar: Groups & Presets
            Surface(
                modifier = Modifier.width(280.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Groups", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { 
                            presetToEdit = CommandPreset(groupName = "Default", name = "New Command", executablePath = "", arguments = "", workingDir = "{project_root}")
                            showEditDialog = true 
                        }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                    
                    val grouped = uiState.presets.groupBy { it.groupName }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        grouped.forEach { (group, items) ->
                            item {
                                Text(
                                    text = group,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(items) { item ->
                                NavigationDrawerItem(
                                    label = { Text(item.name) },
                                    selected = uiState.selectedPreset?.id == item.id,
                                    onClick = { viewModel.selectPreset(item) },
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Content: Details & Execution
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                val selected = uiState.selectedPreset
                if (selected != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selected.name, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { presetToEdit = selected; showEditDialog = true }) {
                            Icon(Icons.Default.Edit, null)
                        }
                        IconButton(onClick = { viewModel.deletePreset(selected) }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Text("Exec: ${selected.executablePath}", style = MaterialTheme.typography.bodySmall)
                    Text("Args: ${selected.arguments}", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { 
                                // В реальности сюда надо передать корень проекта, 
                                // пока используем текущую папку
                                viewModel.execute(File(System.getProperty("user.dir"))) 
                            },
                            enabled = !uiState.isRunning
                        ) {
                            if (uiState.isRunning) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.PlayArrow, null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Run Command")
                        }
                        
                        OutlinedButton(onClick = { viewModel.clearLogs() }) {
                            Text("Clear Logs")
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())) {
                            Text(uiState.logs, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a command to start", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        CommandEditDialog(
            preset = presetToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { 
                viewModel.savePreset(it)
                showEditDialog = false 
            }
        )
    }
}

@Composable
fun CommandEditDialog(
    preset: CommandPreset,
    onDismiss: () -> Unit,
    onSave: (CommandPreset) -> Unit
) {
    var group by remember { mutableStateOf(preset.groupName) }
    var name by remember { mutableStateOf(preset.name) }
    var path by remember { mutableStateOf(preset.executablePath) }
    var args by remember { mutableStateOf(preset.arguments) }
    var dir by remember { mutableStateOf(preset.workingDir) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Command Editor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = group, onValueChange = { group = it }, label = { Text("Group") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = path, onValueChange = { path = it }, label = { Text("Executable / Script Path") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = args, onValueChange = { args = it }, label = { Text("Arguments") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dir, onValueChange = { dir = it }, label = { Text("Working Directory") }, modifier = Modifier.fillMaxWidth())
                Text("Macros: {project_root}, {user_home}, {timestamp}, {date}", style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(preset.copy(groupName = group, name = name, executablePath = path, arguments = args, workingDir = dir)) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
