package presentation.screens.settings

import data.settings.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val savePath: String = "",
    val globalVariables: Map<String, String> = emptyMap(),
    val appVersion: String = "1.0.0",
    val deviceInfo: String = "Desktop",
    val osInfo: String = System.getProperty("os.name")
)

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsDataStore.isDarkTheme,
                settingsDataStore.savePath,
                settingsDataStore.globalVariables
            ) { dark, path, vars ->
                _uiState.update { it.copy(isDarkTheme = dark, savePath = path, globalVariables = vars) }
            }.collect()
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.updateDarkTheme(enabled) }
    }

    fun setSavePath(path: String) {
        viewModelScope.launch { settingsDataStore.updateSavePath(path) }
    }

    fun updateGlobalVar(key: String, value: String) {
        viewModelScope.launch {
            val current = _uiState.value.globalVariables.toMutableMap()
            if (value.isEmpty()) current.remove(key) else current[key] = value
            settingsDataStore.updateGlobalVariables(current)
        }
    }
    
    fun deleteGlobalVar(key: String) {
        viewModelScope.launch {
            val current = _uiState.value.globalVariables.toMutableMap()
            current.remove(key)
            settingsDataStore.updateGlobalVariables(current)
        }
    }
}
