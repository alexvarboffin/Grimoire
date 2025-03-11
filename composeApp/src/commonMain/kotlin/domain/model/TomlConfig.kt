package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TomlConfig(
    val versions: Map<String, String> = emptyMap(),
    val libraries: Map<String, Map<String, Map<String, Map<String, Long>>>> = emptyMap(), // Изменено для поддержки вложенных таблиц
    val plugins: Map<String, Map<String, Map<String, Map<String, Long>>>> = emptyMap()
)