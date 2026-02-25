package presentation.screens.push

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushNotificationDetailScreen(
    configId: Long,
    viewModel: PushNotificationViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.detailState.collectAsState()
    var showAddDeviceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(configId) {
        viewModel.loadConfigDetails(configId)
    }

    Scaffold(
        topBar = { TopBar(title = uiState.config?.name ?: "Details", onBackClick = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Push Payload Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Push Payload", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = uiState.pushTitle,
                            onValueChange = { viewModel.updatePushTitle(it) },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.pushBody,
                            onValueChange = { viewModel.updatePushBody(it) },
                            label = { Text("Body") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Data Payload Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Data Payload (Extra fields)", style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { viewModel.loadExampleData() }) {
                                    Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Example")
                                }
                                TextButton(onClick = { viewModel.resetDataPayload() }) {
                                    Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reset")
                                }
                                TextButton(onClick = { viewModel.addDataField() }) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add Field")
                                }
                            }
                        }

                        uiState.pushData.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = key,
                                    onValueChange = { newKey -> viewModel.updateDataField(key, newKey, value) },
                                    label = { Text("Key") },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { newValue -> viewModel.updateDataField(key, key, newValue) },
                                    label = { Text("Value") },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                IconButton(onClick = { viewModel.removeDataField(key) }) {
                                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // Devices Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Device Tokens", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.sendPushToAll() },
                            enabled = uiState.devices.isNotEmpty() && !uiState.isSending
                        ) {
                            Icon(Icons.Default.Send, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Send to All")
                        }
                        Button(onClick = { showAddDeviceDialog = true }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Add Device")
                        }
                    }
                }
            }

            if (uiState.devices.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No devices added.")
                    }
                }
            } else {
                items(uiState.devices) { device ->
                    DeviceCard(
                        device = device,
                        onSend = { viewModel.sendPush(device) },
                        onDelete = { viewModel.deleteDevice(device) }
                    )
                }
            }

            // Status Section
            if (uiState.isSending || uiState.lastResponse != null || uiState.error != null) {
                item {
                    Surface(
                        color = when {
                            uiState.error != null -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (uiState.isSending) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Sending push...")
                                }
                            }
                            uiState.lastResponse?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            uiState.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }

        if (showAddDeviceDialog) {
            AddDeviceDialog(
                onDismiss = { showAddDeviceDialog = false },
                onAdd = { name, token ->
                    viewModel.addDevice(configId, name, token)
                    showAddDeviceDialog = false
                }
            )
        }
    }
}

@Composable
fun DeviceCard(
    device: data.local.entities.PushDeviceEntity,
    onSend: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, style = MaterialTheme.typography.titleSmall)
                Text(text = device.token, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
            Button(onClick = onSend) {
                Icon(Icons.Default.Send, null)
                Spacer(Modifier.width(4.dp))
                Text("1") // Default count 1 as requested
            }
        }
    }
}

@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Device Token") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Device Name/Model") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("FCM Token") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, token) },
                enabled = name.isNotBlank() && token.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
