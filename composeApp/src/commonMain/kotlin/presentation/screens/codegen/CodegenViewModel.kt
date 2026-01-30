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
                settingsDataStore.shouldRebuild
            ) { java, spec, output, pkg, rebuild ->
                _uiState.update { it.copy(
                    javaPath = java,
                    specPath = spec,
                    outputPath = output,
                    packageName = pkg,
                    shouldRebuild = rebuild
                ) }
            }.collect()
        }
    }

    fun updateSettings(java: String, spec: String, output: String, pkg: String, rebuild: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateCodegenSettings(java, spec, output, pkg, rebuild)
        }
    }

    fun generate() {
        val state = _uiState.value
        _uiState.update { it.copy(isGenerating = true, logs = "--- Starting process ---\n") }
        
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Ищем корень проекта, поднимаясь вверх от текущей директории
                    var currentDir = File(System.getProperty("user.dir"))
                    var projectRoot: File? = null
                    
                    while (currentDir.exists()) {
                        if (File(currentDir, "openapi-generator-cli").exists()) {
                            projectRoot = currentDir
                            break
                        }
                        currentDir = currentDir.parentFile ?: break
                    }

                    if (projectRoot == null) {
                        throw Exception("Could not find project root (directory containing 'openapi-generator-cli') starting from ${System.getProperty("user.dir")}")
                    }
                    
                    updateLog("Project root found: ${projectRoot.absolutePath}\n")

                    // 1. Rebuild if needed
                    if (state.shouldRebuild) {
                        updateLog("Rebuilding generator...\n")
                        val gradlewName = if (System.getProperty("os.name").contains("Win")) "gradlew.bat" else "./gradlew"
                        runCommand(listOf("cmd", "/c", gradlewName, ":my-codegen:build", "-x", "test"), projectRoot)
                    }

                    // 2. Пути к JAR делаем абсолютными
                    val genJar = File(projectRoot, "openapi-generator-cli/openapi-generator-cli-7.13.0.jar").absolutePath
                    val customJar = File(projectRoot, "openapi-generator-cli/my-codegen/build/libs/my-codegen-1.0.0.jar").absolutePath
                    
                    if (!File(customJar).exists()) {
                        throw Exception("Custom generator JAR not found at $customJar\nPlease enable 'Rebuild Generator' and try again.")
                    }

                    val cmd = listOf(
                        state.javaPath,
                        "-cp", "\"$genJar;$customJar\"",
                        "org.openapitools.codegen.OpenAPIGenerator",
                        "generate",
                        "-i", state.specPath,
                        "-g", "my-codegen",
                        "-o", state.outputPath,
                        "--global-property", "apis,models,supportingFiles,infrastructure",
                        "--additional-properties", "library=jvm-ktor,serializationLibrary=kotlinx_serialization,useSealedClasses=true,oneOfInterfaces=true,packageName=${state.packageName}",
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
        updateLog("\nExecuting in ${workingDir.name}: ${command.joinToString(" ")}\n")
        
        val processBuilder = ProcessBuilder(command)
            .directory(workingDir)
            .redirectErrorStream(true)
        
        val process = processBuilder.start()
        process.inputStream.bufferedReader().use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                updateLog(line + "\n")
            }
        }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw Exception("Process failed with exit code $exitCode")
        }
    }

    private fun updateLog(message: String) {
        _uiState.update { it.copy(logs = it.logs + message) }
    }
}
