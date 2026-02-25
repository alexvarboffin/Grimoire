package presentation.screens.push

import kotlinx.serialization.Serializable

@Serializable
data class PushExportModel(
    val version: Int = 1,
    val configs: List<PushConfigExport> = emptyList()
)

@Serializable
data class PushConfigExport(
    val name: String,
    val serviceAccountJsonPath: String,
    val pushData: Map<String, String> = emptyMap(),
    val devices: List<PushDeviceExport> = emptyList()
)

@Serializable
data class PushDeviceExport(
    val name: String,
    val token: String
)
