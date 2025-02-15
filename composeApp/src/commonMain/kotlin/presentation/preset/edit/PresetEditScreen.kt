package presentation.preset.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.TextReplacement
import moe.tlaster.precompose.viewmodel.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PresetEditScreen(
    presetId: Long?,
    onNavigateBack: () -> Unit,
    
    viewModel: PresetEditViewModel = viewModel { PresetEditViewModel(get(
        clazz = TODO(),
        qualifier = TODO(),
        parameters = TODO()
    ), presetId) }
) {
    val state by viewModel.state.collectAsState()
    var showAddExtensionDialog by remember { mutableStateOf(false) }
    var showAddReplacementDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.name.isNotEmpty()) state.name else "Новый пресет") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.savePreset()
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Название пресета") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = state.targetDirectory,
                    onValueChange = { viewModel.updateTargetDirectory(it) },
                    label = { Text("Целевая директория") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { /* TODO: Добавить выбор директории */ }) {
                            Icon(Icons.Default.Folder, contentDescription = "Выбрать папку")
                        }
                    }
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Расширения файлов", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { showAddExtensionDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить расширение")
                            }
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.fileExtensions.forEach { extension ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(extension) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { viewModel.removeFileExtension(extension) },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Замены", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { showAddReplacementDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить замену")
                            }
                        }
                        state.replacements.forEach { replacement ->
                            ReplacementItem(
                                replacement = replacement,
                                onDelete = { viewModel.removeReplacement(replacement) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddExtensionDialog) {
        var extension by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddExtensionDialog = false },
            title = { Text("Добавить расширение") },
            text = {
                OutlinedTextField(
                    value = extension,
                    onValueChange = { extension = it },
                    label = { Text("Расширение файла") },
                    placeholder = { Text("Например: .txt") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addFileExtension(extension)
                        showAddExtensionDialog = false
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExtensionDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showAddReplacementDialog) {
        var searchPattern by remember { mutableStateOf("") }
        var replacement by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddReplacementDialog = false },
            title = { Text("Добавить замену") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchPattern,
                        onValueChange = { searchPattern = it },
                        label = { Text("Искать") },
                        placeholder = { Text("RegExp паттерн") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = replacement,
                        onValueChange = { replacement = it },
                        label = { Text("Заменить на") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addReplacement(searchPattern, replacement)
                        showAddReplacementDialog = false
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReplacementDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ReplacementItem(
    replacement: TextReplacement,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Искать: ${replacement.searchPattern}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Заменить на: ${replacement.replacement}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
} 