package presentation.preset.list

import domain.model.Preset
import domain.repository.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope


class PresetListViewModel(private val presetRepository: PresetRepository) : ViewModel()
{
    private val _presets = MutableStateFlow<List<Preset>>(emptyList())
    val presets: StateFlow<List<Preset>> = _presets.asStateFlow()

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
} 