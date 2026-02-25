package data.local

import androidx.room.*
import data.local.entities.PushConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PushConfigDao {
    @Query("SELECT * FROM push_configs ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<PushConfigEntity>>

    @Query("SELECT * FROM push_configs WHERE id = :id")
    suspend fun getConfigById(id: Long): PushConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: PushConfigEntity): Long

    @Delete
    suspend fun deleteConfig(config: PushConfigEntity)
}
