package presentation.screens.templates

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import domain.io.FileWatcher
import domain.io.createFileWatcher
import domain.templates.renderTemplate
import moe.tlaster.precompose.viewmodel.ViewModel
import util.VelocityParser
import java.io.File

class TemplateEditViewModel(private val filePath: String) : ViewModel() {

    val variables = mutableStateMapOf<String, String>()
    val renderedOutput = mutableStateOf("")
    private val fileWatcher: FileWatcher

    init {
        loadTemplate()
        fileWatcher = createFileWatcher(filePath) {
            loadTemplate()
        }
        fileWatcher.watch()
    }

    private fun loadTemplate() {
        val file = File(filePath)
        if (file.exists()) {
            val content = file.readText()
            val extractedVars = VelocityParser.extractVariables(content)
            
            val currentVars = variables.keys.toSet()
            
            // Add new vars
            extractedVars.forEach { 
                if (!variables.containsKey(it)) {
                    variables[it] = ""
                }
            }
            
            // Remove old vars
            currentVars.forEach {
                if (it !in extractedVars) {
                    variables.remove(it)
                }
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

    override fun onCleared() {
        super.onCleared()
        fileWatcher.close()
    }
}