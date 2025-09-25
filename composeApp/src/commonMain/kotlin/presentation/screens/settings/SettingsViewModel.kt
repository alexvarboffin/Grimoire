package presentation.screens.settings

import data.settings.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import theme.ThemeManager

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val savePath: String = "",
    val appVersion: String = "1.0.0",
    val deviceInfo: String = "Desktop",
    val osInfo: String = System.getProperty("os.name") + " " + System.getProperty("os.version")
)

class SettingsViewModel(
    private val themeManager: ThemeManager,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState(isDarkTheme = themeManager.isDarkTheme.value))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.savePath.collect { path ->
                _uiState.update { it.copy(savePath = path) }
            }
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        _uiState.update { it.copy(isDarkTheme = isDark) }
        themeManager.setDarkTheme(isDark)
    }

    fun setSavePath(path: String) {
        viewModelScope.launch {
            settingsDataStore.setSavePath(path)
        }
    }
}