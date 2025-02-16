package presentation.preset.list

import domain.model.Preset
import domain.repository.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import util.FileProcessor


class PresetListViewModel(
    private val presetRepository: PresetRepository,
    private val fileProcessor: FileProcessor
) : ViewModel()
{
    private val _presets = MutableStateFlow<List<Preset>>(emptyList())
    val presets: StateFlow<List<Preset>> = _presets.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    init {
        viewModelScope.launch {
            presetRepository.getAllPresets().collect { presetList ->
                _presets.value = presetList
            }
        }
    }

    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            presetRepository.deletePreset(preset)
        }
    }

    fun applyPreset(preset: Preset) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                fileProcessor.processPreset(preset)
            } catch (e: Exception) {
                // TODO: Добавить обработку ошибок
                println("Ошибка при применении пресета: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
} 