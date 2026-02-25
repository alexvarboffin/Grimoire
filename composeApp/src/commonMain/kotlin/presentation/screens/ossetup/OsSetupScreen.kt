package presentation.screens.ossetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import presentation.components.TopBar

@Composable
fun OsSetupScreen(
    viewModel: OsSetupViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                title = "New OS Setup",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Commands List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Developer Tools (Winget)", style = MaterialTheme.typography.titleLarge)
                    Button(
                        onClick = { viewModel.runAll() },
                        enabled = !uiState.isRunningAll
                    ) {
                        if (uiState.isRunningAll) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PlayArrow, null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Run All")
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.items) { item ->
                        CommandItemRow(item, onRun = { viewModel.runCommand(item) })
                    }
                }
            }

            // Logs
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Logs", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = uiState.logs,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandItemRow(
    item: OsSetupItem,
    onRun: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.command, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = when (item.status) {
                        SetupStatus.IDLE -> "Idle"
                        SetupStatus.RUNNING -> "Installing..."
                        SetupStatus.SUCCESS -> "Installed"
                        SetupStatus.FAILED -> "Failed"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (item.status) {
                        SetupStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                        SetupStatus.RUNNING -> MaterialTheme.colorScheme.primary
                        SetupStatus.SUCCESS -> Color(0xFF4CAF50)
                        SetupStatus.FAILED -> MaterialTheme.colorScheme.error
                    }
                )
            }
            
            IconButton(
                onClick = onRun,
                enabled = item.status != SetupStatus.RUNNING
            ) {
                when (item.status) {
                    SetupStatus.RUNNING -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    SetupStatus.SUCCESS -> Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50))
                    else -> Icon(Icons.Default.PlayArrow, null)
                }
            }
        }
    }
}
