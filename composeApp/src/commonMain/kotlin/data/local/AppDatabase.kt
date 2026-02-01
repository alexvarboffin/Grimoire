package data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PresetEntity::class, 
        ListGeneratorProjectEntity::class, 
        data.local.entities.CommandPreset::class,
        data.local.entities.CommandHistory::class,
        data.local.entities.CommandPipeline::class,
        data.local.entities.PipelineStep::class
    ],
    version = 5
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao
    abstract fun listGeneratorProjectDao(): ListGeneratorProjectDao
    abstract fun commandPresetDao(): CommandPresetDao
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun commandPipelineDao(): CommandPipelineDao
}