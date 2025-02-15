package data.repository

import data.local.PresetDao
import data.local.PresetEntity
import domain.model.Preset
import domain.repository.PresetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PresetRepositoryImpl(
    val presetDao: PresetDao
) : PresetRepository {

    override fun getAllPresets(): Flow<List<Preset>> {
        return presetDao.getAllPresets().map { entities ->
            entities.map { it.toPreset() }
        }
    }

    override suspend fun getPresetById(id: Long): Preset? {
        return presetDao.getPresetById(id)?.toPreset()
    }

    override suspend fun insertPreset(preset: Preset): Long {
        return presetDao.insertPreset(PresetEntity.fromPreset(preset))
    }

    override suspend fun updatePreset(preset: Preset) {
        presetDao.updatePreset(PresetEntity.fromPreset(preset))
    }

    override suspend fun deletePreset(preset: Preset) {
        presetDao.deletePreset(PresetEntity.fromPreset(preset))
    }

    private fun PresetEntity.toPreset(): Preset {
        return Preset(
            id = id,
            name = name,
            targetDirectory = targetDirectory,
            fileExtensions = fileExtensions,
            replacements = replacements
        )
    }
} 