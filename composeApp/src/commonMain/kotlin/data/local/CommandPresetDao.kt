package data.local

import androidx.room.*
import data.local.entities.CommandPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandPresetDao {
    @Query("SELECT * FROM command_presets ORDER BY groupName, subGroupName, name")
    fun getAllPresets(): Flow<List<CommandPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CommandPreset)

    @Delete
    suspend fun deletePreset(preset: CommandPreset)

    @Update
    suspend fun updatePreset(preset: CommandPreset)
}
