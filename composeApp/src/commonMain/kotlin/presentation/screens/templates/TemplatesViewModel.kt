package presentation.screens.templates

import androidx.compose.runtime.mutableStateOf
import domain.io.FileEntry
import domain.io.FileSystem
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlinx.coroutines.launch

class TemplatesViewModel(private val fileSystem: FileSystem) : ViewModel() {

    private val rootPath = "C:\\src\\Synced\\Grimoire\\Templates"

    val files = mutableStateOf<List<FileEntry>>(emptyList())
    val currentPath = mutableStateOf(rootPath)

    init {
        loadFiles(rootPath)
    }

    fun loadFiles(path: String) {
        viewModelScope.launch {
            currentPath.value = path
            files.value = fileSystem.listFiles(path)
        }
    }
    
    fun navigateUp() {
        val parentPath = currentPath.value.substringBeforeLast('\\')
        if (parentPath.isNotEmpty() && parentPath.startsWith(rootPath)) {
            loadFiles(parentPath)
        }
    }
}