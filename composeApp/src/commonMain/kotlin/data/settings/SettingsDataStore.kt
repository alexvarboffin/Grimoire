package data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val dataStore: DataStore<Preferences>) {
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_THEME] ?: false
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    val savePath: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SAVE_PATH] ?: ""
    }

    suspend fun setSavePath(path: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVE_PATH] = path
        }
    }

    // Codegen settings
    val javaPath: Flow<String> = dataStore.data.map { it[PreferencesKeys.JAVA_PATH] ?: "C:\\Program Files\\Java\\jdk-17\\bin\\java.exe" }
    val specPath: Flow<String> = dataStore.data.map { it[PreferencesKeys.SPEC_PATH] ?: "" }
    val outputPath: Flow<String> = dataStore.data.map { it[PreferencesKeys.OUTPUT_PATH] ?: "" }
    val packageName: Flow<String> = dataStore.data.map { it[PreferencesKeys.PACKAGE_NAME] ?: "a.a.a" }

    suspend fun updateCodegenSettings(java: String, spec: String, output: String, pkg: String) {
        dataStore.edit {
            it[PreferencesKeys.JAVA_PATH] = java
            it[PreferencesKeys.SPEC_PATH] = spec
            it[PreferencesKeys.OUTPUT_PATH] = output
            it[PreferencesKeys.PACKAGE_NAME] = pkg
        }
    }

    private object PreferencesKeys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val SAVE_PATH = stringPreferencesKey("save_path")
        val JAVA_PATH = stringPreferencesKey("codegen_java_path")
        val SPEC_PATH = stringPreferencesKey("codegen_spec_path")
        val OUTPUT_PATH = stringPreferencesKey("codegen_output_path")
        val PACKAGE_NAME = stringPreferencesKey("codegen_package_name")
    }
} 