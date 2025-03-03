import kotlinx.serialization.Serializable

@Serializable
data class TextReplacement(
    val searchPattern: String,
    val replacement: String,
    val isRegex: Boolean = false
) 