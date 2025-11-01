package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ListGeneratorProject(
    val id: Long = 0,
    val name: String,
    val templatePath: String,
    val variableValues: String
)
