package presentation.screens.commands

import data.local.CommandPresetDao
import data.local.CommandHistoryDao
import data.local.CommandPipelineDao
import data.local.entities.*
import data.settings.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.viewModelScope
import util.MacroParser
import java.io.File

data class CommandPanelUiState(
    val presets: List<CommandPreset> = emptyList(),
    val pipelines: List<CommandPipeline> = emptyList(),
    val selectedPreset: CommandPreset? = null,
    val selectedPipeline: CommandPipeline? = null,
    val selectedPipelineSteps: List<CommandPreset> = emptyList(),
    val history: List<CommandHistory> = emptyList(),
    val logs: String = "",
    val isRunning: Boolean = false,
    val pendingInputs: List<String> = emptyList(),
    val showInputDialog: Boolean = false,
    val toastMessage: String? = null
)

class CommandPanelViewModel(
    private val dao: CommandPresetDao,
    private val historyDao: CommandHistoryDao,
    private val pipelineDao: CommandPipelineDao,
    private val settings: SettingsDataStore
) : moe.tlaster.precompose.viewmodel.ViewModel() {

    private val _uiState = MutableStateFlow(CommandPanelUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch { dao.getAllPresets().collect { list -> _uiState.update { it.copy(presets = list) } } }
        viewModelScope.launch { pipelineDao.getAllPipelines().collect { list -> _uiState.update { it.copy(pipelines = list) } } }
        viewModelScope.launch { historyDao.getRecentHistory().collect { list -> _uiState.update { it.copy(history = list) } } }
    }

    fun selectPreset(preset: CommandPreset?) { _uiState.update { it.copy(selectedPreset = preset, selectedPipeline = null, logs = "") } }
    
    fun selectPipeline(pipeline: CommandPipeline?) {
        _uiState.update { it.copy(selectedPipeline = pipeline, selectedPreset = null, logs = "") }
        if (pipeline != null) {
            viewModelScope.launch {
                val steps = pipelineDao.getStepsForPipeline(pipeline.id)
                val presetsMap = _uiState.value.presets.associateBy { it.id }
                val presets = steps.mapNotNull { presetsMap[it.presetId] }
                _uiState.update { it.copy(selectedPipelineSteps = presets) }
            }
        } else {
            _uiState.update { it.copy(selectedPipelineSteps = emptyList()) }
        }
    }

    fun savePipeline(id: Long, name: String, steps: List<Long>) {
        viewModelScope.launch {
            val pipelineId = if (id == 0L) {
                pipelineDao.insertPipeline(CommandPipeline(name = name))
            } else {
                pipelineDao.insertPipeline(CommandPipeline(id = id, name = name))
                id
            }
            pipelineDao.deleteStepsForPipeline(pipelineId)
            steps.forEachIndexed { index, presetId ->
                pipelineDao.insertStep(PipelineStep(pipelineId = pipelineId, presetId = presetId, sequenceOrder = index))
            }
            // Refresh selection if it was the edited pipeline
            if (_uiState.value.selectedPipeline?.id == pipelineId) {
                selectPipeline(CommandPipeline(id = pipelineId, name = name))
            }
        }
    }

    fun deletePipeline(pipeline: CommandPipeline) {
        viewModelScope.launch {
            pipelineDao.deletePipeline(pipeline)
            _uiState.update { it.copy(selectedPipeline = null) }
        }
    }

    fun exportData(file: File) {
        viewModelScope.launch {
            try {
                val presets = _uiState.value.presets
                val pipelines = _uiState.value.pipelines
                
                val exportPresets = presets.map { 
                    CommandPresetExport(it.groupName, it.subGroupName, it.name, it.executablePath, it.arguments, it.workingDir, it.description)
                }
                
                val exportPipelines = mutableListOf<CommandPipelineExport>()
                for (p in pipelines) {
                    val steps = pipelineDao.getStepsForPipeline(p.id)
                    val stepNames = steps.mapNotNull { step -> presets.find { it.id == step.presetId }?.name }
                    exportPipelines.add(CommandPipelineExport(p.name, p.description, stepNames))
                }
                
                val model = CommandExportModel(presets = exportPresets, pipelines = exportPipelines)
                val json = kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(CommandExportModel.serializer(), model)
                
                file.writeText(json)
                _uiState.update { it.copy(toastMessage = "Exported to ${file.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun importData(file: File) {
        viewModelScope.launch {
            try {
                val json = file.readText()
                val model = kotlinx.serialization.json.Json.decodeFromString(CommandExportModel.serializer(), json)
                
                // Import Presets
                for (p in model.presets) {
                    dao.insertPreset(CommandPreset(
                        groupName = p.groupName,
                        subGroupName = p.subGroupName,
                        name = p.name,
                        executablePath = p.executablePath,
                        arguments = p.arguments,
                        workingDir = p.workingDir,
                        description = p.description
                    ))
                }
                
                // Wait for DB to update presets list to get IDs
                val freshPresets = dao.getAllPresets().first()
                
                // Import Pipelines
                for (pipe in model.pipelines) {
                    val pipelineId = pipelineDao.insertPipeline(CommandPipeline(name = pipe.name, description = pipe.description))
                    pipe.stepPresetNames.forEachIndexed { index, name ->
                        val presetId = freshPresets.find { it.name == name }?.id
                        if (presetId != null) {
                            pipelineDao.insertStep(PipelineStep(pipelineId = pipelineId, presetId = presetId, sequenceOrder = index))
                        }
                    }
                }
                
                _uiState.update { it.copy(toastMessage = "Imported successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Import failed: ${e.message}") }
            }
        }
    }

    fun executePipeline(projectRoot: File?) {
        val pipeline = _uiState.value.selectedPipeline ?: return
        _uiState.update { it.copy(isRunning = true, logs = "--- Starting Pipeline: ${pipeline.name} ---\\n") }
        
        viewModelScope.launch {
            val steps = pipelineDao.getStepsForPipeline(pipeline.id)
            val presetsMap = _uiState.value.presets.associateBy { it.id }
            val globalVars = settings.globalVariables.first()
            
            var allSuccess = true
            for (step in steps) {
                val preset = presetsMap[step.presetId] ?: continue
                updateLog("\\n>> Step: ${preset.name}\\n")
                val success = runCommandSync(preset, projectRoot, emptyMap(), globalVars)
                if (!success) {
                    allSuccess = false
                    updateLog("\\n[PIPELINE STOPPED] Step ${preset.name} failed.\\n")
                    break
                }
            }
            
            _uiState.update { it.copy(
                isRunning = false, 
                toastMessage = if (allSuccess) "Pipeline Finished Successfully!" else "Pipeline Failed!"
            ) }
        }
    }

    private suspend fun runCommandSync(preset: CommandPreset, projectRoot: File?, userInputs: Map<String, String>, globalVars: Map<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val execPath = MacroParser.parse(preset.executablePath, projectRoot, globalVars, userInputs)
                val argsString = MacroParser.parse(preset.arguments, projectRoot, globalVars, userInputs)
                val args = argsString.split(" ").filter { it.isNotBlank() }
                val workingDirString = MacroParser.parse(preset.workingDir, projectRoot, globalVars, userInputs)
                val workingDir = File(workingDirString)

                val command = mutableListOf<String>()
                if (System.getProperty("os.name").contains("Win") && (execPath.endsWith(".bat") || execPath.endsWith(".cmd"))) {
                    command.addAll(listOf("cmd", "/c"))
                }
                command.add(execPath)
                command.addAll(args)

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
                process.waitFor() == 0
            } catch (e: Exception) {
                updateLog("[ERROR] ${e.message}\\n")
                false
            }
        }
    }

    fun prepareExecution() {
        val preset = _uiState.value.selectedPreset ?: return
        val textToParse = preset.executablePath + " " + preset.arguments
        val inputs = MacroParser.extractDynamicMacros(textToParse)
        if (inputs.isNotEmpty()) _uiState.update { it.copy(pendingInputs = inputs, showInputDialog = true) }
        else {
            viewModelScope.launch {
                val globalVars = settings.globalVariables.first()
                execute(File(System.getProperty("user.dir")), emptyMap())
            }
        }
    }

    fun execute(projectRoot: File?, userInputs: Map<String, String>) {
        val preset = _uiState.value.selectedPreset ?: return
        _uiState.update { it.copy(isRunning = true, showInputDialog = false, logs = "--- Executing: ${preset.name} ---\\n") }
        viewModelScope.launch {
            val globalVars = settings.globalVariables.first()
            val success = runCommandSync(preset, projectRoot, userInputs, globalVars)
            _uiState.update { it.copy(isRunning = false, toastMessage = if (success) "Finished!" else "Failed!") }
        }
    }

    private fun updateLog(msg: String) { _uiState.update { it.copy(logs = it.logs + msg) } }
    fun clearToast() { _uiState.update { it.copy(toastMessage = null) } }
    fun savePreset(preset: CommandPreset) { viewModelScope.launch { dao.insertPreset(preset) } }
    fun deletePreset(preset: CommandPreset) { viewModelScope.launch { dao.deletePreset(preset) } }
    fun dismissInputDialog() { _uiState.update { it.copy(showInputDialog = false) } }
    fun clearLogs() {

    }
}

