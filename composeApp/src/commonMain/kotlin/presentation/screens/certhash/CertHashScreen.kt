package presentation.screens.certhash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import domain.certificate.CertificateInfo
import presentation.components.SingleFilePicker
import presentation.components.TopBar
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertHashScreen(
    onNavigateBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val viewModel: CertHashViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    var showFilePicker by remember { mutableStateOf(false) }

    LaunchedEffect(clipboardManager) {
        viewModel.setClipboardManager(clipboardManager)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Certificate Hash Grabber",
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = {
            if (uiState.showCopiedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Скопировано в буфер обмена")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hostname input
            OutlinedTextField(
                value = uiState.hostname,
                onValueChange = viewModel::updateHostname,
                label = { Text("Hostname (e.g. api.ktor.io)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Language, contentDescription = null)
                }
            )

            // Output file selection
            Button(
                onClick = { showFilePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Выбрать путь для сохранения")
            }
            
            SingleFilePicker(
                show = showFilePicker,
                fileExtensions = listOf("txt"),
                onFileSelected = { mpFile ->
                    showFilePicker = false
                    mpFile?.path?.let { viewModel.updateOutputPath(it) }
                }
            )

            // Current output path
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Путь сохранения: ${uiState.outputPath}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Grab certificates button
            Button(
                onClick = { viewModel.grabCertificates() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.hostname.isNotBlank() && !uiState.isLoading
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Получить сертификаты")
            }

            // Error message
            uiState.error?.let { error ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Certificates list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.certificates) { cert ->
                    CertificateCard(cert, viewModel::copyToClipboard)
                }
            }
        }
    }
}

@Composable
private fun CertificateCard(
    cert: CertificateInfo,
    onCopyHash: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null)
                    Text(
                        "Сертификат",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Копировать хэш") },
                            onClick = {
                                onCopyHash(cert.pinHash)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Копировать всё") },
                            onClick = {
                                onCopyHash("""
                                    Subject: ${cert.subject}
                                    Issuer: ${cert.issuer}
                                    Valid from: ${cert.validFrom}
                                    Valid until: ${cert.validUntil}
                                    Pin hash: ${cert.pinHash}
                                """.trimIndent())
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.FileCopy, contentDescription = null)
                            }
                        )
                    }
                }
            }

            Divider()

            // Certificate details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Text("Субъект: ${cert.subject}")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Business, contentDescription = null)
                Text("Издатель: ${cert.issuer}")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null)
                Text("Действителен с: ${cert.validFrom}")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Event, contentDescription = null)
                Text("Действителен до: ${cert.validUntil}")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Key, contentDescription = null)
                Text("Хэш для пиннинга: ${cert.pinHash}")
            }
        }
    }
} 