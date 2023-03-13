package co.anbora.labs.spatia.geometry

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GeometryConverters {
    private fun writeGeometryHeader(geometry: Geometry, type: Int, buffer: ByteBuffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
            .put(0x00)  // start
            .put(0x01)  // byte order: little endian
            .putInt(geometry.srid)
            .putDouble(geometry.mbr.minX)
            .putDouble(geometry.mbr.minY)
            .putDouble(geometry.mbr.maxX)
            .putDouble(geometry.mbr.maxY)
            .put(0x7c)  // mbr_end
            .putInt(type)  // geometry type
    }

    private fun writeGeometryEnd(buffer: ByteBuffer) {
        buffer.put(0xfe.toByte())
    }

    private fun writePoint(buffer: ByteBuffer, point: Point) {
        buffer.putDouble(point.x)
            .putDouble(point.y)
    }

    private fun writeLineString(
        buffer: ByteBuffer,
        lineString: LineString
    ) {
        buffer.putInt(lineString.points.size)
        lineString.points.forEach {
            writePoint(buffer, it)
        }
    }

    private fun writePolygon(
        buffer: ByteBuffer,
        polygon: Polygon
    ) {
        buffer.putInt(polygon.rings.size)
        polygon.rings.forEach {
            writeLineString(buffer, it)
        }
    }

    private fun writeGeoCollection(
        buffer: ByteBuffer,
        collection: GeoCollection<*>
    ) {
        buffer.putInt(collection.items.size)
        collection.items.forEach {
            buffer.put(0x69)  // entity
            buffer.putInt(getType(it))
            writeGeometryBody(it, buffer)
        }
    }

    private fun readGeometryHeader(bytes: ByteArray): Triple<Int, Int, ByteBuffer> {
        assert(bytes[0] == 0x00.toByte())  // start

        val byteOrder = when (bytes[1]) {
            0x01.toByte() -> ByteOrder.LITTLE_ENDIAN
            0x00.toByte() -> ByteOrder.BIG_ENDIAN
            else -> {
                throw NotImplementedError("Unrecognized byte order: ${bytes[1]}")
            }
            // NOTE: Spatialite Version 5.0 adds two new possible values (0x80 and 0x81) here,
            // which identify the TinyPoint format. This is not yet implemented as we are only
            // using Spatialite 4.3. https://www.gaia-gis.it/gaia-sins/BLOB-TinyPoint.html
        }
        assert(bytes[38] == 0x7c.toByte())  // mbr_end
        assert(bytes.last() == 0xfe.toByte())  // end

        val buffer = ByteBuffer.wrap(bytes).order(byteOrder)
        val srid = buffer.getInt(2)

        // move to position 39, after mbr_end
        buffer.position(39)

        val type = buffer.getInt()
        return Triple(srid, type, buffer)
    }

    private fun readPoint(
        buffer: ByteBuffer,
        srid: Int
    ) = Point(buffer.getDouble(), buffer.getDouble(), srid)

    private fun readLineString(
        buffer: ByteBuffer,
        srid: Int
    ): LineString {
        val nPoints = buffer.getInt()
        val points = List(nPoints) { readPoint(buffer, srid) }
        return LineString(points)
    }

    private fun readPolygon(
        buffer: ByteBuffer,
        srid: Int
    ): Polygon {
        val nRings = buffer.getInt()
        val rings = List(nRings) { readLineString(buffer, srid) }
        return Polygon(rings)
    }

    private fun readGeoCollection(
        buffer: ByteBuffer,
        srid: Int
    ): List<Geometry> {
        val nItems = buffer.getInt()
        val items = List(nItems) {
            assert(buffer.get() == 0x69.toByte())  // entity
            val type = buffer.getInt()
            readGeometryBody(srid, type, buffer)
        }
        return items
    }

    private fun readGeometry(bytes: ByteArray): Geometry {
        val (srid, type, buffer) = readGeometryHeader(bytes)
        return readGeometryBody(srid, type, buffer)
    }

    private fun readGeometryBody(
        srid: Int,
        type: Int,
        buffer: ByteBuffer,
    ): Geometry {
        return when (type) {
            1 -> readPoint(buffer, srid)
            2 -> readLineString(buffer, srid)
            3 -> readPolygon(buffer, srid)
            4 -> MultiPoint(readGeoCollection(buffer, srid) as List<Point>)
            5 -> MultiLineString(readGeoCollection(buffer, srid) as List<LineString>)
            6 -> MultiPolygon(readGeoCollection(buffer, srid) as List<Polygon>)
            7 -> GeometryCollection(readGeoCollection(buffer, srid))
            else -> throw IllegalArgumentException("unsupported geometry type: $type")
        }
    }

    private fun calculateSize(geometry: Geometry): Int {
        val baseSize = 44  // header + end byte
        return baseSize + when (geometry) {
            is Point -> 16
            is LineString -> 4 + geometry.points.size * 16
            is Polygon -> 4 + geometry.rings.size * 4 + geometry.rings.sumOf { it.points.size } * 16
            is MultiPoint -> 4 + geometry.points.size * (5 + 16)
            is MultiLineString -> 4 + geometry.lineStrings.size * (5 + 4) + geometry.lineStrings.sumOf { it.points.size } * 16
            is MultiPolygon -> 4 + geometry.polygons.size * (5 + 4) + geometry.polygons.sumOf { it.rings.size * 4 + it.rings.sumOf { it.points.size } * 16 }
            is GeometryCollection -> 4 + geometry.geometries.size * 5 + geometry.geometries.sumOf {
                when (it) {
                    is Point -> 16
                    is LineString -> 4 + it.points.size * 16
                    is Polygon -> 4 + it.rings.size * 4 + it.rings.sumOf { it.points.size } * 16
                    else -> throw IllegalArgumentException("nested geometry collections are not allowed")
                }
            }
        }
    }

    private fun getType(geometry: Geometry) = when (geometry) {
        is Point -> 1
        is LineString -> 2
        is Polygon -> 3
        is MultiPoint -> 4
        is MultiLineString -> 5
        is MultiPolygon -> 6
        is GeometryCollection -> 7
    }

    private fun writeGeometry(geometry: Geometry): ByteArray {
        val buffer = ByteBuffer.allocate(calculateSize(geometry))
        val type = getType(geometry)
        writeGeometryHeader(geometry, type, buffer)
        writeGeometryBody(geometry, buffer)
        writeGeometryEnd(buffer)
        assert(!buffer.hasRemaining())
        return buffer.array()
    }

    private fun writeGeometryBody(geometry: Geometry, buffer: ByteBuffer) {
        when (geometry) {
            is Point -> writePoint(buffer, geometry)
            is LineString -> writeLineString(buffer, geometry)
            is Polygon -> writePolygon(buffer, geometry)
            is MultiPoint -> writeGeoCollection(buffer, geometry)
            is MultiLineString -> writeGeoCollection(buffer, geometry)
            is MultiPolygon ->  writeGeoCollection(buffer, geometry)
            is GeometryCollection ->  writeGeoCollection(buffer, geometry)
        }
    }

    @TypeConverter
    fun toPoint(bytes: ByteArray): Point {
        return readGeometry(bytes) as Point
    }

    @TypeConverter
    fun fromPoint(point: Point): ByteArray {
        return writeGeometry(point)
    }

    @TypeConverter
    fun toLineString(bytes: ByteArray): LineString {
        return readGeometry(bytes) as LineString
    }

    @TypeConverter
    fun fromLineString(lineString: LineString): ByteArray {
        return writeGeometry(lineString)
    }

    @TypeConverter
    fun toPolygon(bytes: ByteArray): Polygon {
        return readGeometry(bytes) as Polygon
    }

    @TypeConverter
    fun fromPolygon(polygon: Polygon): ByteArray {
        return writeGeometry(polygon)
    }

    @TypeConverter
    fun toMultiPoint(bytes: ByteArray): MultiPoint {
        return readGeometry(bytes) as MultiPoint
    }

    @TypeConverter
    fun fromMultiPoint(multiPoint: MultiPoint): ByteArray {
        return writeGeometry(multiPoint)
    }

    @TypeConverter
    fun toMultiLineString(bytes: ByteArray): MultiLineString {
        return readGeometry(bytes) as MultiLineString
    }

    @TypeConverter
    fun fromMultiLineString(multiLineString: MultiLineString): ByteArray {
        return writeGeometry(multiLineString)
    }

    @TypeConverter
    fun toMultiPolygon(bytes: ByteArray): MultiPolygon {
        return readGeometry(bytes) as MultiPolygon
    }

    @TypeConverter
    fun fromMultiPolygon(multiPolygon: MultiPolygon): ByteArray {
        return writeGeometry(multiPolygon)
    }

    @TypeConverter
    fun toGeometryCollection(bytes: ByteArray): GeometryCollection {
        return readGeometry(bytes) as GeometryCollection
    }

    @TypeConverter
    fun fromGeometryCollection(geometryCollection: GeometryCollection): ByteArray {
        return writeGeometry(geometryCollection)
    }

    @TypeConverter
    fun toGeometry(bytes: ByteArray): Geometry {
        return readGeometry(bytes)
    }

    @TypeConverter
    fun fromGeometry(geometry: Geometry): ByteArray {
        return writeGeometry(geometry)
    }
}
