package data.local

import androidx.room.*
import data.local.entities.CommandPipeline
import data.local.entities.PipelineStep
import data.local.entities.CommandPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandPipelineDao {
    @Query("SELECT * FROM command_pipelines")
    fun getAllPipelines(): Flow<List<CommandPipeline>>

    @Transaction
    @Query("SELECT * FROM pipeline_steps WHERE pipelineId = :pipelineId ORDER BY sequenceOrder")
    suspend fun getStepsForPipeline(pipelineId: Long): List<PipelineStep>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPipeline(pipeline: CommandPipeline): Long

    @Insert
    suspend fun insertStep(step: PipelineStep)

    @Query("DELETE FROM pipeline_steps WHERE pipelineId = :pipelineId")
    suspend fun deleteStepsForPipeline(pipelineId: Long)

    @Delete
    suspend fun deletePipeline(pipeline: CommandPipeline)
}
