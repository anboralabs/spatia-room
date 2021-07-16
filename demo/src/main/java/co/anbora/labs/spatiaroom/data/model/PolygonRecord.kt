package co.anbora.labs.spatiaroom.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.locationtech.jts.geom.MultiPolygon

@Entity(tableName = "honitby")
data class PolygonRecord(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "nazev") val name: String?,
    @ColumnInfo(name = "kod")   val code: String?,
    val geom: MultiPolygon?
)