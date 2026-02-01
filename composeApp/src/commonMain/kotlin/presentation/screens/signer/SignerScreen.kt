package presentation.screens.signer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import presentation.components.TopBar
import presentation.components.SingleFilePicker

@Composable
fun SignerScreen(
    viewModel: SignerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    
    var javaPath by remember(uiState.javaPath) { mutableStateOf(uiState.javaPath) }
    var jarPath by remember(uiState.jarPath) { mutableStateOf(uiState.jarPath) }
    var apkPath by remember(uiState.apkPath) { mutableStateOf(uiState.apkPath) }

    var showJavaPicker by remember { mutableStateOf(false) }
    var showJarPicker by remember { mutableStateOf(false) }
    var showApkPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(title = "Uber APK Signer", onBackClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = javaPath, onValueChange = { javaPath = it }, label = { Text("Java EXE Path") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showJavaPicker = true }) { Icon(Icons.Default.FolderOpen, null) } })
            
            OutlinedTextField(value = jarPath, onValueChange = { jarPath = it }, label = { Text("uber-apk-signer JAR Path") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showJarPicker = true }) { Icon(Icons.Default.FolderOpen, null) } })
            
            OutlinedTextField(value = apkPath, onValueChange = { apkPath = it }, label = { Text("APK File to Sign") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showApkPicker = true }) { Icon(Icons.Default.FolderOpen, null) } })

            Button(
                onClick = { 
                    viewModel.updateSettings(javaPath, jarPath, apkPath)
                    viewModel.sign() 
                },
                enabled = !uiState.isSigning,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSigning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Sign APK")
            }

            if (uiState.logs.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Logs:", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { clipboardManager.setText(AnnotatedString(uiState.logs)) }) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy")
                    }
                }
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 400.dp), shape = MaterialTheme.shapes.medium) {
                    Text(text = uiState.logs, modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    SingleFilePicker(showJavaPicker, fileExtensions = listOf("exe")) { file -> showJavaPicker = false; file?.path?.let { javaPath = it } }
    SingleFilePicker(showJarPicker, fileExtensions = listOf("jar")) { file -> showJarPicker = false; file?.path?.let { jarPath = it } }
    SingleFilePicker(showApkPicker, fileExtensions = listOf("apk")) { file -> showApkPicker = false; file?.path?.let { apkPath = it } }
}
