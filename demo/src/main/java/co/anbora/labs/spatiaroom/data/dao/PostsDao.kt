/*
 * MIT License
 *
 * Copyright (c) 2020 Shreyas Patil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package co.anbora.labs.spatiaroom.data.dao

import androidx.room.*
import co.anbora.labs.spatiaroom.data.model.PolygonRecord
import co.anbora.labs.spatiaroom.data.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [dev.shreyaspatil.foodium.data.local.FoodiumPostsDatabase]
 */
@Dao
interface PostsDao {

    /**
     * Inserts [posts] into the [Post.TABLE_NAME] table.
     * Duplicate values are replaced in the table.
     * @param posts Posts
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(posts: List<Post>)

    /**
     * Get Spatia lite version
     */
    @Query("SELECT spatialite_version()")
    @SkipQueryVerification
    fun getSpatiaVersion(): String

    /**
     * Get Proj4 version
     */
    @Query("SELECT proj4_version()")
    @SkipQueryVerification
    fun getProj4Version(): String

    /**
     * Get Geos version
     */
    @Query("SELECT geos_version()")
    @SkipQueryVerification
    fun getGeosVersion(): String

    /**
     * Create a polyline from String and return String
     */
    @Query("""
        SELECT ASText( 
            MakePolygon(
                GeomFromText('LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0)') 
            ) 
        ) as line
    """)
    @SkipQueryVerification
    fun getMakePolyline(): String

    /**
     * Get distance between two points in meters
     */
    @Query("""
        SELECT ST_Distance(
            Transform(MakePoint(-72.1235, 42.3521, 4326), 26986),
            Transform(MakePoint(-71.1235, 42.1521, 4326), 26986)
        ) as distance
    """)
    @SkipQueryVerification
    fun getDistance(): Double

    /**
     * Deletes all the posts from the [Post.TABLE_NAME] table.
     */
    @Query("DELETE FROM ${Post.TABLE_NAME}")
    fun deleteAllPosts()

    /**
     * Fetches the post from the [Post.TABLE_NAME] table whose id is [postId].
     * @param postId Unique ID of [Post]
     * @return [Flow] of [Post] from database table.
     */
    @Query("SELECT * FROM ${Post.TABLE_NAME} WHERE ID = :postId")
    fun getPostById(postId: Int): Flow<Post>

    /**
     * Fetches all the posts from the [Post.TABLE_NAME] table.
     * @return [Flow]
     */
    @Query("SELECT * FROM ${Post.TABLE_NAME}")
    fun getAllPosts(): Flow<List<Post>>

    /**
     * Get all post
     */
    @Query("SELECT * FROM ${Post.TABLE_NAME}")
    fun getAllPostsList(): List<Post>

    /**
     * Retrieves a single report from the database
     */
    @Query("""SELECT h.id, h.nazev, h.kod,
            AsText(h.geom) AS geom
        FROM honitby h""")
    @SkipQueryVerification
    fun getPolygon(): List<PolygonRecord>
}