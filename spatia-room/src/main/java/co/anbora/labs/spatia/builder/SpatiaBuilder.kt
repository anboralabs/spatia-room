package co.anbora.labs.spatia.builder

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import co.anbora.labs.spatia.db.SpatiaHelperFactory
import java.util.concurrent.Executor

class SpatiaBuilder<T : RoomDatabase?> (
    context: Context,
    klass: Class<T>,
    name: String
): SpatiaRoom.Builder<T> {

    private val roomBuilder = Room.databaseBuilder(
        context.applicationContext,
        klass,
        name
    ).createFromAsset("spatia_db_template.sqlite")
        .openHelperFactory(SpatiaHelperFactory())

    override fun openHelperFactory(factory: SupportSQLiteOpenHelper.Factory?): SpatiaRoom.Builder<T> {
        roomBuilder.openHelperFactory(factory)
        return this
    }

    override fun addMigrations(vararg migrations: Migration): SpatiaRoom.Builder<T> {
        roomBuilder.addMigrations(*migrations)
        return this
    }

    override fun allowMainThreadQueries(): SpatiaRoom.Builder<T> {
        roomBuilder.allowMainThreadQueries()
        return this
    }

    override fun setJournalMode(journalMode: RoomDatabase.JournalMode): SpatiaRoom.Builder<T> {
        roomBuilder.setJournalMode(journalMode)
        return this
    }

    override fun setQueryExecutor(executor: Executor): SpatiaRoom.Builder<T> {
        roomBuilder.setQueryExecutor(executor)
        return this
    }

    override fun setTransactionExecutor(executor: Executor): SpatiaRoom.Builder<T> {
        roomBuilder.setTransactionExecutor(executor)
        return this
    }

    override fun enableMultiInstanceInvalidation(): SpatiaRoom.Builder<T> {
        roomBuilder.enableMultiInstanceInvalidation()
        return this
    }

    override fun fallbackToDestructiveMigration(): SpatiaRoom.Builder<T> {
        roomBuilder.fallbackToDestructiveMigration()
        return this
    }

    override fun fallbackToDestructiveMigrationOnDowngrade(): SpatiaRoom.Builder<T> {
        roomBuilder.fallbackToDestructiveMigrationOnDowngrade()
        return this
    }

    override fun fallbackToDestructiveMigrationFrom(vararg startVersions: Int): SpatiaRoom.Builder<T> {
        roomBuilder.fallbackToDestructiveMigrationFrom(*startVersions)
        return this
    }

    override fun addCallback(callback: RoomDatabase.Callback): SpatiaRoom.Builder<T> {
        roomBuilder.addCallback(callback)
        return this
    }

    override fun build(): T = roomBuilder.build()


}