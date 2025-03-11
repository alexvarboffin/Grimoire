package presentation.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import navigation.NavGraph.CERT_HASH_ROUTE
import navigation.NavGraph.PRESET_LIST_ROUTE
import navigation.NavGraph.TOML_MERGER_ROUTE
import navigation.NavGraph.REST_CLIENT_ROUTE
import navigation.NavGraph.PACKAGE_MANAGER_ROUTE
import presentation.components.TopBar

data class Tool(
    val name: String,
    val description: String,
    val route: String,
    val icon: ImageVector
)

private val tools = listOf(
    Tool(
        name = "Cert Hash Grabber",
        description = "Получение хэшей SSL сертификатов для пиннинга",
        route = CERT_HASH_ROUTE,
        icon = Icons.Default.Security
    ),
    Tool(
        name = "Text Presets",
        description = "Управление пресетами для замены текста",
        route = PRESET_LIST_ROUTE,
        icon = Icons.Default.TextFields
    ),
    Tool(
        name = "TOML Merger",
        description = "Слияние TOML файлов с версиями зависимостей",
        route = TOML_MERGER_ROUTE,
        icon = Icons.Default.Merge
    ),
    Tool(
        name = "REST Client",
        description = "Отправка HTTP запросов и тестирование API",
        route = REST_CLIENT_ROUTE,
        icon = Icons.Default.Api
    ),
    Tool(
        name = "Package Manager",
        description = "Управление приложениями через ADB",
        route = PACKAGE_MANAGER_ROUTE,
        icon = Icons.Default.Refresh
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateToTool: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                title = "ByteBreakerz Tools",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 250.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tools) { tool ->
                ToolCard(
                    tool = tool,
                    onClick = { onNavigateToTool(tool.route) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolCard(
    tool: Tool,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 