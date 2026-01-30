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
    val shouldRebuild: Boolean = false,
    val library: String = "jvm-ktor",
    val serializationLibrary: String = "kotlinx_serialization",
    val useSealedClasses: Boolean = true,
    val oneOfInterfaces: Boolean = true,
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
                settingsDataStore.packageName,
                settingsDataStore.shouldRebuild,
                settingsDataStore.library,
                settingsDataStore.serializationLibrary,
                settingsDataStore.useSealedClasses,
                settingsDataStore.oneOfInterfaces
            ) { args ->
                _uiState.update { it.copy(
                    javaPath = args[0] as String,
                    specPath = args[1] as String,
                    outputPath = args[2] as String,
                    packageName = args[3] as String,
                    shouldRebuild = args[4] as Boolean,
                    library = args[5] as String,
                    serializationLibrary = args[6] as String,
                    useSealedClasses = args[7] as Boolean,
                    oneOfInterfaces = args[8] as Boolean
                ) }
            }.collect()
        }
    }

    fun updateSettings(
        java: String, spec: String, output: String, pkg: String, 
        rebuild: Boolean, lib: String, serialization: String, 
        useSealed: Boolean, oneOf: Boolean
    ) {
        viewModelScope.launch {
            settingsDataStore.updateCodegenSettings(java, spec, output, pkg, rebuild, lib, serialization, useSealed, oneOf)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsDataStore.resetCodegenSettings()
        }
    }

    fun generate() {
        val state = _uiState.value
        _uiState.update { it.copy(isGenerating = true, logs = "--- Starting process ---\n") }
        
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    var currentDir = File(System.getProperty("user.dir"))
                    var projectRoot: File? = null
                    while (currentDir.exists()) {
                        if (File(currentDir, "openapi-generator-cli").exists()) {
                            projectRoot = currentDir
                            break
                        }
                        currentDir = currentDir.parentFile ?: break
                    }

                    if (projectRoot == null) throw Exception("Project root not found")
                    
                    updateLog("Project root found: ${projectRoot.absolutePath}\n")

                    if (state.shouldRebuild) {
                        updateLog("Rebuilding generator...\n")
                        val gradlewName = if (System.getProperty("os.name").contains("Win")) "gradlew.bat" else "./gradlew"
                        runCommand(listOf("cmd", "/c", gradlewName, ":my-codegen:build", "-x", "test"), projectRoot)
                    }

                    val genJar = File(projectRoot, "openapi-generator-cli/openapi-generator-cli-7.13.0.jar").absolutePath
                    val customJar = File(projectRoot, "openapi-generator-cli/my-codegen/build/libs/my-codegen-1.0.0.jar").absolutePath
                    
                    if (!File(customJar).exists()) throw Exception("Custom JAR not found. Please Rebuild.")

                    val additionalProps = listOf(
                        "library=${state.library}",
                        "serializationLibrary=${state.serializationLibrary}",
                        "useSealedClasses=${state.useSealedClasses}",
                        "oneOfInterfaces=${state.oneOfInterfaces}",
                        "packageName=${state.packageName}"
                    ).joinToString(",")

                    val cmd = listOf(
                        state.javaPath,
                        "-cp", "\"$genJar;$customJar\"",
                        "org.openapitools.codegen.OpenAPIGenerator",
                        "generate",
                        "-i", state.specPath,
                        "-g", "my-codegen",
                        "-o", state.outputPath,
                        "--global-property", "apis,models,supportingFiles,infrastructure",
                        "--additional-properties", "\"$additionalProps\"",
                        "--skip-validate-spec"
                    )
                    
                    updateLog("Running generation...\n")
                    runCommand(cmd, projectRoot)
                    updateLog("\n[SUCCESS] All done!")
                } catch (e: Exception) {
                    updateLog("\n[ERROR] ${e.message}")
                } finally {
                    _uiState.update { it.copy(isGenerating = false) }
                }
            }
        }
    }

    private fun runCommand(command: List<String>, workingDir: File) {
        updateLog("\nExecuting: ${command.joinToString(" ")}\n")
        val processBuilder = ProcessBuilder(command).directory(workingDir).redirectErrorStream(true)
        val process = processBuilder.start()
        process.inputStream.bufferedReader().use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) { updateLog(line + "\n") }
        }
        val exitCode = process.waitFor()
        if (exitCode != 0) throw Exception("Exit code $exitCode")
    }

    private fun updateLog(message: String) {
        _uiState.update { it.copy(logs = it.logs + message) }
    }
}