package co.anbora.labs.spatia.db

import co.anbora.labs.spatia.geometry.*
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

    @Test
    fun testMultiPoint() {
        val multiPoint = MultiPoint(listOf(
            Point(-122.080699, 37.426734),
            Point(-122.084801, 37.422131),
            Point(-122.088006, 37.418401)
        ))
        val binary = converters.fromMultiPoint(multiPoint)
        assertEquals(multiPoint, converters.toMultiPoint(binary))
    }

    @Test
    fun testMultiLineString() {
        val multiLineString = MultiLineString(listOf(
            LineString(listOf(
                Point(-122.080699, 37.426734),
                Point(-122.084801, 37.422131),
                Point(-122.088006, 37.418401),
            )),
            LineString(listOf(
                Point(-122.084801, 37.422131),
                Point(-122.088006, 37.418401),
                Point(-122.080699, 37.426734),
            ))
        ))
        val binary = converters.fromMultiLineString(multiLineString)
        assertEquals(multiLineString, converters.toMultiLineString(binary))
    }

    @Test
    fun testMultiPolygon() {
        val multiPolygon = MultiPolygon(listOf(
            Polygon(LineString(listOf(
                Point(-122.080699, 37.426734),
                Point(-122.084801, 37.422131),
                Point(-122.088006, 37.418401),
                Point(-122.080699, 37.426734),
            ))),
            Polygon(LineString(listOf(
                Point(-122.080699, 37.426734),
                Point(-122.088006, 37.418401),
                Point(-122.084801, 37.422131),
                Point(-122.080699, 37.426734),
            )))
        ))
        val binary = converters.fromMultiPolygon(multiPolygon)
        assertEquals(multiPolygon, converters.toMultiPolygon(binary))
    }

    @Test
    fun testGeometryCollection() {
        val geometryCollection = GeometryCollection(listOf(
            Point(-122.084801, 37.422131),
            LineString(listOf(
                Point(-122.080699, 37.426734),
                Point(-122.084801, 37.422131),
                Point(-122.088006, 37.418401),
                Point(-122.080699, 37.426734),
            )),
            Polygon(LineString(listOf(
                Point(-122.080699, 37.426734),
                Point(-122.088006, 37.418401),
                Point(-122.084801, 37.422131),
                Point(-122.080699, 37.426734),
            )))
        ))
        val binary = converters.fromGeometryCollection(geometryCollection)
        assertEquals(geometryCollection, converters.toGeometryCollection(binary))
    }
}
