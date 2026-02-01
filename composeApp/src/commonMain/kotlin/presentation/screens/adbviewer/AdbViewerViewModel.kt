package presentation.screens.adbviewer

import domain.model.AndroidDevice
import domain.repository.AdbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

enum class LogLevel(val code: String) {
    VERBOSE(" V "),
    DEBUG(" D "),
    INFO(" I "),
    WARN(" W "),
    ERROR(" E ")
}

data class AdbViewerUiState(
    val devices: List<AndroidDevice> = emptyList(),
    val selectedDevice: AndroidDevice? = null,
    val deviceInfo: Map<String, String> = emptyMap(),
    val logLines: List<String> = emptyList(),
    val isLogging: Boolean = false,
    val isPaused: Boolean = false,
    val filter: String = "",
    val selectedLogLevel: LogLevel? = null,
    val isLoading: Boolean = false,
    val toastMessage: String? = null
)

class AdbViewerViewModel(
    private val adbRepository: AdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdbViewerUiState())
    val uiState = _uiState.asStateFlow()

    private var logJob: kotlinx.coroutines.Job? = null

    init {
        refreshDevices()
    }

    fun refreshDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val devices = adbRepository.getDevices()
                _uiState.update { it.copy(devices = devices) }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectDevice(device: AndroidDevice) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedDevice = device, deviceInfo = emptyMap(), logLines = emptyList(), isPaused = false) }
            try {
                val info = adbRepository.getDeviceInfo(device.id)
                _uiState.update { it.copy(deviceInfo = info) }
                startLogcat(device.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Error: ${e.message}") }
            }
        }
    }

    private fun startLogcat(deviceId: String) {
        logJob?.cancel()
        logJob = viewModelScope.launch {
            _uiState.update { it.copy(isLogging = true) }
            adbRepository.getLogcat(deviceId)
                .buffer(100)
                .collect {
                    if (!_uiState.value.isPaused) {
                        _uiState.update { state ->
                            val newLines = (state.logLines + it).takeLast(2000)
                            state.copy(logLines = newLines)
                        }
                    }
                }
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun updateFilter(filter: String) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setLogLevel(level: LogLevel?) {
        _uiState.update { it.copy(selectedLogLevel = level) }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logLines = emptyList()) }
    }

    fun exportLogs(directory: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val fileName = "logcat_${_uiState.value.selectedDevice?.model ?: "device"}_$timestamp.txt"
                val file = File(directory, fileName)
                file.writeText(_uiState.value.logLines.joinToString("\n"))
                _uiState.update { it.copy(toastMessage = "Logs exported to ${file.absolutePath}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun takeScreenshot(directory: File) {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bytes = adbRepository.takeScreenshot(deviceId)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val fileName = "screenshot_${_uiState.value.selectedDevice?.model ?: "device"}_$timestamp.png"
                val file = File(directory, fileName)
                file.writeBytes(bytes)
                _uiState.update { it.copy(toastMessage = "Screenshot saved to ${file.absolutePath}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Screenshot failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}