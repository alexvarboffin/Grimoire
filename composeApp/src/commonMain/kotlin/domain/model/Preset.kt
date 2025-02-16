package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Preset(
    val id: Long = 0,
    val name: String,
    val targetDirectory: String,
    val fileExtensions: List<String>,
    val replacements: List<TextReplacement>
)

@Serializable
data class TextReplacement(
    val id: Long = 0,
    val searchPattern: String,
    val replacement: String,
    val isRegex: Boolean = false
) 