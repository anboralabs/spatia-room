package co.anbora.labs.spatiaroom.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import co.anbora.labs.spatia.builder.SpatiaRoom
import co.anbora.labs.spatiaroom.data.dao.PostsDao
import co.anbora.labs.spatiaroom.data.model.Post

@Database(
    entities = [Post::class],
    version = 1
)
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
                ).build()

                INSTANCE = instance
                return instance
            }
        }

    }
}