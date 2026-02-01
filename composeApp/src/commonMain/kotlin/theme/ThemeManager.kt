package theme

import data.settings.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeManager(
    private val settingsDataStore: SettingsDataStore,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    val isDarkTheme: StateFlow<Boolean> = settingsDataStore.isDarkTheme
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun setDarkTheme(isDark: Boolean) {
        scope.launch {
            settingsDataStore.updateDarkTheme(isDark)
        }
    }
} 