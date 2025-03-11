package presentation.screens.tomlmerger

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import org.koin.compose.koinInject
import presentation.components.SingleFilePicker
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomlMergerScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: TomlMergerViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                title = "TOML Merger",
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = {
            if (uiState.error != null || uiState.success != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (uiState.error != null) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(uiState.error ?: uiState.success ?: "")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Source file selection
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Исходный файл",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Button(
                        onClick = { viewModel.showSourceFilePicker() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.FileOpen,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(if (uiState.sourceFilePath.isEmpty()) "Выбрать файл" else "Изменить файл")
                    }
                    
                    if (uiState.sourceFilePath.isNotEmpty()) {
                        Text(
                            text = "Выбран: ${uiState.sourceFilePath}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Target file selection
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Целевой файл",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Button(
                        onClick = { viewModel.showTargetFilePicker() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.FileOpen,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(if (uiState.targetFilePath.isEmpty()) "Выбрать файл" else "Изменить файл")
                    }
                    
                    if (uiState.targetFilePath.isNotEmpty()) {
                        Text(
                            text = "Выбран: ${uiState.targetFilePath}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Merge button
            Button(
                onClick = { viewModel.mergeFiles() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.sourceFilePath.isNotEmpty() && 
                         uiState.targetFilePath.isNotEmpty() &&
                         !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Merge,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Объединить файлы")
                }
            }

            // File pickers
            SingleFilePicker(
                show = uiState.showSourceFilePicker,
                fileExtensions = listOf("toml"),
                onFileSelected = { file: MPFile<Any>? ->
                    file?.path?.let { viewModel.setSourceFile(it) }
                }
            )

            SingleFilePicker(
                show = uiState.showTargetFilePicker,
                fileExtensions = listOf("toml"),
                onFileSelected = { file: MPFile<Any>? ->
                    file?.path?.let { viewModel.setTargetFile(it) }
                }
            )
        }
    }
} 