package presentation.screens.templates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.core.parameter.parametersOf
import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditScreen(filePath: String, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel(TemplateEditViewModel::class) { parametersOf(filePath) }

    Scaffold(
        topBar = {
            TopBar(
                title = "Edit Template",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
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
    }
}