package presentation.preset.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import domain.model.TextReplacement
import moe.tlaster.precompose.koin.koinViewModel
import moe.tlaster.precompose.viewmodel.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PresetEditScreen(
    presetId: Long?,
    onNavigateBack: () -> Unit,
    
    viewModel: PresetEditViewModel = koinViewModel { parametersOf(presetId) }
) {
    val state by viewModel.state.collectAsState()
    var showAddExtensionDialog by remember { mutableStateOf(false) }
    var showAddReplacementDialog by remember { mutableStateOf(false) }
    val stats by viewModel.processingStats.collectAsState()

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
                    val isProcessing by viewModel.isProcessing.collectAsState()
                    
                    IconButton(
                        onClick = { viewModel.importPreset() },
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Импортировать")
                    }
                    
                    IconButton(
                        onClick = { viewModel.exportPreset() },
                        enabled = !isProcessing && state.name.isNotBlank()
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Экспортировать")
                    }
                    
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.applyPreset() }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Применить пресет")
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.savePreset()
                            onNavigateBack()
                        },
                        enabled = !isProcessing
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
                    modifier = Modifier.fillMaxWidth()
                        .onDragAndDrop { file -> viewModel.handleDirectoryDrop(file) },
                    trailingIcon = {
                        Row {
                            if (state.targetDirectory.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearTargetDirectory() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                }
                            }
                            IconButton(onClick = { viewModel.selectDirectory() }) {
                                Icon(Icons.Default.Folder, contentDescription = "Выбрать папку")
                            }
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
                                onDelete = { viewModel.removeReplacement(replacement) },
                                viewModel = viewModel
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
        var isRegex by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddReplacementDialog = false },
            title = { Text("Добавить замену") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Тип замены:")
                        Switch(
                            checked = isRegex,
                            onCheckedChange = { isRegex = it },
                            thumbContent = if (isRegex) {
                                { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else {
                                { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            }
                        )
                    }
                    Text(
                        text = if (isRegex) "RegExp" else "Обычный текст",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchPattern,
                        onValueChange = { searchPattern = it },
                        label = { Text("Искать") },
                        placeholder = { Text(if (isRegex) "RegExp паттерн" else "Текст для поиска") },
                        supportingText = if (isRegex) { 
                            { 
                                Column {
                                    Text("Специальные символы нужно экранировать дважды:")
                                    Text("Одинарная кавычка: \\'")
                                    Text("Двойная кавычка: \\\"")
                                    Text("Обратный слеш: \\\\")
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = replacement,
                        onValueChange = { replacement = it },
                        label = { Text("Заменить на") },
                        placeholder = { Text(if (isRegex) "Например: id(\"$1\")" else "Текст замены") },
                        supportingText = if (isRegex) {
                            { Text("Используйте \$1, \$2... для групп захвата") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isRegex) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Шаблоны замен:",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FilledTonalButton(
                                    onClick = {
                                        searchPattern = "id \\'(.*)\\'"
                                        replacement = "id(\"$1\")"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gradle: id '...' → id(\"...\")")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addReplacement(searchPattern, replacement, isRegex)
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

    stats?.let { processingStats ->
        AlertDialog(
            onDismissRequest = { viewModel.clearStats() },
            title = { Text("Статистика замен") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Обработано файлов: ${processingStats.processedFiles}")
                    Text("Всего замен: ${processingStats.totalReplacements}")
                    
                    if (processingStats.fileStats.isNotEmpty()) {
                        Text("Детали по файлам:", style = MaterialTheme.typography.titleSmall)
                        processingStats.fileStats.forEach { (file, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = file,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearStats() }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
private fun ReplacementItem(
    replacement: TextReplacement,
    onDelete: () -> Unit,
    viewModel: PresetEditViewModel
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        if (replacement.isRegex) Icons.Default.Code else Icons.Default.TextFields,
                        contentDescription = if (replacement.isRegex) "RegExp" else "Текст",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = replacement.searchPattern,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Заменить на",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = replacement.replacement,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        var searchPattern by remember { mutableStateOf(replacement.searchPattern) }
        var replacementText by remember { mutableStateOf(replacement.replacement) }
        var isRegex by remember { mutableStateOf(replacement.isRegex) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редактировать замену") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Тип замены:")
                        Switch(
                            checked = isRegex,
                            onCheckedChange = { isRegex = it },
                            thumbContent = if (isRegex) {
                                { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else {
                                { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            }
                        )
                    }
                    Text(
                        text = if (isRegex) "RegExp" else "Обычный текст",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchPattern,
                        onValueChange = { searchPattern = it },
                        label = { Text("Искать") },
                        placeholder = { Text(if (isRegex) "RegExp паттерн" else "Текст для поиска") },
                        supportingText = if (isRegex) { 
                            { 
                                Column {
                                    Text("Специальные символы нужно экранировать дважды:")
                                    Text("Одинарная кавычка: \\'")
                                    Text("Двойная кавычка: \\\"")
                                    Text("Обратный слеш: \\\\")
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = replacementText,
                        onValueChange = { replacementText = it },
                        label = { Text("Заменить на") },
                        placeholder = { Text(if (isRegex) "Например: id(\"$1\")" else "Текст замены") },
                        supportingText = if (isRegex) {
                            { Text("Используйте \$1, \$2... для групп захвата") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isRegex) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Шаблоны замен:",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FilledTonalButton(
                                    onClick = {
                                        searchPattern = "id \\'(.*)\\'"
                                        replacementText = "id(\"$1\")"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gradle: id '...' → id(\"...\")")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateReplacement(replacement, searchPattern, replacementText, isRegex)
                        showEditDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun Modifier.onDragAndDrop(onDrop: (File) -> Unit): Modifier = composed {
    val window = ComposeWindow()
    DisposableEffect(window) {
        val dropTarget = DropTarget()
        dropTarget.addDropTargetListener(object : DropTargetAdapter() {
            override fun drop(event: DropTargetDropEvent) {
                event.acceptDrop(DnDConstants.ACTION_COPY)
                val transferable = event.transferable
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    @Suppress("UNCHECKED_CAST")
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                    files.firstOrNull()?.let(onDrop)
                }
                event.dropComplete(true)
            }
        })
        window.contentPane.dropTarget = dropTarget
        onDispose {
            window.contentPane.dropTarget = null
        }
    }
    this
} 