package presentation.screens.list

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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

data class ListGeneratorUiState(
    val templates: List<FileEntry> = emptyList(),
    val selectedTemplate: FileEntry? = null,
    val allVariables: List<String> = emptyList(),
    val listVariables: List<String> = emptyList(),
    val output: String = "",
    val errorMessage: String? = null,
    val filenamePattern: String? = null
)

class ListGeneratorViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val templatesRootPath = "C:\\src\\Synced\\Grimoire\\Templates"
    private var generatedContent: String = ""

    val uiState = mutableStateOf(ListGeneratorUiState())
    val variableValues = mutableStateMapOf<String, Any>()

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
                val allVars = VelocityParser.extractVariables(content).toList()
                val listVars = VelocityParser.extractListVariableNames(content)
                val defaults = VelocityParser.extractDefaults(content)
                val filenamePattern = VelocityParser.extractFilename(content)

                variableValues.clear()
                allVars.forEach { varName ->
                    if (listVars.contains(varName)) {
                        variableValues[varName] = SnapshotStateList<String>()
                    } else {
                        variableValues[varName] = defaults[varName] ?: ""
                    }
                }

                uiState.value = uiState.value.copy(
                    selectedTemplate = template,
                    allVariables = allVars,
                    listVariables = listVars,
                    output = "",
                    filenamePattern = filenamePattern
                )
                generatedContent = ""
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = "Ошибка чтения шаблона: ${e.message}")
            }
        }
    }

    fun onSimpleVariableChanged(name: String, value: String) {
        clearError()
        variableValues[name] = value
    }

    fun addListItem(listName: String, item: String) {
        clearError()
        val list = variableValues[listName] as? SnapshotStateList<String>
        if (list != null) {
            if (item.isNotBlank() && !list.contains(item)) {
                list.add(item)
            }
        }
    }

    fun removeListItem(listName: String, item: String) {
        clearError()
        val list = variableValues[listName] as? SnapshotStateList<String>
        list?.remove(item)
    }

    fun addListItemsFromFile(listName: String, path: String) {
        clearError()
        viewModelScope.launch {
            try {
                val content = readFileContent(path)
                content.lines().forEach { currentLine ->
                    val item = currentLine.trim()
                    addListItem(listName, item)
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
                if (uiState.value.listVariables.any { (variableValues[it] as? SnapshotStateList<*>)?.isEmpty() == true }) {
                    throw IllegalStateException("Один из списков элементов пуст.")
                }

                val templateContent = readFileContent(template.path)
                val allVars: MutableMap<String, Any> = variableValues.toMutableMap()

                val result = renderTemplate(templateContent, allVars)
                generatedContent = result
                uiState.value = uiState.value.copy(output = result)
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = "Ошибка генерации: ${e.message}")
            }
        }
    }

    fun save() {
        clearError()
        viewModelScope.launch {
            try {
                val filenamePattern = uiState.value.filenamePattern ?: throw IllegalStateException("Директива @filename не найдена в шаблоне.")
                if (generatedContent.isBlank()) {
                    throw IllegalStateException("Нечего сохранять. Сначала сгенерируйте файл.")
                }

                val allVars: MutableMap<String, Any> = variableValues.toMutableMap()
                val fileName = renderTemplate(filenamePattern, allVars)

                val savePath = settingsDataStore.savePath.first()
                val finalPath = if (allVars.containsKey("packageName")) {
                    val packageName = allVars["packageName"] as String
                    val packagePath = packageName.replace('.', '/')
                    "$savePath/$packagePath/$fileName"
                } else {
                    "$savePath/$fileName"
                }
                saveFile(finalPath, generatedContent)
                uiState.value = uiState.value.copy(output = "Сохранено: $finalPath\n\n" + uiState.value.output)
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = "Ошибка сохранения: ${e.message}")
            }
        }
    }
}