package presentation.screens.commands

import data.local.CommandPresetDao
import data.local.entities.CommandPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import util.MacroParser
import java.io.File

data class CommandPanelUiState(
    val presets: List<CommandPreset> = emptyList(),
    val selectedPreset: CommandPreset? = null,
    val logs: String = "",
    val isRunning: Boolean = false
)

class CommandPanelViewModel(
    private val dao: CommandPresetDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommandPanelUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAllPresets().collect { list ->
                _uiState.update { it.copy(presets = list) }
            }
        }
    }

    fun selectPreset(preset: CommandPreset?) {
        _uiState.update { it.copy(selectedPreset = preset) }
    }

    fun savePreset(preset: CommandPreset) {
        viewModelScope.launch {
            dao.insertPreset(preset)
        }
    }

    fun deletePreset(preset: CommandPreset) {
        viewModelScope.launch {
            dao.deletePreset(preset)
            if (_uiState.value.selectedPreset?.id == preset.id) {
                _uiState.update { it.copy(selectedPreset = null) }
            }
        }
    }

    fun execute(projectRoot: File?) {
        val preset = _uiState.value.selectedPreset ?: return
        _uiState.update { it.copy(isRunning = true, logs = "--- Executing: ${preset.name} ---\\n") }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val execPath = MacroParser.parse(preset.executablePath, projectRoot)
                    val args = MacroParser.parse(preset.arguments, projectRoot).split(" ").filter { it.isNotBlank() }
                    val workingDir = File(MacroParser.parse(preset.workingDir, projectRoot))

                    val command = mutableListOf<String>()
                    if (System.getProperty("os.name").contains("Win")) {
                        if (execPath.endsWith(".bat") || execPath.endsWith(".cmd")) {
                            command.add("cmd")
                            command.add("/c")
                        }
                    }
                    command.add(execPath)
                    command.addAll(args)

                    updateLog("Command: ${command.joinToString(" ")}\\n")
                    updateLog("Working Dir: ${workingDir.absolutePath}\\n\\n")

                    val process = ProcessBuilder(command)
                        .directory(if (workingDir.exists()) workingDir else projectRoot)
                        .redirectErrorStream(true)
                        .start()

                    process.inputStream.bufferedReader().use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            updateLog(line + "\\n")
                        }
                    }
                    val exitCode = process.waitFor()
                    updateLog("\\n--- Finished with exit code: $exitCode ---\\n")
                } catch (e: Exception) {
                    updateLog("\\n[ERROR] ${e.message}\\n")
                } finally {
                    _uiState.update { it.copy(isRunning = false) }
                }
            }
        }
    }

    private fun updateLog(msg: String) {
        _uiState.update { it.copy(logs = it.logs + msg) }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = "") }
    }
}
