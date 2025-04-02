package presentation.screens.tomlmerger

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import domain.model.TomlConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import okio.FileSystem
import okio.Path.Companion.toPath

data class TomlMergerUiState(
    val sourceFilePath: String = "",
    val targetFilePath: String = "",
    val lastUsedSourcePath: String? = null,
    val lastUsedTargetPath: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val showSourceFilePicker: Boolean = false,
    val showTargetFilePicker: Boolean = false
)

class TomlMergerViewModel(
    private val fileSystem: FileSystem
) : ViewModel() {
    private val _uiState = MutableStateFlow(TomlMergerUiState())
    val uiState: StateFlow<TomlMergerUiState> = _uiState.asStateFlow()

    fun showSourceFilePicker() {
        _uiState.update { it.copy(showSourceFilePicker = true) }
    }

    fun showTargetFilePicker() {
        _uiState.update { it.copy(showTargetFilePicker = true) }
    }

    fun hideSourceFilePicker() {
        _uiState.update { it.copy(showSourceFilePicker = false) }
    }

    fun hideTargetFilePicker() {
        _uiState.update { it.copy(showTargetFilePicker = false) }
    }

    fun setSourceFile(path: String) {
        _uiState.update { 
            it.copy(
                sourceFilePath = path,
                lastUsedSourcePath = path,
                showSourceFilePicker = false
            )
        }
    }

    fun setTargetFile(path: String) {
        _uiState.update { 
            it.copy(
                targetFilePath = path,
                lastUsedTargetPath = path,
                showTargetFilePicker = false
            )
        }
    }

    fun mergeFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = null) }
            
            try {
                val sourcePath = _uiState.value.sourceFilePath.toPath()
                val targetPath = _uiState.value.targetFilePath.toPath()
                
                // Проверяем существование файлов
                if (!fileSystem.exists(sourcePath)) {
                    throw IllegalArgumentException("Исходный файл не найден")
                }
                if (!fileSystem.exists(targetPath)) {
                    throw IllegalArgumentException("Целевой файл не найден")
                }

                // Читаем TOML файлы
                val toml = Toml(TomlInputConfig(ignoreUnknownNames = true))
                val sourceToml = toml.decodeFromString(
                    TomlConfig.serializer(),
                    fileSystem.read(sourcePath) { readUtf8() }
                )
                val targetToml = toml.decodeFromString(
                    TomlConfig.serializer(),
                    fileSystem.read(targetPath) { readUtf8() }
                )

                // Объединяем версии
                val targetDeps = targetToml.versions.toMutableMap()
                var addedCount = 0
                
                sourceToml.versions.forEach { (key, version1) ->
                    if (key !in targetDeps) {
                        targetDeps[key] = version1
                        addedCount++
                    } else if (targetDeps[key] != version1) {
                        targetDeps[key] = compareVersions(version1, targetDeps[key] ?: "")
                    }
                }

                // Формируем новый TOML с сортировкой
                val tomlContent = buildString {
                    appendLine("[versions]")
                    // Сортируем версии по ключам
                    targetDeps.toSortedMap().forEach { (key, value) ->
                        appendLine("$key = \"$value\"")
                    }
                    
                    appendLine("\n[libraries]")
                    // Сортируем библиотеки по именам
                    sourceToml.libraries.toSortedMap().forEach { (libName, libConfig) ->
                        append("$libName = {")
                        val entries = libConfig.entries.toList().sortedBy { it.key }
                        entries.forEachIndexed { index, entry ->
                            if (entry.value is LinkedHashMap) {
                                var o = entry.key
                                (entry.value as Map<*, *>).entries.sortedBy { it.key.toString() }
                                    .forEachIndexed { innerIndex, innerEntry ->
                                        o += "." + innerEntry.key
                                        append(" $o = \"${innerEntry.value}\"${if ((innerIndex < entry.value.entries.size - 1) && index < entries.size - 1) "," else ","}")
                                    }
                            } else {
                                append(" ${entry.key} = \"${entry.value}\"${if (index < entries.size - 1) "," else ""}")
                            }
                        }
                        append("}\n")
                    }
                    
                    appendLine("\n[plugins]")
                    // Сортируем плагины по именам
                    sourceToml.plugins.toSortedMap().forEach { (libName, libConfig) ->
                        append("$libName = {")
                        val entries = libConfig.entries.toList().sortedBy { it.key }
                        entries.forEachIndexed { index, entry ->
                            if (entry.value is LinkedHashMap) {
                                var o = entry.key
                                (entry.value as Map<*, *>).entries.sortedBy { it.key.toString() }
                                    .forEachIndexed { innerIndex, innerEntry ->
                                        o += "." + innerEntry.key
                                        append(" $o = \"${innerEntry.value}\"${if ((innerIndex < entry.value.entries.size - 1) && index < entries.size - 1) "," else ","}")
                                    }
                            } else {
                                append(" ${entry.key} = \"${entry.value}\"${if (index < entries.size - 1) "," else ""}")
                            }
                        }
                        append("}\n")
                    }
                }

                // Сохраняем результат
                val resultPath = targetPath.parent?.resolve("libs.versions-merged.toml") 
                    ?: throw IllegalStateException("Не удалось создать путь для результата")
                
                fileSystem.write(resultPath) {
                    writeUtf8(tomlContent)
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        success = "🎉 Файл успешно обновлен! Добавлено версий: $addedCount"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "❌ Ошибка: ${e.message}"
                    )
                }
            }
        }
    }

    private fun compareVersions(version1: String, version2: String): String {
        val result = version1.compareTo(version2)
        return when {
            result > 0 -> version1
            result < 0 -> version2
            else -> version1
        }
    }
} 