package domain.repository

import domain.model.ListGeneratorProject
import kotlinx.coroutines.flow.Flow

interface ListGeneratorProjectRepository {
    fun getAllProjects(): Flow<List<ListGeneratorProject>>
    suspend fun getProjectById(id: Long): ListGeneratorProject?
    suspend fun insertProject(project: ListGeneratorProject): Long
    suspend fun updateProject(project: ListGeneratorProject)
    suspend fun deleteProject(project: ListGeneratorProject)
}
