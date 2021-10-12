package co.anbora.labs.spatiaroom.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import co.anbora.labs.spatia.builder.SpatiaRoom
import co.anbora.labs.spatiaroom.data.dao.ContractDao
import co.anbora.labs.spatiaroom.data.dao.PostsDao
import co.anbora.labs.spatiaroom.data.model.Contract
import co.anbora.labs.spatiaroom.data.model.Post

@Database(
    entities = [Post::class, Contract::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * @return [PostsDao] Foodium Posts Data Access Object.
     */
    abstract fun getPostsDao(): PostsDao

    abstract fun contractDao(): ContractDao

}