package data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import domain.model.ListGeneratorProject
import kotlinx.serialization.Serializable

@Entity(tableName = "list_generator_projects")
@Serializable
data class ListGeneratorProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val templatePath: String,
    val variableValues: String
) {
    fun toListGeneratorProject(): ListGeneratorProject = ListGeneratorProject(
        id = id,
        name = name,
        templatePath = templatePath,
        variableValues = variableValues
    )

    companion object {
        fun fromListGeneratorProject(project: ListGeneratorProject): ListGeneratorProjectEntity = ListGeneratorProjectEntity(
            id = project.id,
            name = project.name,
            templatePath = project.templatePath,
            variableValues = project.variableValues
        )
    }
}
