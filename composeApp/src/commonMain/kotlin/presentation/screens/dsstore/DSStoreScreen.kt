package presentation.screens.dsstore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import presentation.components.SingleFilePicker
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSStoreScreen(onNavigateBack: () -> Unit) {
    val viewModel: DSStoreViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    var showFilePicker by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopBar(
                title = ".DS_Store Parser",
                onBackClick = onNavigateBack
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL input and button
            var url by remember { mutableStateOf("") }
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Enter .DS_Store file URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.parseFromUrl(url) }) {
                Text("Parse from URL")
            }

            // Local file picker button
            Button(onClick = { showFilePicker = true }) {
                Text("Parse from Local File")
            }

            SingleFilePicker(
                show = showFilePicker,
                fileExtensions = listOf("DS_Store", ""),
                onFileSelected = { mpFile ->
                    showFilePicker = false
                    mpFile?.path?.let { viewModel.parseFromFile(it) }
                }
            )

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            // Error message
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Results
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.records) { record ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(record.name, style = MaterialTheme.typography.titleMedium)
                                if (uiState.baseUrl != null) {
                                    val fullUrl = uiState.baseUrl + record.name
                                    Row {
                                        IconButton(onClick = { uriHandler.openUri(fullUrl) }) {
                                            Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in browser")
                                        }
                                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(fullUrl)) }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL")
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            record.fields.forEach { (key, value) ->
                                Text("$key: ${record.humanReadable(key, value)}")
                            }
                        }
                    }
                }
            }
        }
    }
}
