package data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_presets")
data class CommandPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupName: String,
    val subGroupName: String = "",
    val name: String,
    val executablePath: String,
    val arguments: String,
    val workingDir: String,
    val description: String = ""
)
