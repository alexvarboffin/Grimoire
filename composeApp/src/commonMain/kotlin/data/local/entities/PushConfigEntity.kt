package data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "push_configs")
data class PushConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val serviceAccountJsonPath: String,
    val createdAt: Long = System.currentTimeMillis()
)
