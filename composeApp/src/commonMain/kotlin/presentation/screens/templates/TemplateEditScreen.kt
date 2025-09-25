package presentation.screens.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.core.parameter.parametersOf
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditScreen(filePath: String, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel(TemplateEditViewModel::class) { parametersOf(filePath) }
    val renderedOutput by viewModel.renderedOutput
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopBar(
                title = "Edit Template",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.variables.keys.toList()) { key ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = key)
                        TextField(
                            value = viewModel.variables[key] ?: "",
                            onValueChange = { viewModel.onVariableChange(key, it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.render() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Применить")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = renderedOutput,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().weight(1f),
                label = { Text("Результат") }
            )

            IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(renderedOutput)) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy to clipboard")
            }
        }
    }
}