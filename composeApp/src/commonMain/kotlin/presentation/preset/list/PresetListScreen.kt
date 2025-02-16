package presentation.preset.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Preset
import moe.tlaster.precompose.koin.koinViewModel
import moe.tlaster.precompose.viewmodel.viewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetListScreen(
    onNavigateToEdit: (Long?) -> Unit,
    //viewModel: PresetListViewModel = koinInject()
    viewModel: PresetListViewModel = koinViewModel()

) {

    val presets by viewModel.presets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Пресеты замены текста") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEdit(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить пресет")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(presets) { preset ->
                PresetItem(
                    preset = preset,
                    onPresetClick = { onNavigateToEdit(preset.id) },
                    onDeleteClick = { viewModel.deletePreset(preset) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetItem(
    preset: Preset,
    onPresetClick: () -> Unit,
    onDeleteClick: () -> Unit,
    viewModel: PresetListViewModel
) {
    val isProcessing by viewModel.isProcessing.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onPresetClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Папка: ${preset.targetDirectory}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Расширения: ${preset.fileExtensions.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.applyPreset(preset) }) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Применить пресет",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        enabled = !isProcessing
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить пресет"
                        )
                    }
                }
            }
        }
    }
} 