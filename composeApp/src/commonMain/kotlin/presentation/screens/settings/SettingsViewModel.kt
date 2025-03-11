package presentation.screens.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import moe.tlaster.precompose.viewmodel.ViewModel
import theme.ThemeManager

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val appVersion: String = "1.0.0",
    val deviceInfo: String = "Desktop",
    val osInfo: String = System.getProperty("os.name") + " " + System.getProperty("os.version")
)

class SettingsViewModel(
    private val themeManager: ThemeManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState(isDarkTheme = themeManager.isDarkTheme.value))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setDarkTheme(isDark: Boolean) {
        _uiState.update { it.copy(isDarkTheme = isDark) }
        themeManager.setDarkTheme(isDark)
    }
} 