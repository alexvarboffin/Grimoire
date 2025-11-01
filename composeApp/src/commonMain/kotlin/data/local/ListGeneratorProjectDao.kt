package data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListGeneratorProjectDao {
    @Query("SELECT * FROM list_generator_projects")
    fun getAllProjects(): Flow<List<ListGeneratorProjectEntity>>

    @Query("SELECT * FROM list_generator_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ListGeneratorProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ListGeneratorProjectEntity): Long

    @Update
    suspend fun updateProject(project: ListGeneratorProjectEntity)

    @Delete
    suspend fun deleteProject(project: ListGeneratorProjectEntity)
}
