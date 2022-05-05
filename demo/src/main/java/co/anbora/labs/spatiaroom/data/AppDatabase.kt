package co.anbora.labs.spatiaroom.data

import android.content.Context
import android.telecom.Call
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import co.anbora.labs.spatia.builder.SpatiaRoom
import co.anbora.labs.spatia.geometry.GeometryConverters
import co.anbora.labs.spatiaroom.data.dao.PostsDao
import co.anbora.labs.spatiaroom.data.model.Post

@Database(
    entities = [Post::class],
    version = 1
)
@TypeConverters(GeometryConverters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * @return [PostsDao] Foodium Posts Data Access Object.
     */
    abstract fun getPostsDao(): PostsDao

    companion object {
        const val DB_NAME = "geo_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = SpatiaRoom.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Initialize Spatialite
                        db.query("SELECT InitSpatialMetaData();").moveToNext()
                        // Room already creates a BLOB column for the geometry, so we need to use
                        // RecoverGeometryColumn to correctly initialize Spatialite's metadata
                        db.query("SELECT RecoverGeometryColumn('geo_posts', 'location', 4326, 'POINT', 'XY');")
                            .moveToNext()
                        // create a spatial index (optional)
                        db.query("SELECT CreateSpatialIndex('geo_posts', 'location');")
                            .moveToNext()
                    }
                }).build()

                INSTANCE = instance
                return instance
            }
        }

    }
}