package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VariableValue(
    val stringValue: String? = null,
    val listValue: List<String>? = null
)
