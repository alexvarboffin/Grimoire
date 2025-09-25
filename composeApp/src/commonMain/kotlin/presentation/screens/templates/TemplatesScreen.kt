package presentation.screens.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import moe.tlaster.precompose.navigation.Navigator
import navigation.NavGraph
import presentation.components.TopBar
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(navigator: Navigator, onNavigateBack: () -> Unit) {
    val viewModel: TemplatesViewModel = koinInject()
    val files by viewModel.files
    val currentPath by viewModel.currentPath

    Scaffold(
        topBar = {
            TopBar(
                title = currentPath,
                onBackClick = onNavigateBack,
                actions = {
                    if (viewModel.currentPath.value.equals(viewModel.rootPath, ignoreCase = true).not()) {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Up")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            items(files) { file ->
                Row(
                    modifier = Modifier
                        .clickable {
                            if (file.isDirectory) {
                                viewModel.loadFiles(file.path)
                            } else {
                                val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                                navigator.navigate(NavGraph.TEMPLATE_EDIT_ROUTE.replace("{filePath}", encodedPath))
                            }
                        }
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                        contentDescription = null
                    )
                    Text(text = file.name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
