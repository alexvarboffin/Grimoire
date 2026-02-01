package data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_pipelines")
data class CommandPipeline(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = ""
)

@Entity(tableName = "pipeline_steps")
data class PipelineStep(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pipelineId: Long,
    val presetId: Long,
    val sequenceOrder: Int
)
