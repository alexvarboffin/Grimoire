package data.repository

import data.local.ListGeneratorProjectDao
import data.local.ListGeneratorProjectEntity
import domain.model.ListGeneratorProject
import domain.repository.ListGeneratorProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ListGeneratorProjectRepositoryImpl(private val projectDao: ListGeneratorProjectDao) : ListGeneratorProjectRepository {

    override fun getAllProjects(): Flow<List<ListGeneratorProject>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { it.toListGeneratorProject() }
        }
    }

    override suspend fun getProjectById(id: Long): ListGeneratorProject? {
        return projectDao.getProjectById(id)?.toListGeneratorProject()
    }

    override suspend fun insertProject(project: ListGeneratorProject): Long {
        return projectDao.insertProject(ListGeneratorProjectEntity.fromListGeneratorProject(project))
    }

    override suspend fun updateProject(project: ListGeneratorProject) {
        projectDao.updateProject(ListGeneratorProjectEntity.fromListGeneratorProject(project))
    }

    override suspend fun deleteProject(project: ListGeneratorProject) {
        projectDao.deleteProject(ListGeneratorProjectEntity.fromListGeneratorProject(project))
    }
}
