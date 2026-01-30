package presentation.screens.codegen

import data.settings.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import java.io.File

data class CodegenUiState(
    val javaPath: String = "",
    val specPath: String = "",
    val outputPath: String = "",
    val packageName: String = "a.a.a",
    val isGenerating: Boolean = false,
    val logs: String = ""
)

class CodegenViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CodegenUiState())
    val uiState: StateFlow<CodegenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsDataStore.javaPath,
                settingsDataStore.specPath,
                settingsDataStore.outputPath,
                settingsDataStore.packageName
            ) { java, spec, output, pkg ->
                _uiState.update { it.copy(
                    javaPath = java,
                    specPath = spec,
                    outputPath = output,
                    packageName = pkg
                ) }
            }.collect()
        }
    }

    fun updateSettings(java: String, spec: String, output: String, pkg: String) {
        viewModelScope.launch {
            settingsDataStore.updateCodegenSettings(java, spec, output, pkg)
        }
    }

    fun generate() {
        val state = _uiState.value
        _uiState.update { it.copy(isGenerating = true, logs = "--- Starting generation ---\\n") }
        
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Собираем генератор перед запуском (опционально, если нужно)
                    runCommand(listOf("./gradlew.bat", ":my-codegen:build", "-x", "test"))

                    val genJar = "openapi-generator-cli/openapi-generator-cli-7.13.0.jar"
                    val customJar = "openapi-generator-cli/my-codegen/build/libs/my-codegen-1.0.0.jar"
                    
                    val cmd = listOf(
                        state.javaPath,
                        "-cp", "$genJar;$customJar",
                        "org.openapitools.codegen.OpenAPIGenerator",
                        "generate",
                        "-i", state.specPath,
                        "-g", "my-codegen",
                        "-o", state.outputPath,
                        "--global-property", "apis,models,supportingFiles,infrastructure",
                        "--additional-properties", "library=jvm-ktor,serializationLibrary=kotlinx_serialization,useSealedClasses=true,oneOfInterfaces=true,packageName=${state.packageName}",
                        "--skip-validate-spec"
                    )
                    
                    runCommand(cmd)
                    
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(logs = it.logs + "\\n[SUCCESS] Generation complete!") }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(logs = it.logs + "\\n[ERROR] ${e.message}") }
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isGenerating = false) }
                    }
                }
            }
        }
    }

    private fun runCommand(command: List<String>) {
        updateLog("\\nExecuting: ${command.joinToString(" ")}\\n")
        
        val processBuilder = ProcessBuilder(command)
            .directory(File(".")) // Корень проекта
            .redirectErrorStream(true)
        
        val process = processBuilder.start()
        process.inputStream.bufferedReader().use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                updateLog(line + "\\n")
            }
        }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw Exception("Process failed with exit code $exitCode")
        }
    }

    private fun updateLog(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(logs = it.logs + message) }
        }
    }
}