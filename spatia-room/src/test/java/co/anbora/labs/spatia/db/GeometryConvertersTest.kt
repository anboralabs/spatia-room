package co.anbora.labs.spatia.db

import co.anbora.labs.spatia.geometry.GeometryConverters
import co.anbora.labs.spatia.geometry.LineString
import co.anbora.labs.spatia.geometry.Point
import co.anbora.labs.spatia.geometry.Polygon
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GeometryConvertersTest {
    val converters = GeometryConverters()

    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    @Test
    fun testPoint() {
        val point = Point(-122.084801, 37.422131)
        val binary = converters.fromPoint(point)
        assertEquals(point, converters.toPoint(binary))
    }

    @Test
    fun testLineString() {
        val lineString = LineString(listOf(
            Point(-122.080699, 37.426734),
            Point(-122.084801, 37.422131),
            Point(-122.088006, 37.418401),
        ))
        val binary = converters.fromLineString(lineString)
        assertEquals(lineString, converters.toLineString(binary))
    }

    @Test
    fun testPolygon() {
        val polygon = Polygon(LineString(listOf(
            Point(-122.080699, 37.426734),
            Point(-122.084801, 37.422131),
            Point(-122.088006, 37.418401),
            Point(-122.080699, 37.426734),
        )))
        val binary = converters.fromPolygon(polygon)
        assertEquals(polygon, converters.toPolygon(binary))
    }
}
