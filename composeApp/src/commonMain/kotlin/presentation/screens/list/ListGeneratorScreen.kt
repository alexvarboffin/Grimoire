package presentation.screens.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListGeneratorScreen(projectId: Long?, onNavigateBack: () -> Unit) {
    val viewModel: ListGeneratorViewModel = koinInject()
    val uiState by viewModel.uiState
    var showSaveProjectDialog by remember { mutableStateOf(false) }

    if (showSaveProjectDialog) {
        var projectName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveProjectDialog = false },
            title = { Text("Сохранить проект") },
            text = {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Название проекта") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveProject(projectName)
                        showSaveProjectDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProjectDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Генератор списков",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Left Panel - Configuration
            Column(
                modifier = Modifier.weight(1f).padding(end = 8.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TemplateSelector(viewModel, uiState)

                if (uiState.selectedTemplate != null) {
                    VariableFields(viewModel, uiState)
                }
            }

            // Right Panel - Output
            Column(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { viewModel.generate() }, modifier = Modifier.weight(1f)) { Text("Сгенерировать") }
                    Button(onClick = { viewModel.save() }, modifier = Modifier.weight(1f)) { Text("Сохранить") }
                    Button(onClick = { showSaveProjectDialog = true }, modifier = Modifier.weight(1f)) { Text("Сохранить проект") }
                }
                TextField(
                    value = uiState.errorMessage ?: uiState.output,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Результат") },
                    isError = uiState.errorMessage != null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateSelector(viewModel: ListGeneratorViewModel, uiState: ListGeneratorUiState) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            value = uiState.selectedTemplate?.name ?: "Выберите шаблон",
            onValueChange = {},
            readOnly = true,
            label = { Text("Шаблон") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            uiState.templates.forEach { template ->
                DropdownMenuItem(
                    text = { Text(template.name) },
                    onClick = {
                        viewModel.onTemplateSelected(template)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun VariableFields(viewModel: ListGeneratorViewModel, uiState: ListGeneratorUiState) {
    uiState.allVariables.forEach { variableName ->
        if (uiState.listVariables.contains(variableName)) {
            val list = viewModel.variableValues[variableName]?.listValue as? SnapshotStateList<String>
            if (list != null) {
                ListItemEditor(viewModel, listName = variableName, listItems = list)
            }
        } else {
            val value = viewModel.variableValues[variableName]?.stringValue
            if (value != null) {
                TextField(
                    value = value,
                    onValueChange = { viewModel.onSimpleVariableChanged(variableName, it) },
                    label = { Text(variableName) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ListItemEditor(viewModel: ListGeneratorViewModel, listName: String, listItems: SnapshotStateList<String>) {
    var showFilePicker by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(listName, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("Новый элемент") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showFilePicker = true }) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Импорт")
                }
                IconButton(onClick = { 
                    viewModel.addListItem(listName, newItemText)
                    newItemText = ""
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                }
            }
            // This LazyColumn inside a verticalScroll is not ideal, but for a limited number of items it's fine.
            // A fixed height would be better for performance if lists get long.
            Column {
                listItems.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item, modifier = Modifier.weight(1f).padding(start = 8.dp))
                        IconButton(onClick = { viewModel.removeListItem(listName, item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            }
        }
    }

    FilePicker(showFilePicker, fileExtensions = listOf("txt")) { file ->
        showFilePicker = false
        if (file != null) {
            viewModel.addListItemsFromFile(listName, file.path)
        }
    }
}