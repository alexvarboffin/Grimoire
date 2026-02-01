package data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PresetEntity::class, ListGeneratorProjectEntity::class, data.local.entities.CommandPreset::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao
    abstract fun listGeneratorProjectDao(): ListGeneratorProjectDao
    abstract fun commandPresetDao(): CommandPresetDao
}