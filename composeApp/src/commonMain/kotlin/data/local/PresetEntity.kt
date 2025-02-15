package data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import domain.model.Preset
import domain.model.TextReplacement
import kotlinx.serialization.Serializable

@Entity(tableName = "presets")
@Serializable
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetDirectory: String,
    val fileExtensions: List<String>,
    val replacements: List<TextReplacement>
) {
    fun toPreset(): Preset = Preset(
        id = id,
        name = name,
        targetDirectory = targetDirectory,
        fileExtensions = fileExtensions,
        replacements = replacements
    )

    companion object {
        fun fromPreset(preset: Preset): PresetEntity = PresetEntity(
            id = preset.id,
            name = preset.name,
            targetDirectory = preset.targetDirectory,
            fileExtensions = preset.fileExtensions,
            replacements = preset.replacements
        )
    }
} 