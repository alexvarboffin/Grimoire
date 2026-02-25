package data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "push_devices",
    foreignKeys = [
        ForeignKey(
            entity = PushConfigEntity::class,
            parentColumns = ["id"],
            childColumns = ["configId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("configId")]
)
data class PushDeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val configId: Long,
    val name: String,
    val token: String,
    val createdAt: Long = System.currentTimeMillis()
)
