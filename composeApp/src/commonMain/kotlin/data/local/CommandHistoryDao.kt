package data.local

import androidx.room.*
import data.local.entities.CommandHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandHistoryDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<CommandHistory>>

    @Insert
    suspend fun insertHistory(history: CommandHistory): Long

    @Update
    suspend fun updateHistory(history: CommandHistory)

    @Query("DELETE FROM command_history")
    suspend fun clearHistory()
}
