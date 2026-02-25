package presentation.screens.ossetup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import java.io.BufferedReader
import java.io.InputStreamReader

data class OsSetupItem(
    val command: String,
    val status: SetupStatus = SetupStatus.IDLE
)

enum class SetupStatus {
    IDLE, RUNNING, SUCCESS, FAILED
}

data class OsSetupUiState(
    val items: List<OsSetupItem> = listOf(
        OsSetupItem("winget install --id=Google.AndroidStudio -e"),
        OsSetupItem("winget install everything"),
        OsSetupItem("winget install Mozilla.Firefox"),
        OsSetupItem("winget install git.git"),
        OsSetupItem("winget install nodejs"),
        OsSetupItem("winget install telegram.telegramdesktop"),
        OsSetupItem("winget install DominikReichl.KeePass.Classic"),
        OsSetupItem("winget install rapidee"),
        OsSetupItem("winget install notepad++"),
        OsSetupItem("winget install brave.brave"),
        OsSetupItem("winget install obsidian.obsidian")
    ),
    val logs: String = "",
    val isRunningAll: Boolean = false
)

class OsSetupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OsSetupUiState())
    val uiState = _uiState.asStateFlow()

    fun runCommand(item: OsSetupItem) {
        viewModelScope.launch {
            updateItemStatus(item, SetupStatus.RUNNING)
            val success = executeWinget(item.command)
            updateItemStatus(item, if (success) SetupStatus.SUCCESS else SetupStatus.FAILED)
        }
    }

    fun runAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunningAll = true) }
            _uiState.value.items.forEach { item ->
                if (item.status != SetupStatus.SUCCESS) {
                    runCommand(item)
                    // Wait for it to finish (simplified)
                    while (_uiState.value.items.find { it.command == item.command }?.status == SetupStatus.RUNNING) {
                        kotlinx.coroutines.delay(500)
                    }
                }
            }
            _uiState.update { it.copy(isRunningAll = false) }
        }
    }

    private fun updateItemStatus(item: OsSetupItem, status: SetupStatus) {
        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.command == item.command) it.copy(status = status) else it })
        }
    }

    private suspend fun executeWinget(command: String): Boolean = withContext(Dispatchers.IO) {
        try {
            updateLog(" > $command")
            val process = ProcessBuilder(command.split(" "))
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                updateLog(line + "")
            }
            process.waitFor() == 0
        } catch (e: Exception) {
            updateLog("[ERROR] ${e.message}")
            false
        }
    }

    private fun updateLog(msg: String) {
        _uiState.update { it.copy(logs = it.logs + msg) }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = "") }
    }
}
