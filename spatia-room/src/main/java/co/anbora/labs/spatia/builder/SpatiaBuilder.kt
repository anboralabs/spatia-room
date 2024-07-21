package co.anbora.labs.spatia.builder

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import co.anbora.labs.spatia.db.SpatiaHelperFactory
import java.io.File
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class SpatiaBuilder<T : RoomDatabase> (
    context: Context,
    klass: Class<T>,
    name: String?
): SpatiaRoom.Builder<T> {

    private val roomBuilder = if (name != null) {
        Room.databaseBuilder(
            context.applicationContext,
            klass,
            name
        )
    } else {
        Room.inMemoryDatabaseBuilder(context.applicationContext, klass)
    }.addCallback(object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.query("SELECT InitSpatialMetaData();").moveToNext()
        }
    }).openHelperFactory(SpatiaHelperFactory())

    @Deprecated(
        "This API is experimental. It may be changed in the future without notice.",
        level = DeprecationLevel.WARNING
    )
    override fun createFromAsset(databaseFilePath: String): SpatiaRoom.Builder<T> {
        roomBuilder.createFromAsset(databaseFilePath)
        return this
    }

    override fun createFromAsset(
        databaseFilePath: String,
        callback: RoomDatabase.PrepackagedDatabaseCallback
    ): SpatiaRoom.Builder<T> {
        roomBuilder.createFromAsset(databaseFilePath, callback)
        return this
    }

    override fun createFromFile(databaseFile: File): SpatiaRoom.Builder<T> {
        roomBuilder.createFromFile(databaseFile)
        return this
    }

    override fun createFromFile(
        databaseFile: File,
        callback: RoomDatabase.PrepackagedDatabaseCallback
    ): SpatiaRoom.Builder<T> {
        roomBuilder.createFromFile(databaseFile, callback)
        return this
    }

    override fun createFromInputStream(inputStreamCallable: Callable<InputStream>): SpatiaRoom.Builder<T> {
        roomBuilder.createFromInputStream(inputStreamCallable)
        return this
    }

    override fun createFromInputStream(
        inputStreamCallable: Callable<InputStream>,
        callback: RoomDatabase.PrepackagedDatabaseCallback
    ): SpatiaRoom.Builder<T> {
        roomBuilder.createFromInputStream(inputStreamCallable, callback)
        return this
    }

    override fun openHelperFactory(factory: SupportSQLiteOpenHelper.Factory?): SpatiaRoom.Builder<T> {
        roomBuilder.openHelperFactory(factory)
        return this
    }

    override fun addMigrations(vararg migrations: Migration): SpatiaRoom.Builder<T> {
        roomBuilder.addMigrations(*migrations)
        return this
    }

    override fun addAutoMigrationSpec(autoMigrationSpec: AutoMigrationSpec): SpatiaRoom.Builder<T> {
        roomBuilder.addAutoMigrationSpec(autoMigrationSpec)
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

    override fun setQueryCallback(
        queryCallback: RoomDatabase.QueryCallback,
        executor: Executor
    ): SpatiaRoom.Builder<T> {
        roomBuilder.setQueryCallback(queryCallback, executor)
        return this
    }

    override fun addTypeConverter(typeConverter: Any): SpatiaRoom.Builder<T> {
        roomBuilder.addTypeConverter(typeConverter)
        return this
    }

    override fun build(): T = roomBuilder.build()


}
