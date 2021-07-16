package co.anbora.labs.spatiaroom.data.converter

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.io.WKTReader

@TypeConverters
object PolygonDataConverters {

    @TypeConverter
    @JvmStatic
    fun toMultiPolygon(wkt: String): MultiPolygon {
        val reader = WKTReader()
        val geometry = reader.read(wkt)
        return geometry as MultiPolygon
    }

    // Writing geometry is not supported but Android requires
    // this to be here.
    @TypeConverter
    @JvmStatic
    fun fromMultiPolygon(dummy : MultiPolygon) = ""
}