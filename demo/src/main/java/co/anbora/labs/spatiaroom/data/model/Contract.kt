package co.anbora.labs.spatiaroom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contract(
    @PrimaryKey(autoGenerate = true)
    var localId: Long = 0,
    val name: String,
    val id: Long,
    val machine_id: Long,
    var isWaitingSync: Boolean = false
)