package presentation.screens.packageManager

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import domain.model.AndroidApp
import domain.model.AndroidDevice
import org.koin.compose.koinInject
import presentation.components.TopBar
import androidx.compose.foundation.layout.FlowRow
import domain.model.AndroidComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageManagerScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: PackageManagerViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    var showDetailsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                title = "Package Manager",
                onBackClick = onNavigateBack,
                actions = {
                    // ADB Server control
                    IconButton(
                        onClick = { 
                            if (uiState.isAdbRunning) viewModel.stopAdb()
                            else viewModel.startAdb()
                        }
                    ) {
                        Icon(
                            if (uiState.isAdbRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isAdbRunning) "Stop ADB" else "Start ADB"
                        )
                    }
                    // Refresh devices
                    IconButton(
                        onClick = { viewModel.refreshDevices() },
                        enabled = uiState.isAdbRunning
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
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
            // Device selection
            DeviceSelector(
                devices = uiState.devices,
                selectedDevice = uiState.selectedDevice,
                onDeviceSelected = viewModel::selectDevice,
                enabled = uiState.isAdbRunning && !uiState.isLoading
            )

            // Search and filters
            if (uiState.selectedDevice != null) {
                SearchAndFilters(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    showSystemApps = uiState.showSystemApps,
                    onShowSystemAppsChange = { viewModel.toggleSystemApps() },
                    sortOrder = uiState.sortOrder,
                    onSortOrderChange = viewModel::setSortOrder
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Error message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Apps list
            if (!uiState.isLoading && uiState.selectedDevice != null) {
                AppsList(
                    apps = uiState.apps.filter { app ->
                        val matchesSearch = app.appName.contains(uiState.searchQuery, ignoreCase = true) ||
                                          app.packageName.contains(uiState.searchQuery, ignoreCase = true)
                        val matchesFilter = uiState.showSystemApps || !app.isSystemApp
                        matchesSearch && matchesFilter
                    }.sortedWith(when (uiState.sortOrder) {
                        SortOrder.NAME -> compareBy { it.appName }
                        SortOrder.INSTALL_DATE -> compareByDescending { it.installTime }
                        SortOrder.UPDATE_DATE -> compareByDescending { it.updateTime }
                        SortOrder.PACKAGE_NAME -> compareBy { it.packageName }
                    }),
                    onAppClick = { app ->
                        viewModel.selectApp(app)
                        showDetailsDialog = true
                    },
                    onUninstall = { app -> 
                        viewModel.selectApp(app)
                        viewModel.uninstallSelectedApp()
                    },
                    onClearData = { app ->
                        viewModel.selectApp(app)
                        viewModel.clearSelectedAppData()
                    },
                    onForceStop = { app ->
                        viewModel.selectApp(app)
                        viewModel.forceStopSelectedApp()
                    },
                    onOpenInGooglePlay = { app ->
                        viewModel.selectApp(app)
                        viewModel.openInGooglePlay()
                    },
                    onExtractApk = { app ->
                        viewModel.selectApp(app)
                        viewModel.extractApk()
                    }
                )
            }
        }

        // Details dialog
        if (showDetailsDialog && uiState.selectedApp != null) {
            AppDetailsDialog(
                app = uiState.selectedApp!!,
                onDismiss = { showDetailsDialog = false },
                onLaunchActivity = { packageName ->
                    viewModel.launchActivity(packageName)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceSelector(
    devices: List<AndroidDevice>,
    selectedDevice: AndroidDevice?,
    onDeviceSelected: (AndroidDevice) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedDevice?.model ?: "Выберите устройство",
            onValueChange = { },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            devices.forEach { device ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(device.model)
                            Text(
                                text = device.id,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    onClick = {
                        onDeviceSelected(device)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            if (device.isEmulator) Icons.Default.PhoneAndroid
                            else Icons.Default.Smartphone,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSystemApps: Boolean,
    onShowSystemAppsChange: () -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск по имени или package") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showSystemApps,
                    onCheckedChange = { onShowSystemAppsChange() }
                )
                Text("Системные приложения")
            }

            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Сортировка")
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("По имени") },
                        onClick = { 
                            onSortOrderChange(SortOrder.NAME)
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("По дате установки") },
                        onClick = { 
                            onSortOrderChange(SortOrder.INSTALL_DATE)
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("По дате обновления") },
                        onClick = { 
                            onSortOrderChange(SortOrder.UPDATE_DATE)
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("По package name") },
                        onClick = { 
                            onSortOrderChange(SortOrder.PACKAGE_NAME)
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppsList(
    apps: List<AndroidApp>,
    onAppClick: (AndroidApp) -> Unit,
    onUninstall: (AndroidApp) -> Unit,
    onClearData: (AndroidApp) -> Unit,
    onForceStop: (AndroidApp) -> Unit,
    onOpenInGooglePlay: (AndroidApp) -> Unit,
    onExtractApk: (AndroidApp) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps) { app ->
            AppItem(
                app = app,
                onClick = { onAppClick(app) },
                onUninstall = { onUninstall(app) },
                onClearData = { onClearData(app) },
                onForceStop = { onForceStop(app) },
                onOpenInGooglePlay = { onOpenInGooglePlay(app) },
                onExtractApk = { onExtractApk(app) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppItem(
    app: AndroidApp,
    onClick: () -> Unit,
    onUninstall: () -> Unit,
    onClearData: () -> Unit,
    onForceStop: () -> Unit,
    onOpenInGooglePlay: () -> Unit,
    onExtractApk: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (app.isSystemApp) Icons.Default.Android else Icons.Default.Apps,
                contentDescription = null,
                tint = if (app.isSystemApp) 
                    MaterialTheme.colorScheme.tertiary 
                else 
                    MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "v${app.versionName} (${app.versionCode})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    if (!app.isSystemApp) {
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            onClick = { 
                                onUninstall()
                                showMenu = false 
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                    
                    DropdownMenuItem(
                        text = { Text("Очистить данные") },
                        onClick = { 
                            onClearData()
                            showMenu = false 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.CleaningServices, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Остановить") },
                        onClick = { 
                            onForceStop()
                            showMenu = false 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Stop, contentDescription = null)
                        }
                    )
                    
                    Divider()
                    
                    DropdownMenuItem(
                        text = { Text("Открыть в Google Play") },
                        onClick = { 
                            onOpenInGooglePlay()
                            showMenu = false 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Shop, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Извлечь APK") },
                        onClick = { 
                            onExtractApk()
                            showMenu = false 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Download, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppActionsDialog(
    app: AndroidApp,
    onDismiss: () -> Unit,
    onUninstall: () -> Unit,
    onClearData: () -> Unit,
    onForceStop: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(app.appName) },
        text = { 
            Column {
                Text(app.packageName)
                Text("Version: ${app.versionName} (${app.versionCode})")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Закрыть")
            }
        },
        dismissButton = {
            Row {
                if (!app.isSystemApp) {
                    TextButton(
                        onClick = onUninstall,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Удалить")
                    }
                }
                TextButton(
                    onClick = onClearData
                ) {
                    Text("Очистить данные")
                }
                TextButton(
                    onClick = onForceStop
                ) {
                    Text("Остановить")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailsDialog(
    app: AndroidApp,
    onDismiss: () -> Unit,
    onLaunchActivity: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (app.isSystemApp) Icons.Default.Android else Icons.Default.Apps,
                    contentDescription = null,
                    tint = if (app.isSystemApp) 
                        MaterialTheme.colorScheme.tertiary 
                    else 
                        MaterialTheme.colorScheme.primary
                )
                Text(app.appName)
            }
        },
        text = { 
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Basic Info Section
                SectionTitle("Основная информация")
                DetailRow(
                    icon = Icons.Default.Label,
                    label = "Имя пакета:",
                    value = app.packageName
                )
                DetailRow(
                    icon = Icons.Default.Info,
                    label = "Версия:",
                    value = "${app.versionName} (${app.versionCode})"
                )
                DetailRow(
                    icon = Icons.Default.Schedule,
                    label = "Установлено:",
                    value = formatDateTime(app.installTime)
                )
                DetailRow(
                    icon = Icons.Default.Update,
                    label = "Обновлено:",
                    value = formatDateTime(app.updateTime)
                )
                DetailRow(
                    icon = Icons.Default.PhoneAndroid,
                    label = "SDK:",
                    value = "Target: ${app.targetSdkVersion}, Min: ${app.minSdkVersion}"
                )

                // Badges Section
                SectionTitle("Характеристики")
                AppBadges(app)

                // Components Sections
                ComponentSection(
                    title = "Активности (${app.activities.size})",
                    components = app.activities,
                    icon = Icons.Default.Widgets,
                    packageName = app.packageName,
                    onLaunchActivity = onLaunchActivity
                )
                
                ComponentSection(
                    title = "Сервисы (${app.services.size})",
                    components = app.services,
                    icon = Icons.Default.Settings,
                    packageName = app.packageName
                )
                
                ComponentSection(
                    title = "Приемники (${app.receivers.size})",
                    components = app.receivers,
                    icon = Icons.Default.Radio,
                    packageName = app.packageName
                )
                
                ComponentSection(
                    title = "Провайдеры (${app.providers.size})",
                    components = app.providers,
                    icon = Icons.Default.Storage,
                    packageName = app.packageName
                )

                // Permissions Section
                if (app.permissions.isNotEmpty()) {
                    SectionTitle("Разрешения (${app.permissions.size})")
                    app.permissions.forEach { permission ->
                        PermissionItem(permission)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun PermissionItem(permission: String) {
    val (icon, description) = getPermissionInfo(permission)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = permission.substringAfterLast("."),
                style = MaterialTheme.typography.bodyMedium
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getPermissionInfo(permission: String): Pair<ImageVector, String?> {
    return when {
        permission.contains("INTERNET") -> 
            Icons.Default.Public to "Доступ в интернет"
        permission.contains("CAMERA") -> 
            Icons.Default.PhotoCamera to "Доступ к камере"
        permission.contains("LOCATION") -> 
            Icons.Default.LocationOn to "Доступ к геолокации"
        permission.contains("STORAGE") || permission.contains("EXTERNAL_STORAGE") -> 
            Icons.Default.Storage to "Доступ к хранилищу"
        permission.contains("CONTACTS") -> 
            Icons.Default.Contacts to "Доступ к контактам"
        permission.contains("MICROPHONE") || permission.contains("RECORD_AUDIO") -> 
            Icons.Default.Mic to "Доступ к микрофону"
        permission.contains("PHONE") -> 
            Icons.Default.Phone to "Доступ к телефону"
        permission.contains("SMS") -> 
            Icons.Default.Sms to "Доступ к SMS"
        permission.contains("CALENDAR") -> 
            Icons.Default.CalendarMonth to "Доступ к календарю"
        permission.contains("BLUETOOTH") -> 
            Icons.Default.Bluetooth to "Доступ к Bluetooth"
        permission.contains("NOTIFICATION") -> 
            Icons.Default.Notifications to "Доступ к уведомлениям"
        permission.contains("WAKE_LOCK") -> 
            Icons.Default.PowerSettingsNew to "Управление питанием"
        permission.contains("FOREGROUND_SERVICE") -> 
            Icons.Default.AppShortcut to "Фоновая служба"
        else -> Icons.Default.Lock to null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppBadges(app: AndroidApp) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (app.isSystemApp) {
            AppBadge(
                text = "System",
                icon = Icons.Default.Android,
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
        
        if (app.isUpdatedSystemApp) {
            AppBadge(
                text = "Updated System",
                icon = Icons.Default.Update,
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        
        if (app.hasSslPinning) {
            AppBadge(
                text = "SSL Pinning",
                icon = Icons.Default.Security,
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }
        
        if (app.isDebugApp) {
            AppBadge(
                text = "Debug",
                icon = Icons.Default.BugReport,
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
        
        if (app.isTestOnly) {
            AppBadge(
                text = "Test Only",
                icon = Icons.Default.Science,
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        
        if (app.hasNativeLibs) {
            AppBadge(
                text = "Native",
                icon = Icons.Default.Memory,
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }
        
        if (!app.allowsBackup) {
            AppBadge(
                text = "No Backup",
                icon = Icons.Default.BackupTable,
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
        
        if (app.hasInternetAccess) {
            AppBadge(
                text = "Internet",
                icon = Icons.Default.Public,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
private fun AppBadge(
    text: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun AppListItem(
    app: AndroidApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "v${app.versionName} (${app.versionCode})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (app.targetSdkVersion > 0) {
                        Text(
                            text = "Target SDK: ${app.targetSdkVersion}, Min SDK: ${app.minSdkVersion}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                app.icon?.let { iconData ->
                    Image(
                        bitmap = org.jetbrains.skia.Bitmap.makeFromImage(
                            org.jetbrains.skia.Image.makeFromEncoded(iconData)
                        ).asComposeImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Badges
            AppBadges(app)
            
            // Permissions count
            if (app.permissions.isNotEmpty()) {
                Text(
                    text = "Permissions: ${app.permissions.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ComponentBadge(
    text: String,
    icon: ImageVector,
    isExported: Boolean,
    hasPermission: Boolean,
    enabled: Boolean
) {
    Surface(
        color = when {
            !enabled -> MaterialTheme.colorScheme.errorContainer
            isExported && !hasPermission -> MaterialTheme.colorScheme.errorContainer
            isExported -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
            if (isExported) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
            }
            if (hasPermission) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
            }
            if (!enabled) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityMenu(
    activity: AndroidComponent,
    packageName: String,
    onDismiss: () -> Unit,
    onLaunch: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Запустить") },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
            },
            onClick = {
                onLaunch("am start -n $packageName/${activity.name}")
                onDismiss()
            }
        )
        
        if (activity.intentFilters.isNotEmpty()) {
            DropdownMenuItem(
                text = { Text("Запустить с Intent Filter") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null
                    )
                },
                onClick = {
                    showDialog = true
                    onDismiss()
                }
            )
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Выберите Intent Filter") },
            text = {
                LazyColumn {
                    items(activity.intentFilters) { filter ->
                        val intentInfo = buildString {
                            append("Actions: ${filter.actions.joinToString()}\n")
                            if (filter.categories.isNotEmpty()) {
                                append("Categories: ${filter.categories.joinToString()}\n")
                            }
                            if (filter.dataSchemes.isNotEmpty()) {
                                append("Schemes: ${filter.dataSchemes.joinToString()}")
                            }
                        }
                        
                        Surface(
                            onClick = {
                                val action = filter.actions.firstOrNull() ?: return@Surface
                                onLaunch("am start -a $action -n $packageName/${activity.name}")
                                showDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = intentInfo,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComponentSection(
    title: String,
    components: List<AndroidComponent>,
    icon: ImageVector,
    packageName: String,
    onLaunchActivity: ((String) -> Unit)? = null
) {
    if (components.isNotEmpty()) {
        SectionTitle(title)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            //verticalAlignment = Alignment.CenterVertically
        ) {
            components.forEach { component ->
                val shortName = component.name.substringAfterLast(".")
                var showMenu by remember { mutableStateOf(false) }
                
                if (onLaunchActivity != null && component.isExported && component.enabled) {
                    Surface(
                        onClick = { showMenu = true },
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.height(24.dp)
                    ) {
                        ComponentBadge(
                            text = shortName,
                            icon = icon,
                            isExported = component.isExported,
                            hasPermission = component.permission != null,
                            enabled = component.enabled
                        )
                    }
                    
                    if (showMenu) {
                        ActivityMenu(
                            activity = component,
                            packageName = packageName,
                            onDismiss = { showMenu = false },
                            onLaunch = onLaunchActivity
                        )
                    }
                } else {
                    ComponentBadge(
                        text = shortName,
                        icon = icon,
                        isExported = component.isExported,
                        hasPermission = component.permission != null,
                        enabled = component.enabled
                    )
                }
            }
        }
    }
} 