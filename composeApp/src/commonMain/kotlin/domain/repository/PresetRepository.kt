package domain.repository

import domain.model.Preset
import kotlinx.coroutines.flow.Flow

interface PresetRepository {
    fun getAllPresets(): Flow<List<Preset>>
    suspend fun getPresetById(id: Long): Preset?
    suspend fun insertPreset(preset: Preset): Long
    suspend fun updatePreset(preset: Preset)
    suspend fun deletePreset(preset: Preset)
} 