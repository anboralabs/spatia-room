package co.anbora.labs.spatia.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.spatialite.database.SQLiteDatabase
import org.spatialite.database.SQLiteOpenHelper

class Helper(
    private val context: Context,
    private val dbName: String,
    private val callback: SupportSQLiteOpenHelper.Callback
): SupportSQLiteOpenHelper {

    private val delegate: OpenHelper = createDelegate(callback)

    private fun createDelegate(
        callback: SupportSQLiteOpenHelper.Callback
    ): OpenHelper {
        return OpenHelper(callback)
    }

    override val databaseName: String?
        get() = delegate.databaseName

    override val writableDatabase: SupportSQLiteDatabase
        get() = try {
            delegate.getWritableSupportDatabase() as SupportSQLiteDatabase
        } catch (e: Exception) {
            throw e
        }

    override val readableDatabase: SupportSQLiteDatabase
        get() = writableDatabase

    override fun close() = delegate.close()

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) = delegate.setWriteAheadLoggingEnabled(enabled)

    inner class OpenHelper(
        @Volatile private var callback: SupportSQLiteOpenHelper.Callback
    ): SQLiteOpenHelper(context, dbName, null, callback.version) {

        @Volatile
        private var migrated = false
        private var wrappedDb: SupportSQLiteDatabase? = null

        @Synchronized
        fun getWritableSupportDatabase(): SupportSQLiteDatabase? {
            migrated = false
            val db = super.getWritableDatabase()
            if (migrated) {
                close()
                return getWritableSupportDatabase()
            }
            return getWrappedDb(db)
        }

        @Synchronized
        fun getWrappedDb(db: SQLiteDatabase?): SupportSQLiteDatabase {
            if (wrappedDb == null) {
                wrappedDb = Database(db!!)
            }
            return wrappedDb as SupportSQLiteDatabase
        }

        override fun onCreate(db: SQLiteDatabase?) {
            callback.onCreate(getWrappedDb(db))
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            migrated = true
            callback.onUpgrade(getWrappedDb(db), oldVersion, newVersion)
        }

        override fun onConfigure(db: SQLiteDatabase?) {
            callback.onConfigure(getWrappedDb(db))
        }

        override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            migrated = true
            callback.onDowngrade(getWrappedDb(db), oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase?) {
            if (!migrated) {
                // from Google: "if we've migrated, we'll re-open the db so we  should not call the callback."
                callback.onOpen(getWrappedDb(db))
            }
        }

        override fun close() {
            super.close()
            wrappedDb = null
        }
    }

}
