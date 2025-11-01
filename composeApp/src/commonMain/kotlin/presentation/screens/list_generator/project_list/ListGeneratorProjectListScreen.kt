package presentation.screens.list_generator.project_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.ListGeneratorProject
import org.koin.compose.koinInject
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListGeneratorProjectListScreen(
    onNavigateToProject: (Long?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: ListGeneratorProjectListViewModel = koinInject()
    val projects by viewModel.projects.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                title = "Проекты генератора списков",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToProject(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить проект")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(projects) { project ->
                ProjectItem(
                    project = project,
                    onProjectClick = { onNavigateToProject(project.id) },
                    onDeleteClick = { viewModel.deleteProject(project) },
                    onRenameClick = { newName -> viewModel.renameProject(project, newName) },
                    onCopyClick = { viewModel.copyProject(project) }
                )
            }
        }
    }
}

@Composable
private fun ProjectItem(
    project: ListGeneratorProject,
    onProjectClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: (String) -> Unit,
    onCopyClick: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        var newName by remember { mutableStateOf(project.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Переименовать проект") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Название проекта") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameClick(newName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Переименовать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onProjectClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = onCopyClick) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Создать копию"
                    )
                }
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Переименовать проект"
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить проект"
                    )
                }
            }
        }
    }
}
