package presentation.preset.edit

import domain.model.Preset
import domain.model.TextReplacement
import domain.repository.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class PresetEditViewModel(

    private val presetRepository: PresetRepository,
    private val presetId: Long?
) : ViewModel() {
    private val _state = MutableStateFlow(PresetEditState())
    val state: StateFlow<PresetEditState> = _state.asStateFlow()

    init {
        if (presetId != null) {
            loadPreset(presetId)
        }
    }

    private fun loadPreset(id: Long) {
        viewModelScope.launch {
            presetRepository.getPresetById(id)?.let { preset ->
                _state.value = PresetEditState(
                    name = preset.name,
                    targetDirectory = preset.targetDirectory,
                    fileExtensions = preset.fileExtensions,
                    replacements = preset.replacements
                )
            }
        }
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updateTargetDirectory(directory: String) {
        _state.value = _state.value.copy(targetDirectory = directory)
    }

    fun addFileExtension(extension: String) {
        val currentExtensions = _state.value.fileExtensions.toMutableList()
        if (extension.isNotBlank() && !currentExtensions.contains(extension)) {
            currentExtensions.add(extension)
            _state.value = _state.value.copy(fileExtensions = currentExtensions)
        }
    }

    fun removeFileExtension(extension: String) {
        val currentExtensions = _state.value.fileExtensions.toMutableList()
        currentExtensions.remove(extension)
        _state.value = _state.value.copy(fileExtensions = currentExtensions)
    }

    fun addReplacement(searchPattern: String, replacement: String) {
        if (searchPattern.isNotBlank()) {
            val newReplacement = TextReplacement(
                id = System.currentTimeMillis(),
                searchPattern = searchPattern,
                replacement = replacement
            )
            val currentReplacements = _state.value.replacements.toMutableList()
            currentReplacements.add(newReplacement)
            _state.value = _state.value.copy(replacements = currentReplacements)
        }
    }

    fun removeReplacement(replacement: TextReplacement) {
        val currentReplacements = _state.value.replacements.toMutableList()
        currentReplacements.remove(replacement)
        _state.value = _state.value.copy(replacements = currentReplacements)
    }

    fun savePreset() {
        viewModelScope.launch {
            val preset = Preset(
                id = presetId ?: 0,
                name = _state.value.name,
                targetDirectory = _state.value.targetDirectory,
                fileExtensions = _state.value.fileExtensions,
                replacements = _state.value.replacements
            )
            if (presetId == null) {
                presetRepository.insertPreset(preset)
            } else {
                presetRepository.updatePreset(preset)
            }
        }
    }
}

data class PresetEditState(
    val name: String = "",
    val targetDirectory: String = "",
    val fileExtensions: List<String> = emptyList(),
    val replacements: List<TextReplacement> = emptyList()
) 