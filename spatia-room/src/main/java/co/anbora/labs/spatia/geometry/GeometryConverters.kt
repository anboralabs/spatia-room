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

    @TypeConverter
    fun toPoint(bytes: ByteArray): Point {
        val (srid, type, buffer) = readGeometryHeader(bytes)
        if (type != 1) throw IllegalArgumentException("Geometry is not a Point")
        return readPoint(buffer, srid)
    }

    @TypeConverter
    fun fromPoint(point: Point): ByteArray {
        val buffer = ByteBuffer.allocate(60)
        writeGeometryHeader(point, 1, buffer)
        writePoint(buffer, point)  // y
        writeGeometryEnd(buffer)
        return buffer.array()
    }

    @TypeConverter
    fun toLineString(bytes: ByteArray): LineString {
        val (srid, type, buffer) = readGeometryHeader(bytes)
        if (type != 2) throw IllegalArgumentException("Geometry is not a LineString")
        return readLineString(buffer, srid)
    }

    @TypeConverter
    fun fromLineString(lineString: LineString): ByteArray {
        val buffer = ByteBuffer.allocate(48 + lineString.points.size * 16)
        writeGeometryHeader(lineString, 2, buffer)
        writeLineString(buffer, lineString)
        writeGeometryEnd(buffer)
        return buffer.array()
    }

    @TypeConverter
    fun toPolygon(bytes: ByteArray): Polygon {
        val (srid, type, buffer) = readGeometryHeader(bytes)
        if (type != 3) throw IllegalArgumentException("Geometry is not a Polygon")
        return readPolygon(buffer, srid)
    }

    @TypeConverter
    fun fromPolygon(polygon: Polygon): ByteArray {
        val buffer = ByteBuffer.allocate(48 + polygon.rings.size * 4 + polygon.rings.sumOf { it.points.size } * 16)
        writeGeometryHeader(polygon, 3, buffer)
        writePolygon(buffer, polygon)
        writeGeometryEnd(buffer)
        return buffer.array()
    }
}