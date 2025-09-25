package presentation.screens.templates

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import data.settings.SettingsDataStore
import domain.templates.renderTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import util.VelocityParser
import java.io.File

class TemplateEditViewModel(
    private val filePath: String,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val variables = mutableStateMapOf<String, String>()
    val renderedOutput = mutableStateOf("")

    init {
        loadTemplate()
    }

    private fun loadTemplate() {
        val file = File(filePath)
        if (file.exists()) {
            val content = file.readText()
            val extractedVars = VelocityParser.extractVariables(content)
            val defaults = VelocityParser.extractDefaults(content)

            variables.clear() // Clear previous state
            extractedVars.forEach { newVar ->
                variables[newVar] = defaults[newVar] ?: ""
            }
        }
    }

    fun onVariableChange(name: String, value: String) {
        variables[name] = value
    }

    fun render() {
        val file = File(filePath)
        if (file.exists()) {
            val content = file.readText()
            renderedOutput.value = renderTemplate(content, variables)
        }
    }

    fun save() {
        viewModelScope.launch {
            val savePath = settingsDataStore.savePath.first()
            if (savePath.isEmpty()) {
                // TODO: Show error to user
                return@launch
            }

            val className = variables["className"]
            val fileName = if (!className.isNullOrBlank()) "$className.kt" else File(filePath).name

            val finalPath = if (variables.containsKey("packageName")) {
                val packageName = variables["packageName"]!!
                val packagePath = packageName.replace('.', '/')
                "$savePath/$packagePath/$fileName"
            } else {
                "$savePath/$fileName"
            }

            domain.io.saveFile(finalPath, renderedOutput.value)
        }
    }
}