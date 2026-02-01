package data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetId: Long,
    val commandName: String,
    val fullCommand: String,
    val timestamp: Long = System.currentTimeMillis(),
    val exitCode: Int?,
    val logs: String,
    val status: String // "SUCCESS", "FAILED", "RUNNING"
)
