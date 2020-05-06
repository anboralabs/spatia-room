package co.anbora.labs.spatia.builder

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

object SpatiaRoom {

    fun <T : RoomDatabase?> databaseBuilder(
        context: Context, klass: Class<T>, name: String
    ): RoomDatabase.Builder<T> {
        require(name.trim { it <= ' ' }.isNotEmpty()) {
            ("Cannot build a database with null or empty name."
                    + " If you are trying to create an in memory database, use Room"
                    + ".inMemoryDatabaseBuilder")
        }
        return Room.databaseBuilder(
            context.applicationContext,
            klass,
            name
        ).createFromAsset("spatia_db_template.sqlite")
    }

}