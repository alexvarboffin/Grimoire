package presentation.screens.batch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import org.koin.compose.koinInject
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchGeneratorScreen(onNavigateBack: () -> Unit) {
    val viewModel: BatchGeneratorViewModel = koinInject()
    val uiState by viewModel.uiState
    val batchItems = viewModel.batchItems
    val otherVariableValues by viewModel.otherVariableValues
    var newItemText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopBar(
                title = "Мульти-генератор",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Left Panel - Configuration
            Column(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TemplateSelector(viewModel, uiState)
                if (uiState.selectedTemplate != null) {
                    OtherVariablesFields(viewModel, uiState, otherVariableValues)
                    BatchListEditor(viewModel, batchItems, newItemText, onNewItemTextChange = { newItemText = it })
                }
            }

            // Right Panel - Output
            Column(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { viewModel.generate() }, modifier = Modifier.weight(1f)) { Text("Сгенерировать") }
                    Button(onClick = { viewModel.save() }, modifier = Modifier.weight(1f)) { Text("Сохранить все") }
                }
                TextField(
                    value = uiState.errorMessage ?: uiState.logOutput,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Лог") },
                    isError = uiState.errorMessage != null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateSelector(viewModel: BatchGeneratorViewModel, uiState: BatchGeneratorUiState) {
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
private fun OtherVariablesFields(viewModel: BatchGeneratorViewModel, uiState: BatchGeneratorUiState, otherVariableValues: Map<String, String>) {
    uiState.templateVariables.forEach { variable ->
        TextField(
            value = otherVariableValues[variable] ?: "",
            onValueChange = { viewModel.onOtherVariableChanged(variable, it) },
            label = { Text(variable) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BatchListEditor(viewModel: BatchGeneratorViewModel, batchItems: List<String>, newItemText: String, onNewItemTextChange: (String) -> Unit) {
    var showFilePicker by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = newItemText,
                onValueChange = onNewItemTextChange,
                label = { Text("Имя элемента") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showFilePicker = true }) {
                Icon(Icons.Default.UploadFile, contentDescription = "Импорт")
            }
            IconButton(onClick = { 
                viewModel.addBatchItem(newItemText)
                onNewItemTextChange("")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
        LazyColumn {
            items(batchItems) { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.removeBatchItem(item) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }
        }
    }

    FilePicker(showFilePicker, fileExtensions = listOf("txt")) { file ->
        showFilePicker = false
        if (file != null) {
            viewModel.addBatchItemsFromFile(file.path)
        }
    }
}