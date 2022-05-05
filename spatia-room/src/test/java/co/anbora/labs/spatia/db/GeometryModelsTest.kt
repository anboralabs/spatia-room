package co.anbora.labs.spatia.db

import co.anbora.labs.spatia.geometry.LineString
import co.anbora.labs.spatia.geometry.Point
import co.anbora.labs.spatia.geometry.Polygon
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GeometryModelsTest {
    @Test
    fun testPoint() {
        val point = Point(-122.084801, 37.422131)
        assertEquals(point.x, point.mbr.minX)
        assertEquals(point.y, point.mbr.minY)
        assertEquals(point.x, point.mbr.maxX)
        assertEquals(point.y, point.mbr.maxY)
        assertEquals(4326, point.srid)
    }

    @Test
    fun testLineString() {
        val lineString = LineString(listOf(
            Point(-122.080699, 37.426734),
            Point(-122.084801, 37.422131),
            Point(-122.088006, 37.418401),
        ))
        assertEquals(lineString.points[2].x, lineString.mbr.minX)
        assertEquals(lineString.points[2].y, lineString.mbr.minY)
        assertEquals(lineString.points[0].x, lineString.mbr.maxX)
        assertEquals(lineString.points[0].y, lineString.mbr.maxY)
        assertEquals(4326, lineString.srid)
    }

    @Test
    fun testPolygon() {
        val polygon = Polygon(LineString(listOf(
            Point(-122.080699, 37.426734),
            Point(-122.084801, 37.422131),
            Point(-122.088006, 37.418401),
            Point(-122.080699, 37.426734),
        )))
        assertEquals(polygon.exteriorRing.points[2].x, polygon.mbr.minX)
        assertEquals(polygon.exteriorRing.points[2].y, polygon.mbr.minY)
        assertEquals(polygon.exteriorRing.points[0].x, polygon.mbr.maxX)
        assertEquals(polygon.exteriorRing.points[0].y, polygon.mbr.maxY)
        assertEquals(4326, polygon.srid)
    }
}