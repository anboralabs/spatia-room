package co.anbora.labs.spatia.geometry

/**
 * Represents a geometry as stored in a Spatialite database.
 */
sealed class Geometry {
    /**
     * The Spatial Reference Identifier (SRID) for this Geometry.
     *
     * Should default to 4326, which is the WGS 84 coordinate system.
     */
    abstract val srid: Int

    /**
     * The Minimum Bounding Rectangle (MBR), also called bounding box, for this Geometry.
     */
    abstract val mbr: Mbr
}

/**
 * Represents a Minimum Bounding Rectangle, also called bounding box, for a Geometry.
 */
data class Mbr(val minX: Double, val minY: Double, val maxX: Double, val maxY: Double, val srid: Int) {
    /**
     * Converts this MBR into a Polygon
     */
    fun asPolygon() = Polygon(LineString(listOf(
        Point(minX, minY, srid),
        Point(maxX, minY, srid),
        Point(maxX, maxY, srid),
        Point(minX, maxY, srid),
        Point(minX, minY, srid)
    )))
}

/**
 * Represents a point with an X and Y coordinate.
 *
 * In a spherical coordinate system, X can also be called longitude and Y the latitude.
 */
data class Point(val x: Double, val y: Double, override val srid: Int = 4326): Geometry() {
    override val mbr: Mbr by lazy {
        Mbr(x, y, x, y, srid)
    }
}

/**
 * Represents a line string that consists of multiple points
 */
data class LineString(val points: List<Point>): Geometry() {
    init {
        if (points.isEmpty()) {
            throw IllegalArgumentException("A LineString needs at least one point.")
        }
    }

    override val srid = points.map { it.srid }.distinct().single()

    override val mbr: Mbr by lazy {
        Mbr(points.minOf { it.x }, points.minOf { it.y }, points.maxOf { it.x }, points.maxOf { it.y }, srid)
    }
}

/**
 * Represents a polygon.
 *
 * A simple polygon consists of only one ring
 */
data class Polygon(val rings: List<LineString>): Geometry() {
    init {
        if (rings.isEmpty()) {
            throw IllegalArgumentException("A Polygon needs at least one ring, the exterior ring.")
        }
        for (ring in rings) {
            if (ring.points.first() != ring.points.last()) {
                throw IllegalArgumentException("Rings must be closed.")
            }
        }
        // NOTE: the validations above are not the only criteria for a valid polygon.
        // remaining validation will be performed by Spatialite when inserting to the DB.
    }

    constructor(exteriorRing: LineString): this(listOf(exteriorRing))

    /**
     * Exterior ring of the polygon
     */
    val exteriorRing: LineString
        get() = rings.first()

    override val srid = rings.map { it.srid }.distinct().single()

    override val mbr: Mbr by lazy {
        exteriorRing.mbr
    }
}

sealed class GeoCollection<T: Geometry>(val items: List<T>): Geometry() {
    override val srid = items.map { it.srid }.distinct().single()
    override val mbr: Mbr by lazy {
        Mbr(items.minOf { it.mbr.minX }, items.minOf { it.mbr.minY }, items.maxOf { it.mbr.maxX }, items.maxOf { it.mbr.maxY }, srid)
    }
}

data class MultiPoint(val points: List<Point>): GeoCollection<Point>(points)
data class MultiLineString(val lineStrings: List<LineString>): GeoCollection<LineString>(lineStrings)
data class MultiPolygon(val polygons: List<Polygon>): GeoCollection<Polygon>(polygons)
data class GeometryCollection(val geometries: List<Geometry>): GeoCollection<Geometry>(geometries) {
    init {
        if (geometries.any { it is GeoCollection<*> }) {
            throw IllegalArgumentException("nested geometry collections are not allowed")
        }
    }
}
