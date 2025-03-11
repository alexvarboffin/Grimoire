package presentation.screens.packageManager

import domain.model.AndroidApp
import domain.model.AndroidDevice
import domain.repository.AdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlinx.coroutines.delay

data class PackageManagerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAdbRunning: Boolean = false,
    val devices: List<AndroidDevice> = emptyList(),
    val selectedDevice: AndroidDevice? = null,
    val apps: List<AndroidApp> = emptyList(),
    val searchQuery: String = "",
    val showSystemApps: Boolean = false,
    val sortOrder: SortOrder = SortOrder.NAME,
    val selectedApp: AndroidApp? = null,
    val showAppMenu: Boolean = false,
    val success: String? = null,
    val showSuccessMessage: Boolean = false
)

enum class SortOrder {
    NAME,
    INSTALL_DATE,
    UPDATE_DATE,
    PACKAGE_NAME
}

class PackageManagerViewModel(
    private val adbRepository: AdbRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackageManagerUiState())
    val uiState: StateFlow<PackageManagerUiState> = _uiState.asStateFlow()

    init {
        println("PackageManagerViewModel: Initializing")
        viewModelScope.launch {
            adbRepository.isRunning.collect { isRunning ->
                println("PackageManagerViewModel: ADB running state changed: $isRunning")
                _uiState.update { it.copy(isAdbRunning = isRunning) }
                if (isRunning) {
                    refreshDevices()
                }
            }
        }
    }

    fun startAdb() {
        println("PackageManagerViewModel: Starting ADB")
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                adbRepository.startAdbServer()
                refreshDevices()
            } catch (e: Exception) {
                println("PackageManagerViewModel: Error starting ADB: ${e.message}")
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun stopAdb() {
        println("PackageManagerViewModel: Stopping ADB")
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                adbRepository.stopAdbServer()
                _uiState.update { it.copy(devices = emptyList(), selectedDevice = null, apps = emptyList()) }
            } catch (e: Exception) {
                println("PackageManagerViewModel: Error stopping ADB: ${e.message}")
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refreshDevices() {
        println("PackageManagerViewModel: Refreshing devices")
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val devices = adbRepository.getDevices()
                println("PackageManagerViewModel: Found ${devices.size} devices")
                _uiState.update { it.copy(devices = devices) }
            } catch (e: Exception) {
                println("PackageManagerViewModel: Error refreshing devices: ${e.message}")
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectDevice(device: AndroidDevice) {
        println("PackageManagerViewModel: Selecting device: ${device.id}")
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    selectedDevice = device,
                    isLoading = true,
                    apps = emptyList()
                )
            }
            try {
                // Сначала пробуем получить root права
                adbRepository.selectDevice(device.id)
                // Затем получаем список приложений
                val apps = adbRepository.getInstalledApps(device.id)
                println("PackageManagerViewModel: Found ${apps.size} apps for device ${device.id}")
                _uiState.update { 
                    it.copy(
                        apps = apps,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                println("PackageManagerViewModel: Error selecting device: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSystemApps() {
        _uiState.update { it.copy(showSystemApps = !it.showSystemApps) }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun selectApp(app: AndroidApp) {
        _uiState.update { 
            it.copy(
                selectedApp = app,
                showAppMenu = true
            )
        }
    }

    fun hideAppMenu() {
        _uiState.update { it.copy(showAppMenu = false) }
    }

    fun uninstallSelectedApp() {
        val device = _uiState.value.selectedDevice ?: return
        val app = _uiState.value.selectedApp ?: return
        
        viewModelScope.launch {
            try {
                adbRepository.uninstallApp(device.id, app.packageName)
                // Обновляем список приложений
                selectDevice(device)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
            hideAppMenu()
        }
    }

    fun clearSelectedAppData() {
        val device = _uiState.value.selectedDevice ?: return
        val app = _uiState.value.selectedApp ?: return
        
        viewModelScope.launch {
            try {
                adbRepository.clearAppData(device.id, app.packageName)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
            hideAppMenu()
        }
    }

    fun forceStopSelectedApp() {
        val device = _uiState.value.selectedDevice ?: return
        val app = _uiState.value.selectedApp ?: return
        
        viewModelScope.launch {
            try {
                adbRepository.forceStopApp(device.id, app.packageName)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
            hideAppMenu()
        }
    }

    fun openInGooglePlay() {
        val device = _uiState.value.selectedDevice ?: return
        val app = _uiState.value.selectedApp ?: return
        
        viewModelScope.launch {
            try {
                adbRepository.openInGooglePlay(device.id, app.packageName)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
            hideAppMenu()
        }
    }

    fun extractApk() {
        val device = _uiState.value.selectedDevice ?: return
        val app = _uiState.value.selectedApp ?: return
        
        viewModelScope.launch {
            try {
                val path = adbRepository.extractApk(device.id, app.packageName)
                _uiState.update { 
                    it.copy(
                        success = "APK извлечен в: $path",
                        showSuccessMessage = true
                    )
                }
                delay(3000)
                _uiState.update { it.copy(showSuccessMessage = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
            hideAppMenu()
        }
    }

    fun launchActivity(command: String) {
        val device = _uiState.value.selectedDevice ?: return
        
        viewModelScope.launch {
            try {
                adbRepository.executeShellCommand(command)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            adbRepository.stopAdbServer()
        }
        super.onCleared()
    }
} 