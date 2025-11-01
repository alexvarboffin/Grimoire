package presentation.screens.list_generator.project_list

import domain.model.ListGeneratorProject
import domain.repository.ListGeneratorProjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class ListGeneratorProjectListViewModel(
    private val projectRepository: ListGeneratorProjectRepository
) : ViewModel() {

    val projects = projectRepository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteProject(project: ListGeneratorProject) {
        viewModelScope.launch {
            projectRepository.deleteProject(project)
        }
    }

    fun renameProject(project: ListGeneratorProject, newName: String) {
        viewModelScope.launch {
            val updatedProject = project.copy(name = newName)
            projectRepository.updateProject(updatedProject)
        }
    }
}
