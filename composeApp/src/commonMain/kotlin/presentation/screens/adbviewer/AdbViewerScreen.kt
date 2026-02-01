package presentation.screens.adbviewer

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import presentation.components.TopBar
import presentation.components.DirectoryPickerComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbViewerScreen(
    viewModel: AdbViewerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeTab by remember { mutableStateOf(0) } // 0: Logcat, 1: Device Info

    var showExportPicker by remember { mutableStateOf(false) }
    var showScreenshotPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "ADB Viewer",
                onBackClick = onBack,
                actions = {
                    if (uiState.selectedDevice != null) {
                        IconButton(onClick = { showScreenshotPicker = true }) {
                            Icon(Icons.Default.Screenshot, "Take Screenshot")
                        }
                        IconButton(onClick = { showExportPicker = true }) {
                            Icon(Icons.Default.Save, "Export Logs")
                        }
                    }
                    IconButton(onClick = { viewModel.refreshDevices() }) {
                        Icon(Icons.Default.Refresh, "Refresh Devices")
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
                    Text(
                        "Devices",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyColumn {
                        items(uiState.devices) { device ->
                            NavigationDrawerItem(
                                label = {
                                    Column {
                                        Text(device.model)
                                        Text(device.id, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                selected = uiState.selectedDevice?.id == device.id,
                                onClick = { viewModel.selectDevice(device) },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            // Main Content
            if (uiState.selectedDevice != null) {
                Column(modifier = Modifier.weight(1f)) {
                    TabRow(selectedTabIndex = activeTab) {
                        Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Logcat") })
                        Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Device Info") })
                    }

                    if (uiState.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    when (activeTab) {
                        0 -> LogcatView(uiState, viewModel)
                        1 -> DeviceInfoView(uiState.deviceInfo)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a device to start viewing", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    DirectoryPickerComponent(showExportPicker) { path ->
        showExportPicker = false
        path?.let { viewModel.exportLogs(java.io.File(it)) }
    }

    DirectoryPickerComponent(showScreenshotPicker) { path ->
        showScreenshotPicker = false
        path?.let { viewModel.takeScreenshot(java.io.File(it)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatView(uiState: AdbViewerUiState, viewModel: AdbViewerViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.filter,
                onValueChange = { viewModel.updateFilter(it) },
                label = { Text("Filter logs") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
            
            IconButton(onClick = { viewModel.togglePause() }) {
                Icon(
                    if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (uiState.isPaused) "Resume" else "Pause"
                )
            }
            
            IconButton(onClick = { viewModel.clearLogs() }) {
                Icon(Icons.Default.DeleteSweep, "Clear Logs")
            }
        }
        
        // Log Level Filters
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LogLevel.values().forEach { level ->
                FilterChip(
                    selected = uiState.selectedLogLevel == level,
                    onClick = { 
                        viewModel.setLogLevel(if (uiState.selectedLogLevel == level) null else level)
                    },
                    label = { Text(level.name) }
                )
            }
        }

        val filteredLogs = remember(uiState.logLines, uiState.filter, uiState.selectedLogLevel) {
            uiState.logLines.filter { line ->
                val matchesFilter = if (uiState.filter.isBlank()) true else line.contains(uiState.filter, ignoreCase = true)
                val matchesLevel = if (uiState.selectedLogLevel == null) true else line.contains(uiState.selectedLogLevel.code)
                matchesFilter && matchesLevel
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            shape = MaterialTheme.shapes.medium
        ) {
            val scrollState = rememberScrollState()
            
            // Auto-scroll to bottom if not paused
            if (!uiState.isPaused) {
                LaunchedEffect(filteredLogs.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                filteredLogs.forEach { line ->
                    Text(
                        text = line,
                        color = when {
                            line.contains(" E ") -> Color.Red
                            line.contains(" W ") -> Color(0xFFFFA500)
                            line.contains(" I ") -> Color.Green
                            line.contains(" D ") -> Color.Cyan
                            else -> Color.LightGray
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceInfoView(info: Map<String, String>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(info.toList()) { (key, value) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = key,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = value,
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}