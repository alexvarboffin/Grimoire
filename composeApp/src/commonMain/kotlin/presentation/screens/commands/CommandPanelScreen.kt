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
import data.local.entities.CommandPipeline
import data.local.entities.CommandHistory
import presentation.components.TopBar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun CommandPanelScreen(
    viewModel: CommandPanelViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showEditDialog by remember { mutableStateOf(false) }
    var presetToEdit by remember { mutableStateOf<CommandPreset?>(null) }
    
    var showPipelineDialog by remember { mutableStateOf(false) }
    var pipelineToEdit by remember { mutableStateOf<CommandPipeline?>(null) }

    var showExportPicker by remember { mutableStateOf(false) }
    var showImportPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = { TopBar(title = "Command Dashboard", onBackClick = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Sidebar: Presets, Pipelines, History
            var sidebarTab by remember { mutableStateOf(0) } // 0: Presets, 1: Pipelines, 2: History
            
            Surface(
                modifier = Modifier.width(300.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showExportPicker = true }) { Icon(Icons.Default.FileUpload, "Export") }
                        IconButton(onClick = { showImportPicker = true }) { Icon(Icons.Default.FileDownload, "Import") }
                    }
                    
                    TabRow(selectedTabIndex = sidebarTab) {
                        Tab(selected = sidebarTab == 0, onClick = { sidebarTab = 0 }, icon = { Icon(Icons.Default.List, null) })
                        Tab(selected = sidebarTab == 1, onClick = { sidebarTab = 1 }, icon = { Icon(Icons.Default.Route, null) })
                        Tab(selected = sidebarTab == 2, onClick = { sidebarTab = 2 }, icon = { Icon(Icons.Default.History, null) })
                    }

                    when (sidebarTab) {
                        0 -> SidebarPresetsContent(uiState, viewModel, onAdd = {
                            presetToEdit = CommandPreset(groupName = "Default", name = "New Command", executablePath = "", arguments = "", workingDir = "{project_root}")
                            showEditDialog = true
                        })
                        1 -> SidebarPipelinesContent(uiState, viewModel, onAdd = {
                            pipelineToEdit = null
                            showPipelineDialog = true
                        })
                        2 -> SidebarHistoryContent(uiState.history)
                    }
                }
            }

            // Main Content
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                if (uiState.selectedPreset != null) {
                    PresetDetailsContent(uiState.selectedPreset!!, uiState.isRunning, uiState.logs, viewModel, onEdit = {
                        presetToEdit = it
                        showEditDialog = true
                    })
                } else if (uiState.selectedPipeline != null) {
                    PipelineDetailsContent(uiState.selectedPipeline!!, uiState.selectedPipelineSteps, uiState.isRunning, uiState.logs, viewModel, onEdit = {
                        pipelineToEdit = it
                        showPipelineDialog = true
                    })
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a command or pipeline to start", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (uiState.showInputDialog) {
        DynamicInputDialog(
            inputs = uiState.pendingInputs,
            onDismiss = { viewModel.dismissInputDialog() },
            onConfirm = { viewModel.execute(
                File(System.getProperty("user.dir")), it
            ) }
        )
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

    if (showPipelineDialog) {
        PipelineEditDialog(
            pipeline = pipelineToEdit,
            allPresets = uiState.presets,
            currentSteps = if (pipelineToEdit != null) uiState.selectedPipelineSteps else emptyList(),
            onDismiss = { showPipelineDialog = false },
            onSave = { id, name, steps ->
                viewModel.savePipeline(id, name, steps)
                showPipelineDialog = false
            }
        )
    }

    CommandFilePickers(
        showExport = showExportPicker,
        showImport = showImportPicker,
        onExportDismiss = { showExportPicker = false },
        onImportDismiss = { showImportPicker = false },
        onExport = { viewModel.exportData(it) },
        onImport = { viewModel.importData(it) }
    )
}

