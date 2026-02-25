package presentation.screens.push

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import presentation.components.TopBar
import presentation.components.SingleFilePicker
import presentation.components.DirectoryPickerComponent
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushNotificationListScreen(
    viewModel: PushNotificationViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.listState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showExportPicker by remember { mutableStateOf(false) }
    var showImportPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToastList()
        }
    }

    Scaffold(
        topBar = { 
            TopBar(
                title = "Push Notification Tester", 
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { showExportPicker = true }) { Icon(Icons.Default.FileUpload, "Export") }
                    IconButton(onClick = { showImportPicker = true }) { Icon(Icons.Default.FileDownload, "Import") }
                }
            ) 
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Config")
            }
        }
    ) { padding ->
        if (uiState.configs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No configurations found. Add one to start.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.configs) { config ->
                    ConfigCard(
                        config = config,
                        onClick = { onNavigateToDetail(config.id) },
                        onDelete = { viewModel.deleteConfig(config) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddConfigDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, path ->
                    viewModel.addConfig(name, path)
                    showAddDialog = false
                }
            )
        }

        PushFilePickers(
            showExport = showExportPicker,
            showImport = showImportPicker,
            onExportDismiss = { showExportPicker = false },
            onImportDismiss = { showImportPicker = false },
            onExport = { viewModel.exportData(it) },
            onImport = { viewModel.importData(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigCard(
    config: data.local.entities.PushConfigEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = config.name, style = MaterialTheme.typography.titleMedium)
                Text(text = config.serviceAccountJsonPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun AddConfigDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }
    var showFilePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Firebase Config") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Config Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Service Account JSON Path") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showFilePicker = true }) {
                            Icon(Icons.Default.FolderOpen, null)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, path) },
                enabled = name.isNotBlank() && path.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    SingleFilePicker(showFilePicker, fileExtensions = listOf("json")) { file ->
        showFilePicker = false
        file?.path?.let { path = it }
    }
}

@Composable
private fun PushFilePickers(
    showExport: Boolean,
    showImport: Boolean,
    onExportDismiss: () -> Unit,
    onImportDismiss: () -> Unit,
    onExport: (File) -> Unit,
    onImport: (File) -> Unit
) {
    DirectoryPickerComponent(showExport) { path ->
        onExportDismiss()
        path?.let { onExport(File(it, "grimoire_push_configs_export.json")) }
    }

    SingleFilePicker(showImport, fileExtensions = listOf("json")) { file ->
        onImportDismiss()
        file?.path?.let { onImport(File(it)) }
    }
}
