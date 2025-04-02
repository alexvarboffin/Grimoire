package presentation.screens.rest

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import presentation.components.TopBar
import util.JsonToKotlinGenerator

enum class ResponseTab {
    BODY, HEADERS, COOKIES
}

enum class CodeGenType {
    DATA_CLASS,
    RETROFIT_INTERFACE,
    KTOR_CLIENT,
    KTOR_CLIENT_RESULT,
    REPOSITORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestClientScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: RestClientViewModel = koinInject()
    val clipboardManager = LocalClipboardManager.current
    



    var url by remember { mutableStateOf("") }



    var method by remember { mutableStateOf("GET") }
    var headers by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var isMethodDropdownExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(ResponseTab.BODY) }
    var showCodeGenMenu by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    
    val state by viewModel.state.collectAsState()
    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")

    fun generateCode(type: CodeGenType) {
        val response = state.response?.body ?: return
        val className = "Response"
        val baseUrl = url.substringBefore("/", "")
        val path = url.substringAfter(baseUrl, "")
        
        val code = when (type) {
            CodeGenType.DATA_CLASS -> JsonToKotlinGenerator.generateDataClass(response, className)
            CodeGenType.RETROFIT_INTERFACE -> JsonToKotlinGenerator.generateRetrofitInterface(baseUrl, path, method, className)
            CodeGenType.KTOR_CLIENT -> JsonToKotlinGenerator.generateKtorClient(baseUrl, path, method, className)
            CodeGenType.KTOR_CLIENT_RESULT -> JsonToKotlinGenerator.generateKtorClientResult(baseUrl, path, method, className)
            CodeGenType.REPOSITORY -> JsonToKotlinGenerator.generateRepository(className)
        }
        
        clipboardManager.setText(AnnotatedString(code))
        showSnackbar = true
        snackbarMessage = "Код скопирован в буфер обмена"
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            // TODO: Show snackbar with error
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "REST Client",
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    showSnackbar = false
                }
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(snackbarMessage)
                }
            }
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(error)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Request Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // URL Input and Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("https://api.example.com/endpoint") }
                    )
                    
                    Button(
                        onClick = { 
                            viewModel.sendRequest(url, method, headers, body)
                        },
                        enabled = !state.isLoading,
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    }
                }
                
                // Method Selector
                ExposedDropdownMenuBox(
                    expanded = isMethodDropdownExpanded,
                    onExpandedChange = { isMethodDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = method,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Method") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isMethodDropdownExpanded,
                        onDismissRequest = { isMethodDropdownExpanded = false }
                    ) {
                        methods.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    method = item
                                    isMethodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Headers Input
                OutlinedTextField(
                    value = headers,
                    onValueChange = { headers = it },
                    label = { Text("Headers") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Content-Type: application/json\nAuthorization: Bearer token") }
                )
                
                // Body Input
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Request Body") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("{\n  \"key\": \"value\"\n}") }
                )
            }

            // Response Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Status Bar
                    state.response?.let { response ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ${response.statusCode}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = response.statusText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal
                    ) {
                        ResponseTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = { Text(tab.name) }
                            )
                        }
                    }

                    // Tab Content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        state.response?.let { response ->
                            when (selectedTab) {
                                ResponseTab.BODY -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        OutlinedTextField(
                                            value = response.body,
                                            onValueChange = { },
                                            readOnly = true,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        
                                        // Кнопка генерации кода
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                        ) {
                                            IconButton(onClick = { showCodeGenMenu = true }) {
                                                Icon(Icons.Default.Code, contentDescription = "Генерировать код")
                                            }
                                            
                                            DropdownMenu(
                                                expanded = showCodeGenMenu,
                                                onDismissRequest = { showCodeGenMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Сгенерировать @Serializable класс") },
                                                    onClick = {
                                                        generateCode(CodeGenType.DATA_CLASS)
                                                        showCodeGenMenu = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.DataObject, contentDescription = null)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Сгенерировать Retrofit интерфейс") },
                                                    onClick = {
                                                        generateCode(CodeGenType.RETROFIT_INTERFACE)
                                                        showCodeGenMenu = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Api, contentDescription = null)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Сгенерировать Ktor клиент") },
                                                    onClick = {
                                                        generateCode(CodeGenType.KTOR_CLIENT)
                                                        showCodeGenMenu = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Http, contentDescription = null)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Сгенерировать Ktor клиент (Result)") },
                                                    onClick = {
                                                        generateCode(CodeGenType.KTOR_CLIENT_RESULT)
                                                        showCodeGenMenu = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Http, contentDescription = null)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Сгенерировать Repository") },
                                                    onClick = {
                                                        generateCode(CodeGenType.REPOSITORY)
                                                        showCodeGenMenu = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Storage, contentDescription = null)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                ResponseTab.HEADERS -> {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(response.headers.entries.toList()) { (key, values) ->
                                            Column {
                                                Text(
                                                    text = key,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                values.forEach { value ->
                                                    Text(
                                                        text = value,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                ResponseTab.COOKIES -> {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(response.cookies.entries.toList()) { (key, value) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = key,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = value,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 