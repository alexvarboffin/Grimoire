package presentation.screens.fileexplorer

import androidx.compose.foundation.clickable
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
import domain.model.RemoteFile
import presentation.components.TopBar
import presentation.components.DirectoryPickerComponent
import presentation.components.SingleFilePicker
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    viewModel: FileExplorerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showCreateDirDialog by remember { mutableStateOf(false) }
    var showPushPicker by remember { mutableStateOf(false) }
    var showPullPicker by remember { mutableStateOf(false) }
    var fileToPull by remember { mutableStateOf<RemoteFile?>(null) }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Device File Explorer",
                onBackClick = onBack,
                actions = {
                    if (uiState.selectedDevice != null) {
                        IconButton(onClick = { showCreateDirDialog = true }) {
                            Icon(Icons.Default.CreateNewFolder, "New Folder")
                        }
                        IconButton(onClick = { showPushPicker = true }) {
                            Icon(Icons.Default.FileUpload, "Upload File")
                        }
                    }
                    IconButton(onClick = { viewModel.refreshDevices() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Devices Sidebar
            Surface(
                modifier = Modifier.width(250.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column {
                    Text("Devices", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    LazyColumn {
                        items(uiState.devices) { device ->
                            NavigationDrawerItem(
                                label = { Text(device.model) },
                                selected = uiState.selectedDevice?.id == device.id,
                                onClick = { viewModel.selectDevice(device) },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            // Main Content
            Column(modifier = Modifier.weight(1f)) {
                if (uiState.selectedDevice != null) {
                    // Path bar
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(Icons.Default.ArrowUpward, "Up")
                        }
                        Text(
                            text = uiState.currentPath,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (uiState.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    // File List
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.files) { file ->
                            FileItem(
                                file = file,
                                onClick = {
                                    if (file.isDirectory) {
                                        viewModel.loadFiles(file.path)
                                    }
                                },
                                onPull = {
                                    fileToPull = file
                                    showPullPicker = true
                                },
                                onDelete = { viewModel.deleteFile(file) }
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a device to explore files", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showCreateDirDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDirDialog = false },
            title = { Text("Create Directory") },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.createDirectory(name)
                        showCreateDirDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDirDialog = false }) { Text("Cancel") } }
        )
    }

    SingleFilePicker(showPushPicker) { file ->
        showPushPicker = false
        file?.path?.let { viewModel.pushFile(File(it)) }
    }

    DirectoryPickerComponent(showPullPicker) { path ->
        showPullPicker = false
        if (path != null && fileToPull != null) {
            viewModel.pullFile(fileToPull!!, File(path))
        }
        fileToPull = null
    }
}

@Composable
fun FileItem(
    file: RemoteFile,
    onClick: () -> Unit,
    onPull: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(file.name) },
        supportingContent = {
            Text("${file.permissions} | ${if (file.isDirectory) "Dir" else "${file.size} bytes"} | ${file.lastModified}")
        },
        leadingContent = {
            Icon(
                if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                contentDescription = null,
                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        },
        trailingContent = {
            Row {
                if (!file.isDirectory) {
                    IconButton(onClick = onPull) {
                        Icon(Icons.Default.Download, "Download")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}
