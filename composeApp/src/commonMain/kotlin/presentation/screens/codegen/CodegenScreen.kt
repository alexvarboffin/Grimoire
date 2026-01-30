package presentation.screens.codegen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import presentation.components.TopBar

@Composable
fun CodegenScreen(
    viewModel: CodegenViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Временные стейты для полей ввода (чтобы не дергать DataStore на каждый символ)
    var javaPath by remember(uiState.javaPath) { mutableStateOf(uiState.javaPath) }
    var specPath by remember(uiState.specPath) { mutableStateOf(uiState.specPath) }
    var outputPath by remember(uiState.outputPath) { mutableStateOf(uiState.outputPath) }
    var packageName by remember(uiState.packageName) { mutableStateOf(uiState.packageName) }

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
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = specPath,
                onValueChange = { specPath = it },
                label = { Text("Path to openapi.json") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = outputPath,
                onValueChange = { outputPath = it },
                label = { Text("Output Directory") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("Base Package Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { 
                        viewModel.updateSettings(javaPath, specPath, outputPath, packageName)
                        viewModel.generate() 
                    },
                    enabled = !uiState.isGenerating
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(size = 20.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Save & Generate")
                }
            }

            if (uiState.logs.isNotEmpty()) {
                Text("Logs:", style = MaterialTheme.typography.titleSmall)
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
}

private val CircularProgressIndicatorSize = 20.dp
@Composable
fun CircularProgressIndicator(size: androidx.compose.ui.unit.Dp, color: androidx.compose.ui.graphics.Color) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = color,
        strokeWidth = 2.dp
    )
}
