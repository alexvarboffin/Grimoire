package presentation.screens.commands

import data.local.entities.CommandPreset
import data.local.entities.CommandPipeline
import data.local.entities.PipelineStep
import kotlinx.serialization.Serializable

@Serializable
data class CommandExportModel(
    val version: Int = 1,
    val presets: List<CommandPresetExport> = emptyList(),
    val pipelines: List<CommandPipelineExport> = emptyList()
)

@Serializable
data class CommandPresetExport(
    val groupName: String,
    val subGroupName: String = "",
    val name: String,
    val executablePath: String,
    val arguments: String,
    val workingDir: String,
    val description: String = ""
)

@Serializable
data class CommandPipelineExport(
    val name: String,
    val description: String = "",
    val stepPresetNames: List<String> = emptyList() // Ссылаемся по имени для надежности при импорте
)
