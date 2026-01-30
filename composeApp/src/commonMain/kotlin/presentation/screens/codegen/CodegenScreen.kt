package presentation.screens.codegen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import presentation.components.TopBar
import presentation.components.SingleFilePicker
import presentation.components.DirectoryPickerComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodegenScreen(
    viewModel: CodegenViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    
    var javaPath by remember(uiState.javaPath) { mutableStateOf(uiState.javaPath) }
    var specPath by remember(uiState.specPath) { mutableStateOf(uiState.specPath) }
    var outputPath by remember(uiState.outputPath) { mutableStateOf(uiState.outputPath) }
    var packageName by remember(uiState.packageName) { mutableStateOf(uiState.packageName) }
    var shouldRebuild by remember(uiState.shouldRebuild) { mutableStateOf(uiState.shouldRebuild) }
    
    var library by remember(uiState.library) { mutableStateOf(uiState.library) }
    var serializationLibrary by remember(uiState.serializationLibrary) { mutableStateOf(uiState.serializationLibrary) }
    var useSealedClasses by remember(uiState.useSealedClasses) { mutableStateOf(uiState.useSealedClasses) }
    var oneOfInterfaces by remember(uiState.oneOfInterfaces) { mutableStateOf(uiState.oneOfInterfaces) }

    var showJavaPicker by remember { mutableStateOf(false) }
    var showSpecPicker by remember { mutableStateOf(false) }
    var showOutPicker by remember { mutableStateOf(false) }

    val libraries = listOf("jvm-ktor", "jvm-okhttp4", "jvm-retrofit2")
    val serializations = listOf("kotlinx_serialization", "gson", "jackson")

    Scaffold(
        topBar = { TopBar(title = "OpenAPI Codegen", onBackClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Paths
            OutlinedTextField(value = javaPath, onValueChange = { javaPath = it }, label = { Text("Java EXE Path") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showJavaPicker = true }) { Icon(Icons.Default.FolderOpen, null) } })
            
            OutlinedTextField(value = specPath, onValueChange = { specPath = it }, label = { Text("OpenAPI Spec Path") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showSpecPicker = true }) { Icon(Icons.Default.FolderOpen, null) } })
            
            OutlinedTextField(value = outputPath, onValueChange = { outputPath = it }, label = { Text("Output Directory") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showOutPicker = true }) { Icon(Icons.Default.FolderOpen, null) } })

            OutlinedTextField(value = packageName, onValueChange = { packageName = it }, label = { Text("Package Name") }, modifier = Modifier.fillMaxWidth())

            // Advanced Options
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Library Dropdown
                var expandedLib by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedLib, onExpandedChange = { expandedLib = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(value = library, onValueChange = {}, readOnly = true, label = { Text("Library") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLib) }, modifier = Modifier.menuAnchor())
                    ExposedDropdownMenu(expanded = expandedLib, onDismissRequest = { expandedLib = false }) {
                        libraries.forEach { lib -> DropdownMenuItem(text = { Text(lib) }, onClick = { library = lib; expandedLib = false }) }
                    }
                }

                // Serialization Dropdown
                var expandedSer by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedSer, onExpandedChange = { expandedSer = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(value = serializationLibrary, onValueChange = {}, readOnly = true, label = { Text("Serialization") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSer) }, modifier = Modifier.menuAnchor())
                    ExposedDropdownMenu(expanded = expandedSer, onDismissRequest = { expandedSer = false }) {
                        serializations.forEach { ser -> DropdownMenuItem(text = { Text(ser) }, onClick = { serializationLibrary = ser; expandedSer = false }) }
                    }
                }
            }

            // Checkboxes
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useSealedClasses, onCheckedChange = { useSealedClasses = it })
                    Text("Use Sealed Classes", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = oneOfInterfaces, onCheckedChange = { oneOfInterfaces = it })
                    Text("OneOf Interfaces", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = shouldRebuild, onCheckedChange = { shouldRebuild = it })
                    Text("Rebuild Generator (gradlew build)", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { 
                        viewModel.updateSettings(javaPath, specPath, outputPath, packageName, shouldRebuild, library, serializationLibrary, useSealedClasses, oneOfInterfaces)
                        viewModel.generate() 
                    },
                    enabled = !uiState.isGenerating,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Save & Generate")
                }

                OutlinedButton(
                    onClick = { viewModel.resetToDefaults() },
                    enabled = !uiState.isGenerating
                ) {
                    Text("Reset Defaults")
                }
            }

            // Logs
            if (uiState.logs.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Logs:", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { clipboardManager.setText(AnnotatedString(uiState.logs)) }) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy")
                    }
                }
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 400.dp), shape = MaterialTheme.shapes.medium) {
                    Text(text = uiState.logs, modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    // Pickers
    SingleFilePicker(showJavaPicker, fileExtensions = listOf("exe")) { file -> showJavaPicker = false; file?.path?.let { javaPath = it } }
    SingleFilePicker(showSpecPicker, fileExtensions = listOf("json", "yaml", "yml")) { file -> showSpecPicker = false; file?.path?.let { specPath = it } }
    DirectoryPickerComponent(showOutPicker) { path -> showOutPicker = false; path?.let { outputPath = it } }
}