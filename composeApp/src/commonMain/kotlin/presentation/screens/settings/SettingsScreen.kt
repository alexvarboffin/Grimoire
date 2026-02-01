package presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import org.koin.compose.koinInject
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: SettingsViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    var showDirPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                title = "Настройки",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Тема
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
                        text = "Внешний вид",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (uiState.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null
                            )
                            Text("Темная тема")
                        }
                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) }
                        )
                    }
                }
            }

            // Шаблоны
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
                        text = "Шаблоны",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    TextField(
                        value = uiState.savePath,
                        onValueChange = { viewModel.setSavePath(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Путь для сохранения") },
                        trailingIcon = {
                            IconButton(onClick = { showDirPicker = true }) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "Выбрать папку")
                            }
                        }
                    )
                }
            }

            // Глобальные переменные
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Глобальные переменные", style = MaterialTheme.typography.titleMedium)
                    Text("Используйте их в командах как {KEY}", style = MaterialTheme.typography.bodySmall)
                    
                    uiState.globalVariables.forEach { (k, v) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = k, onValueChange = {}, readOnly = true, modifier = Modifier.weight(1f), label = { Text("Ключ") })
                            OutlinedTextField(value = v, onValueChange = { viewModel.updateGlobalVar(k, it) }, modifier = Modifier.weight(1f), label = { Text("Значение") })
                            IconButton(onClick = { viewModel.deleteGlobalVar(k) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                    
                    var newKey by remember { mutableStateOf("") }
                    var newValue by remember { mutableStateOf("") }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newKey, onValueChange = { newKey = it }, modifier = Modifier.weight(1f), label = { Text("Новый ключ") })
                        OutlinedTextField(value = newValue, onValueChange = { newValue = it }, modifier = Modifier.weight(1f), label = { Text("Значение") })
                        IconButton(onClick = { 
                            if (newKey.isNotBlank()) {
                                viewModel.updateGlobalVar(newKey, newValue)
                                newKey = ""; newValue = ""
                            }
                        }) { Icon(Icons.Default.Add, null) }
                    }
                }
            }

            // Информация о приложении
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
                        text = "О приложении",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Text("Версия: ${uiState.appVersion}")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                        Text("Устройство: ${uiState.deviceInfo}")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null)
                        Text("ОС: ${uiState.osInfo}")
                    }
                }
            }
        }
    }

    DirectoryPicker(showDirPicker) {
        showDirPicker = false
        if (it != null) {
            viewModel.setSavePath(it)
        }
    }
}