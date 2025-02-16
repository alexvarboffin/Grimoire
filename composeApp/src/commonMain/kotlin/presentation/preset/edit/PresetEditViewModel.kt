package presentation.preset.edit

import domain.model.Preset
import domain.model.TextReplacement
import domain.repository.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import util.FileProcessor
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.JFileChooser
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PresetEditViewModel(
    private val presetRepository: PresetRepository,
    private val presetId: Long?,
    private val fileProcessor: FileProcessor
) : ViewModel() {
    private var lastUsedDirectory: String? = null
    private val _state = MutableStateFlow(PresetEditState())
    val state: StateFlow<PresetEditState> = _state.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingStats = MutableStateFlow<FileProcessor.ProcessingStats?>(null)
    val processingStats: StateFlow<FileProcessor.ProcessingStats?> = _processingStats.asStateFlow()

    private val json = Json { 
        prettyPrint = true 
        encodeDefaults = true
    }

    init {
        if (presetId != null) {
            loadPreset(presetId)
        }
    }

    private fun loadPreset(id: Long) {
        viewModelScope.launch {
            presetRepository.getPresetById(id)?.let { preset ->
                _state.value = PresetEditState(
                    name = preset.name,
                    targetDirectory = preset.targetDirectory,
                    fileExtensions = preset.fileExtensions,
                    replacements = preset.replacements
                )
            }
        }
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updateTargetDirectory(directory: String) {
        _state.value = _state.value.copy(targetDirectory = directory)
    }

    fun addFileExtension(extension: String) {
        val currentExtensions = _state.value.fileExtensions.toMutableList()
        if (extension.isNotBlank() && !currentExtensions.contains(extension)) {
            currentExtensions.add(extension)
            _state.value = _state.value.copy(fileExtensions = currentExtensions)
        }
    }

    fun removeFileExtension(extension: String) {
        val currentExtensions = _state.value.fileExtensions.toMutableList()
        currentExtensions.remove(extension)
        _state.value = _state.value.copy(fileExtensions = currentExtensions)
    }

    fun addReplacement(searchPattern: String, replacement: String, isRegex: Boolean) {
        if (searchPattern.isBlank()) return
        
        val newReplacement = TextReplacement(
            searchPattern = searchPattern,
            replacement = replacement,
            isRegex = isRegex
        )
        
        _state.update { currentState ->
            currentState.copy(
                replacements = currentState.replacements + newReplacement
            )
        }
    }

    fun removeReplacement(replacement: TextReplacement) {
        val currentReplacements = _state.value.replacements.toMutableList()
        currentReplacements.remove(replacement)
        _state.value = _state.value.copy(replacements = currentReplacements)
    }

    fun updateReplacement(
        oldReplacement: TextReplacement,
        searchPattern: String,
        replacement: String,
        isRegex: Boolean
    ) {
        if (searchPattern.isBlank()) return

        val newReplacement = TextReplacement(
            searchPattern = searchPattern,
            replacement = replacement,
            isRegex = isRegex
        )

        _state.update { currentState ->
            val index = currentState.replacements.indexOf(oldReplacement)
            if (index != -1) {
                val updatedReplacements = currentState.replacements.toMutableList()
                updatedReplacements[index] = newReplacement
                currentState.copy(replacements = updatedReplacements)
            } else {
                currentState
            }
        }
    }

    fun savePreset() {
        viewModelScope.launch {
            val preset = Preset(
                id = presetId ?: 0,
                name = _state.value.name,
                targetDirectory = _state.value.targetDirectory,
                fileExtensions = _state.value.fileExtensions,
                replacements = _state.value.replacements
            )
            if (presetId == null) {
                presetRepository.insertPreset(preset)
            } else {
                presetRepository.updatePreset(preset)
            }
        }
    }

    fun clearTargetDirectory() {
        updateTargetDirectory("")
    }

    fun handleDirectoryDrop(file: File) {
        if (file.isDirectory) {
            val path = file.absolutePath
            updateTargetDirectory(path)
            lastUsedDirectory = path
        }
    }

    fun selectDirectory() {
        val fileChooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Выберите директорию"
            lastUsedDirectory?.let { currentDirectory = File(it) }
        }
        
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val directory = fileChooser.selectedFile.absolutePath
            updateTargetDirectory(directory)
            lastUsedDirectory = directory
        }
    }

    fun applyPreset() {
        val currentState = _state.value
        if (currentState.name.isBlank() || currentState.targetDirectory.isBlank() || 
            currentState.fileExtensions.isEmpty() || currentState.replacements.isEmpty()) {
            return
        }

        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _processingStats.value = null
                
                val preset = Preset(
                    id = presetId ?: 0,
                    name = currentState.name,
                    targetDirectory = currentState.targetDirectory,
                    fileExtensions = currentState.fileExtensions,
                    replacements = currentState.replacements
                )
                val stats = fileProcessor.processPreset(preset)
                _processingStats.value = stats
            } catch (e: Exception) {
                println("Ошибка при применении пресета: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun exportPreset() {
        val currentState = _state.value
        val preset = Preset(
            id = presetId ?: 0,
            name = currentState.name,
            targetDirectory = currentState.targetDirectory,
            fileExtensions = currentState.fileExtensions,
            replacements = currentState.replacements
        )

        val fileChooser = JFileChooser().apply {
            dialogTitle = "Сохранить пресет"
            selectedFile = File("${preset.name}.xml")
            fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
                "XML files", "xml"
            )
        }

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            try {
                val xmlString = buildString {
                    appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                    appendLine("<preset>")
                    appendLine("  <name>${preset.name}</name>")
                    appendLine("  <targetDirectory>${preset.targetDirectory}</targetDirectory>")
                    appendLine("  <fileExtensions>")
                    preset.fileExtensions.forEach { ext ->
                        appendLine("    <extension>$ext</extension>")
                    }
                    appendLine("  </fileExtensions>")
                    appendLine("  <replacements>")
                    preset.replacements.forEach { replacement ->
                        appendLine("    <replacement>")
                        appendLine("      <searchPattern><![CDATA[${replacement.searchPattern}]]></searchPattern>")
                        appendLine("      <replacement><![CDATA[${replacement.replacement}]]></replacement>")
                        appendLine("      <isRegex>${replacement.isRegex}</isRegex>")
                        appendLine("    </replacement>")
                    }
                    appendLine("  </replacements>")
                    appendLine("</preset>")
                }
                file.writeText(xmlString)
            } catch (e: Exception) {
                println("Ошибка при экспорте пресета: ${e.message}")
            }
        }
    }

    fun importPreset() {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Импортировать пресет"
            fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
                "XML files", "xml"
            )
        }

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                val xmlString = fileChooser.selectedFile.readText()
                val preset = parsePresetXml(xmlString)
                _state.value = PresetEditState(
                    name = preset.name,
                    targetDirectory = preset.targetDirectory,
                    fileExtensions = preset.fileExtensions,
                    replacements = preset.replacements
                )
            } catch (e: Exception) {
                println("Ошибка при импорте пресета: ${e.message}")
            }
        }
    }

    private fun parsePresetXml(xmlString: String): Preset {
        val lines = xmlString.lines()
        var name = ""
        var targetDirectory = ""
        val fileExtensions = mutableListOf<String>()
        val replacements = mutableListOf<TextReplacement>()
        
        var currentReplacement: MutableMap<String, String>? = null
        var inCData = false
        var currentCDataContent = StringBuilder()
        var currentTag = ""
        
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.contains("<![CDATA[") && trimmed.contains("]]>") -> {
                    // CDATA в одной строке
                    val content = trimmed.substringAfter("<![CDATA[").substringBefore("]]>")
                    when {
                        trimmed.startsWith("<searchPattern>") && currentReplacement != null ->
                            currentReplacement!!["searchPattern"] = content
                        trimmed.startsWith("<replacement>") && currentReplacement != null ->
                            currentReplacement!!["replacement"] = content
                    }
                }
                trimmed.contains("<![CDATA[") -> {
                    inCData = true
                    currentCDataContent = StringBuilder()
                    currentTag = when {
                        trimmed.startsWith("<searchPattern>") -> "searchPattern"
                        trimmed.startsWith("<replacement>") -> "replacement"
                        else -> ""
                    }
                    currentCDataContent.append(trimmed.substringAfter("<![CDATA["))
                }
                trimmed.contains("]]>") -> {
                    inCData = false
                    currentCDataContent.append(trimmed.substringBefore("]]>"))
                    if (currentReplacement != null) {
                        currentReplacement!![currentTag] = currentCDataContent.toString()
                    }
                }
                inCData -> {
                    currentCDataContent.append(trimmed)
                }
                trimmed.startsWith("<name>") -> 
                    name = trimmed.removeSurrounding("<name>", "</name>")
                trimmed.startsWith("<targetDirectory>") -> 
                    targetDirectory = trimmed.removeSurrounding("<targetDirectory>", "</targetDirectory>")
                trimmed.startsWith("<extension>") -> 
                    fileExtensions.add(trimmed.removeSurrounding("<extension>", "</extension>"))
                trimmed == "<replacement>" -> 
                    currentReplacement = mutableMapOf()
                trimmed.startsWith("<isRegex>") && currentReplacement != null -> {
                    currentReplacement!!["isRegex"] = trimmed.removeSurrounding("<isRegex>", "</isRegex>")
                    replacements.add(TextReplacement(
                        searchPattern = currentReplacement!!["searchPattern"]!!,
                        replacement = currentReplacement!!["replacement"]!!,
                        isRegex = currentReplacement!!["isRegex"]!!.toBoolean()
                    ))
                    currentReplacement = null
                }
            }
        }
        
        return Preset(
            id = presetId ?: 0,
            name = name,
            targetDirectory = targetDirectory,
            fileExtensions = fileExtensions,
            replacements = replacements
        )
    }

    fun clearStats() {
        _processingStats.value = null
    }
}

data class PresetEditState(
    val name: String = "",
    val targetDirectory: String = "",
    val fileExtensions: List<String> = emptyList(),
    val replacements: List<TextReplacement> = emptyList()
) 