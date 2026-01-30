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
    val packageName: Flow<String> = dataStore.data.map { it[PreferencesKeys.PACKAGE_NAME] ?: "ru.lds.online.v1" }
    val shouldRebuild: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOULD_REBUILD] ?: false }

    val library: Flow<String> = dataStore.data.map { it[PreferencesKeys.LIBRARY] ?: "jvm-ktor" }
    val serializationLibrary: Flow<String> = dataStore.data.map { it[PreferencesKeys.SERIALIZATION] ?: "kotlinx_serialization" }
    val useSealedClasses: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.USE_SEALED] ?: true }
    val oneOfInterfaces: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.ONE_OF_INTERFACES] ?: true }

    suspend fun resetCodegenSettings() {
        dataStore.edit {
            it.remove(PreferencesKeys.JAVA_PATH)
            it.remove(PreferencesKeys.SPEC_PATH)
            it.remove(PreferencesKeys.OUTPUT_PATH)
            it.remove(PreferencesKeys.PACKAGE_NAME)
            it.remove(PreferencesKeys.SHOULD_REBUILD)
            it.remove(PreferencesKeys.LIBRARY)
            it.remove(PreferencesKeys.SERIALIZATION)
            it.remove(PreferencesKeys.USE_SEALED)
            it.remove(PreferencesKeys.ONE_OF_INTERFACES)
        }
    }

    suspend fun updateCodegenSettings(
        java: String,
        spec: String,
        output: String,
        pkg: String,
        rebuild: Boolean,
        lib: String,
        serialization: String,
        useSealed: Boolean,
        oneOf: Boolean
    ) {
        dataStore.edit {
            it[PreferencesKeys.JAVA_PATH] = java
            it[PreferencesKeys.SPEC_PATH] = spec
            it[PreferencesKeys.OUTPUT_PATH] = output
            it[PreferencesKeys.PACKAGE_NAME] = pkg
            it[PreferencesKeys.SHOULD_REBUILD] = rebuild
            it[PreferencesKeys.LIBRARY] = lib
            it[PreferencesKeys.SERIALIZATION] = serialization
            it[PreferencesKeys.USE_SEALED] = useSealed
            it[PreferencesKeys.ONE_OF_INTERFACES] = oneOf
        }
    }

    private object PreferencesKeys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val SAVE_PATH = stringPreferencesKey("save_path")
        val JAVA_PATH = stringPreferencesKey("codegen_java_path")
        val SPEC_PATH = stringPreferencesKey("codegen_spec_path")
        val OUTPUT_PATH = stringPreferencesKey("codegen_output_path")
        val PACKAGE_NAME = stringPreferencesKey("codegen_package_name")
        val SHOULD_REBUILD = booleanPreferencesKey("codegen_should_rebuild")

        val LIBRARY = stringPreferencesKey("codegen_library")
        val SERIALIZATION = stringPreferencesKey("codegen_serialization")
        val USE_SEALED = booleanPreferencesKey("codegen_use_sealed")
        val ONE_OF_INTERFACES = booleanPreferencesKey("codegen_one_of_interfaces")
    }
} 