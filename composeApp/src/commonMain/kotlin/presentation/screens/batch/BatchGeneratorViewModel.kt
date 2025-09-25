package presentation.screens.batch

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import data.settings.SettingsDataStore
import domain.io.FileEntry
import domain.io.listFilesRecursively
import domain.io.readFileContent
import domain.io.saveFile
import domain.templates.renderTemplate
import kotlinx.coroutines.flow.first
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlinx.coroutines.launch
import util.VelocityParser

data class BatchGeneratorUiState(
    val templates: List<FileEntry> = emptyList(),
    val selectedTemplate: FileEntry? = null,
    val templateVariables: List<String> = emptyList(),
    val logOutput: String = "",
    val errorMessage: String? = null
)

class BatchGeneratorViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val templatesRootPath = "C:\\src\\Synced\\Grimoire\\Templates"
    private val generatedFiles = mutableMapOf<String, String>()

    val uiState = mutableStateOf(BatchGeneratorUiState())
    val batchItems = mutableStateListOf<String>()
    val otherVariableValues = mutableStateOf<Map<String, String>>(mutableMapOf())

    init {
        loadTemplates()
    }

    private fun clearError() {
        uiState.value = uiState.value.copy(errorMessage = null)
    }

    private fun loadTemplates() {
        clearError()
        viewModelScope.launch {
            val allFiles = listFilesRecursively(templatesRootPath)
            val vmFiles = allFiles.filter { it.name.endsWith(".vm") }
            uiState.value = uiState.value.copy(templates = vmFiles)
        }
    }

    fun onTemplateSelected(template: FileEntry) {
        clearError()
        viewModelScope.launch {
            try {
                val content = readFileContent(template.path)
                val variables = VelocityParser.extractVariables(content).toList()
                val defaults = VelocityParser.extractDefaults(content)
                
                uiState.value = uiState.value.copy(
                    selectedTemplate = template,
                    templateVariables = variables.filter { it != "itemName" && it != "className" }, // Hide internal variables
                    logOutput = ""
                )
                otherVariableValues.value = defaults
                batchItems.clear()
                generatedFiles.clear()
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = "Ошибка чтения шаблона: ${e.message}")
            }
        }
    }

    fun onOtherVariableChanged(name: String, value: String) {
        clearError()
        val newMap = otherVariableValues.value.toMutableMap()
        newMap[name] = value
        otherVariableValues.value = newMap
    }

    fun addBatchItem(item: String) {
        clearError()
        if (item.isNotBlank() && !batchItems.contains(item)) {
            batchItems.add(item)
        }
    }

    fun removeBatchItem(item: String) {
        clearError()
        batchItems.remove(item)
    }

    fun addBatchItemsFromFile(path: String) {
        clearError()
        viewModelScope.launch {
            try {
                val content = readFileContent(path)
                content.lines().forEach { line ->
                    val item = line.trim()
                    if (item.isNotBlank() && !batchItems.contains(item)) {
                        batchItems.add(item)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = "Ошибка импорта: ${e.message}")
            }
        }
    }

    fun generate() {
        clearError()
        viewModelScope.launch {
            try {
                val template = uiState.value.selectedTemplate ?: throw IllegalStateException("Шаблон не выбран.")
                if (batchItems.isEmpty()) throw IllegalStateException("Список элементов для генерации пуст.")

                val templateContent = readFileContent(template.path)
                
                val output = StringBuilder()
                generatedFiles.clear()

                for (item in batchItems) {
                    val allVars = otherVariableValues.value.toMutableMap()
                    allVars["itemName"] = item

                    val result = renderTemplate(templateContent, allVars)
                    val fileName = "${item}Screen.kt" // Simplified filename logic

                    output.append("--- FILE: $fileName ---\n")
                    output.append(result)
                    output.append("\n\n")

                    val savePath = settingsDataStore.savePath.first()
                    val finalPath = if (allVars.containsKey("packageName")) {
                        val packageName = allVars["packageName"]!!
                        val packagePath = packageName.replace('.', '/')
                        "$savePath/$packagePath/$fileName"
                    } else {
                        "$savePath/$fileName"
                    }
                    generatedFiles[finalPath] = result
                }
                uiState.value = uiState.value.copy(logOutput = output.toString())
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = "Ошибка генерации: ${e.message}")
            }
        }
    }

    fun save() {
        clearError()
        viewModelScope.launch {
            try {
                if (generatedFiles.isEmpty()) {
                    generate() // Auto-generate if not already done
                }
                if (generatedFiles.isEmpty()) {
                     throw IllegalStateException("Нечего сохранять. Сначала сгенерируйте файлы.")
                }
                generatedFiles.forEach { (path, content) ->
                    saveFile(path, content)
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = "Ошибка сохранения: ${e.message}")
            }
        }
    }
}