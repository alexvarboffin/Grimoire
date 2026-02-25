package data.local

import androidx.room.*
import data.local.entities.PushDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PushDeviceDao {
    @Query("SELECT * FROM push_devices WHERE configId = :configId ORDER BY createdAt DESC")
    fun getDevicesByConfigId(configId: Long): Flow<List<PushDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: PushDeviceEntity): Long

    @Delete
    suspend fun deleteDevice(device: PushDeviceEntity)

    @Update
    suspend fun updateDevice(device: PushDeviceEntity)
}
