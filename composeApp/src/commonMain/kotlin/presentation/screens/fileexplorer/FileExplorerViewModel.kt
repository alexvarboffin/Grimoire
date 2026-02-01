package presentation.screens.fileexplorer

import domain.model.AndroidDevice
import domain.model.RemoteFile
import domain.repository.AdbRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import java.io.File

data class FileExplorerUiState(
    val devices: List<AndroidDevice> = emptyList(),
    val selectedDevice: AndroidDevice? = null,
    val currentPath: String = "/sdcard",
    val files: List<RemoteFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class FileExplorerViewModel(
    private val adbRepository: AdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileExplorerUiState())
    val uiState = _uiState.asStateFlow()

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
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectDevice(device: AndroidDevice) {
        _uiState.update { it.copy(selectedDevice = device, currentPath = "/sdcard") }
        loadFiles("/sdcard")
    }

    fun loadFiles(path: String) {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val files = adbRepository.listFiles(deviceId, path)
                _uiState.update { it.copy(files = files, currentPath = path) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun navigateUp() {
        val current = _uiState.value.currentPath
        if (current == "/" || current.isEmpty()) return
        val parent = current.substringBeforeLast("/").ifEmpty { "/" }
        loadFiles(parent)
    }

    fun deleteFile(file: RemoteFile) {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        viewModelScope.launch {
            try {
                adbRepository.deleteFile(deviceId, file.path)
                loadFiles(_uiState.value.currentPath)
                _uiState.update { it.copy(successMessage = "Deleted: ${file.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun pullFile(remoteFile: RemoteFile, localDir: File) {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val target = File(localDir, remoteFile.name)
                adbRepository.pullFile(deviceId, remoteFile.path, target.absolutePath)
                _uiState.update { it.copy(successMessage = "Downloaded to: ${target.absolutePath}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun pushFile(localFile: File) {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        val remotePath = _uiState.value.currentPath
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                adbRepository.pushFile(deviceId, localFile.absolutePath, remotePath)
                loadFiles(remotePath)
                _uiState.update { it.copy(successMessage = "Uploaded: ${localFile.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun createDirectory(name: String) {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        val path = if (_uiState.value.currentPath.endsWith("/")) "${_uiState.value.currentPath}$name" else "${_uiState.value.currentPath}/$name"
        viewModelScope.launch {
            try {
                adbRepository.createDirectory(deviceId, path)
                loadFiles(_uiState.value.currentPath)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
