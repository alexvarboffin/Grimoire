package presentation.screens.signer

import data.settings.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import java.io.File

data class SignerUiState(
    val javaPath: String = "",
    val jarPath: String = "",
    val apkPath: String = "",
    val isSigning: Boolean = false,
    val logs: String = ""
)

class SignerViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignerUiState())
    val uiState: StateFlow<SignerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsDataStore.javaPath,
                settingsDataStore.signerJarPath,
                settingsDataStore.lastApkPath
            ) { java, jar, apk ->
                _uiState.update { it.copy(javaPath = java, jarPath = jar, apkPath = apk) }
            }.collect()
        }
    }

    fun updateSettings(java: String, jar: String, apk: String) {
        viewModelScope.launch {
            settingsDataStore.updateSignerSettings(java, jar, apk)
        }
    }

    fun sign() {
        val state = _uiState.value
        _uiState.update { it.copy(isSigning = true, logs = "--- Starting Signing ---\\n") }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (!File(state.jarPath).exists()) throw Exception("Signer JAR not found!")
                    if (!File(state.apkPath).exists()) throw Exception("APK file not found!")

                    val cmd = listOf(
                        state.javaPath,
                        "-jar", state.jarPath,
                        "-a", state.apkPath
                    )

                    updateLog("Executing: ${cmd.joinToString(" ")}\\n\\n")
                    
                    val process = ProcessBuilder(cmd).redirectErrorStream(true).start()
                    process.inputStream.bufferedReader().use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            updateLog("$line\\n")
                        }
                    }
                    val exitCode = process.waitFor()
                    if (exitCode == 0) updateLog("\\n[SUCCESS] APK signed successfully!")
                    else updateLog("\\n[ERROR] Signing failed with exit code $exitCode")
                    
                } catch (e: Exception) {
                    updateLog("\\n[ERROR] ${e.message}")
                } finally {
                    _uiState.update { it.copy(isSigning = false) }
                }
            }
        }
    }

    private fun updateLog(message: String) {
        _uiState.update { it.copy(logs = it.logs + message) }
    }
}
