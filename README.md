![Spatia Logo](/resources/logo.png)

# Spatia-Room

## What is it?
Stapia-Room is a bridge between Room Android Database and Spatialite.

## How to use it?

- Add Room dependencies
- Add Spatia-room dependency
- Creates database using SpatiaRoom Builder
- Creates Dao with Geo Queries

### Creating the database

```java
val instance = SpatiaRoom.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            DB_NAME
                        ).build()
```

### Creating Dao

- Mark each geo-spatial queries with `@SkipQueryVerification`

```java
import androidx.room.*
import co.anbora.labs.spatiaroom.data.model.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(posts: List<Post>)

    @Query("SELECT spatialite_version()")
    @SkipQueryVerification
    fun getSpatiaVersion(): String

    @Query("SELECT proj4_version()")
    @SkipQueryVerification
    fun getProj4Version(): String

    @Query("SELECT geos_version()")
    @SkipQueryVerification
    fun getGeosVersion(): String

    @Query("""
        SELECT ASText( 
            MakePolygon(
                GeomFromText('LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0)') 
            ) 
        ) as line
    """)
    @SkipQueryVerification
    fun getMakePolyline(): String

    @Query("""
        SELECT ST_Distance(
            Transform(MakePoint(-72.1235, 42.3521, 4326), 26986),
            Transform(MakePoint(-71.1235, 42.1521, 4326), 26986)
        ) as distance
    """)
    @SkipQueryVerification
    fun getDistance(): Double

    @Query("DELETE FROM ${Post.TABLE_NAME}")
    fun deleteAllPosts()

    @Query("SELECT * FROM ${Post.TABLE_NAME} WHERE ID = :postId")
    fun getPostById(postId: Int): Flow<Post>

    @Query("SELECT * FROM ${Post.TABLE_NAME}")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM ${Post.TABLE_NAME}")
    fun getAllPostsList(): List<Post>
}
```

## Example Code
There is a very simple and useless example in the `demo` module.

## Other FAQ

### What is *Spatialite*?
Simply: *Spatialite* = *SQLite* + advanced geospatial support.<br>
*Spatialite* is a geospatial extension to *SQLite*. It is a set of few libraries written in C to extend *SQLite* with geometry data types and many [SQL functions](http://www.gaia-gis.it/gaia-sins/spatialite-sql-4.3.0.html) above geometry data. For more info: https://www.gaia-gis.it/gaia-sins/

### Is there a list of all supported Spatialite functions?
Yes - http://www.gaia-gis.it/gaia-sins/spatialite-sql-4.4.0.html

### Does it use JDBC?
No. It uses cursors - the suggested lightweight approach to access SQL used in the Android platform instead of the heavier JDBC.

### 64-bit architectures supported?

Yes. It builds for `arm64-v8a` and `x86_64`. `mips64` is not tested.

## Credits
The main ideas used here were borrowed from:
- https://github.com/sevar83/android-spatialite
- https://github.com/commonsguy/cwac-saferoom

## License
```
MIT License

Copyright (c) 2020 Anbora Labs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```