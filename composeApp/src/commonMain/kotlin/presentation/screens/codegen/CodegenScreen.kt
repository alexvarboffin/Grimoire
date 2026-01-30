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

    var showJavaPicker by remember { mutableStateOf(false) }
    var showSpecPicker by remember { mutableStateOf(false) }
    var showOutPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(title = "OpenAPI Codegen", onBackClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = javaPath,
                onValueChange = { javaPath = it },
                label = { Text("Path to Java EXE") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showJavaPicker = true }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Select Java EXE")
                    }
                }
            )

            OutlinedTextField(
                value = specPath,
                onValueChange = { specPath = it },
                label = { Text("Path to openapi.json") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showSpecPicker = true }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Select Spec")
                    }
                }
            )

            OutlinedTextField(
                value = outputPath,
                onValueChange = { outputPath = it },
                label = { Text("Output Directory") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showOutPicker = true }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Select Output Dir")
                    }
                }
            )

            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("Base Package Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = shouldRebuild,
                    onCheckedChange = { shouldRebuild = it }
                )
                Text("Rebuild generator before run (gradlew build)", style = MaterialTheme.typography.bodyMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { 
                        viewModel.updateSettings(javaPath, specPath, outputPath, packageName, shouldRebuild)
                        viewModel.generate() 
                    },
                    enabled = !uiState.isGenerating
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Save & Generate")
                }
            }

            if (uiState.logs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Logs:", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.logs))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy")
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 400.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = uiState.logs,
                        modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    SingleFilePicker(showJavaPicker, fileExtensions = listOf("exe")) { file ->
        showJavaPicker = false
        file?.path?.let { javaPath = it }
    }

    SingleFilePicker(showSpecPicker, fileExtensions = listOf("json", "yaml", "yml")) { file ->
        showSpecPicker = false
        file?.path?.let { specPath = it }
    }

    DirectoryPickerComponent(showOutPicker) { path ->
        showOutPicker = false
        path?.let { outputPath = it }
    }
}