@Composable
fun SidebarPresetsContent(uiState: CommandPanelUiState, viewModel: CommandPanelViewModel, onAdd: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Presets", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onAdd) { Icon(Icons.Default.Add, null) }
        }
        val grouped = uiState.presets.groupBy { it.groupName }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            grouped.forEach { (group, items) ->
                item { Text(text = group, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
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

@Composable
fun SidebarPipelinesContent(uiState: CommandPanelUiState, viewModel: CommandPanelViewModel, onAdd: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Pipelines", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onAdd) { Icon(Icons.Default.Add, null) }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.pipelines) { pipe ->
                NavigationDrawerItem(
                    label = { Text(pipe.name) },
                    selected = uiState.selectedPipeline?.id == pipe.id,
                    onClick = { viewModel.selectPipeline(pipe) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SidebarHistoryContent(history: List<CommandHistory>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(history) { h ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when(h.status) {
                        "SUCCESS" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        "FAILED" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(Modifier.padding(8.dp)) {
                    Text(h.commandName, style = MaterialTheme.typography.labelLarge)
                    Text(SimpleDateFormat("HH:mm:ss").format(Date(h.timestamp)), style = MaterialTheme.typography.bodySmall)
                    if (h.exitCode != null) {
                        Text("Exit Code: ${h.exitCode}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun PresetDetailsContent(preset: CommandPreset, isRunning: Boolean, logs: String, viewModel: CommandPanelViewModel, onEdit: (CommandPreset) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(preset.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onEdit(preset) }) { Icon(Icons.Default.Edit, null) }
            IconButton(onClick = { viewModel.deletePreset(preset) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
        Text("Exec: ${preset.executablePath}", style = MaterialTheme.typography.bodySmall)
        Text("Args: ${preset.arguments}", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.prepareExecution() }, enabled = !isRunning) {
                if (isRunning) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Run Command")
            }
            OutlinedButton(onClick = { viewModel.clearLogs() }) { Text("Clear Logs") }
        }
        Spacer(Modifier.height(16.dp))
        LogsContainer(logs)
    }
}

@Composable
fun PipelineDetailsContent(pipeline: CommandPipeline, steps: List<CommandPreset>, isRunning: Boolean, logs: String, viewModel: CommandPanelViewModel, onEdit: (CommandPipeline) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(pipeline.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onEdit(pipeline) }) { Icon(Icons.Default.Edit, null) }
            IconButton(onClick = { viewModel.deletePipeline(pipeline) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
        Text(pipeline.description, style = MaterialTheme.typography.bodyMedium)
        
        Spacer(Modifier.height(8.dp))
        Text("Steps:", style = MaterialTheme.typography.titleSmall)
        steps.forEachIndexed { index, step ->
            Text("${index + 1}. ${step.name}", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { viewModel.executePipeline(File(System.getProperty("user.dir"))) }, enabled = !isRunning) {
            if (isRunning) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text("Run Pipeline")
        }
        Spacer(Modifier.height(16.dp))
        LogsContainer(logs)
    }
}

@Composable
fun LogsContainer(logs: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), shape = MaterialTheme.shapes.medium) {
        Box(modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())) {
            Text(logs, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DynamicInputDialog(inputs: List<String>, onDismiss: () -> Unit, onConfirm: (Map<String, String>) -> Unit) {
    val values = remember { mutableStateMapOf<String, String>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Input Required") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                inputs.forEach { input ->
                    OutlinedTextField(
                        value = values[input] ?: "",
                        onValueChange = { values[input] = it },
                        label = { Text(input) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(values) }) { Text("Run") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CommandEditDialog(preset: CommandPreset, onDismiss: () -> Unit, onSave: (CommandPreset) -> Unit) {
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
                OutlinedTextField(value = path, onValueChange = { path = it }, label = { Text("Executable Path") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = args, onValueChange = { args = it }, label = { Text("Arguments") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dir, onValueChange = { dir = it }, label = { Text("Working Dir") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onSave(preset.copy(groupName = group, name = name, executablePath = path, arguments = args, workingDir = dir)) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PipelineEditDialog(
    pipeline: CommandPipeline?,
    allPresets: List<CommandPreset>,
    currentSteps: List<CommandPreset>,
    onDismiss: () -> Unit,
    onSave: (Long, String, List<Long>) -> Unit
) {
    var name by remember { mutableStateOf(pipeline?.name ?: "New Pipeline") }
    val selectedSteps = remember { mutableStateListOf<CommandPreset>().apply { addAll(currentSteps) } }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pipeline Editor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Pipeline Name") }, modifier = Modifier.fillMaxWidth())
                
                Text("Steps:", style = MaterialTheme.typography.titleSmall)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(selectedSteps) { step ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(step.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = { 
                                val index = selectedSteps.indexOf(step)
                                if (index > 0) {
                                    selectedSteps.removeAt(index)
                                    selectedSteps.add(index - 1, step)
                                }
                            }) { Icon(Icons.Default.ArrowUpward, null) }
                            IconButton(onClick = { 
                                val index = selectedSteps.indexOf(step)
                                if (index < selectedSteps.size - 1) {
                                    selectedSteps.removeAt(index)
                                    selectedSteps.add(index + 1, step)
                                }
                            }) { Icon(Icons.Default.ArrowDownward, null) }
                            IconButton(onClick = { selectedSteps.remove(step) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }

                Box {
                    Button(onClick = { expanded = true }) { Text("Add Step") }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        allPresets.forEach { preset ->
                            DropdownMenuItem(text = { Text(preset.name) }, onClick = {
                                selectedSteps.add(preset)
                                expanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = { 
            Button(onClick = { onSave(pipeline?.id ?: 0L, name, selectedSteps.map { it.id }) }) { Text("Save") } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CommandFilePickers(
    showExport: Boolean,
    showImport: Boolean,
    onExportDismiss: () -> Unit,
    onImportDismiss: () -> Unit,
    onExport: (File) -> Unit,
    onImport: (File) -> Unit
) {
    presentation.components.DirectoryPickerComponent(showExport) { path ->
        onExportDismiss()
        path?.let { onExport(File(it, "grimoire_commands_export.json")) }
    }

    presentation.components.SingleFilePicker(showImport, fileExtensions = listOf("json")) { file ->
        onImportDismiss()
        file?.path?.let { onImport(File(it)) }
    }
}
