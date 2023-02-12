package co.anbora.labs.spatia.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.text.TextUtils
import android.util.Pair
import androidx.sqlite.db.*
import org.spatialite.database.SQLiteCursor
import org.spatialite.database.SQLiteDatabase
import java.util.*

private val CONFLICT_VALUES = arrayOf(
    "",
    " OR ROLLBACK ",
    " OR ABORT ",
    " OR FAIL ",
    " OR IGNORE ",
    " OR REPLACE "
)
class Database(
    private val database: SQLiteDatabase
): SupportSQLiteDatabase {

    override fun compileStatement(sql: String): SupportSQLiteStatement = Statement(database.compileStatement(sql))

    override fun beginTransaction() = database.beginTransaction()

    override fun beginTransactionNonExclusive() = database.beginTransactionNonExclusive()

    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener)
            = database.beginTransactionWithListener(transactionListener)

    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener)
            = database.beginTransactionWithListenerNonExclusive(transactionListener)

    override fun endTransaction() = database.endTransaction()

    override fun setTransactionSuccessful() = database.setTransactionSuccessful()

    override fun inTransaction(): Boolean {
        return if (database.isOpen) {
            database.inTransaction()
        } else {
            throw IllegalStateException("You should not be doing this on a closed database")
        }
    }

    override val isDbLockedByCurrentThread: Boolean
        get() = if (database.isOpen) {
            database.isDbLockedByCurrentThread
        } else {
            throw IllegalStateException("You should not be doing this on a closed database")
        }


    override fun yieldIfContendedSafely(): Boolean {
        return if (database.isOpen) {
            database.yieldIfContendedSafely()
        } else {
            throw IllegalStateException("You should not be doing this on a closed database")
        }
    }

    override fun yieldIfContendedSafely(sleepAfterYieldDelayMillis: Long): Boolean {
        return if (database.isOpen) {
            database.yieldIfContendedSafely(sleepAfterYieldDelayMillis)
        } else {
            throw IllegalStateException("You should not be doing this on a closed database")
        }
    }

    override var version: Int
        get() = database.version
        set(value) {
            database.version = value
        }

    override var maximumSize: Long
        get() = database.maximumSize
        set(numBytes) {
            database.maximumSize = numBytes
        }

    override fun setMaximumSize(numBytes: Long): Long = database.setMaximumSize(numBytes)

    override var pageSize: Long
        get() = database.pageSize
        set(numBytes) {
            database.pageSize = numBytes
        }

    override fun query(query: String): Cursor = query(SimpleSQLiteQuery(query))

    override fun query(query: String, bindArgs: Array<out Any?>): Cursor = query(SimpleSQLiteQuery(query, bindArgs))

    override fun query(query: SupportSQLiteQuery): Cursor = query(query, null)

    override fun query(
        query: SupportSQLiteQuery,
        cancellationSignal: CancellationSignal?
    ): Cursor {

        val binding = SQLiteBinding()
        query.bindTo(binding)

        return database.rawQueryWithFactory({ _, masterQuery, editTable, table ->
            query.bindTo(Program(table))
            SQLiteCursor(masterQuery, editTable, table)
        }, query.sql, binding.getBindings(), null)
    }

    override fun insert(
        table: String,
        conflictAlgorithm: Int,
        values: ContentValues
    ): Long = database.insertWithOnConflict(table, null, values, conflictAlgorithm)

    override fun delete(
        table: String,
        whereClause: String?,
        whereArgs: Array<out Any?>?
    ): Int {
        val query = ("DELETE FROM " + table
                + if (TextUtils.isEmpty(whereClause)) "" else " WHERE $whereClause")
        val statement = compileStatement(query)

        return try {
            SimpleSQLiteQuery.bind(statement, whereArgs)
            statement.executeUpdateDelete()
        } finally {
            try {
                statement.close()
            } catch (e: Exception) {
                throw RuntimeException("Exception attempting to close statement", e)
            }
        }
    }

    override fun update(
        table: String,
        conflictAlgorithm: Int,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out Any?>?
    ): Int {

        // taken from SQLiteDatabase class.
        require(values.size() != 0) { "Empty values" }
        val sql = StringBuilder(120)
        sql.append("UPDATE ")
        sql.append(CONFLICT_VALUES[conflictAlgorithm])
        sql.append(table)
        sql.append(" SET ")

        // move all bind args to one array
        val setValuesSize = values.size()
        val bindArgsSize =
            if (whereArgs == null) setValuesSize else setValuesSize + whereArgs.size
        val bindArgs = arrayOfNulls<Any>(bindArgsSize)
        var i = 0
        for (colName in values.keySet()) {
            sql.append(if (i > 0) "," else "")
            sql.append(colName)
            bindArgs[i++] = values[colName]
            sql.append("=?")
        }
        if (whereArgs != null) {
            i = setValuesSize
            while (i < bindArgsSize) {
                bindArgs[i] = whereArgs[i - setValuesSize]
                i++
            }
        }
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ")
            sql.append(whereClause)
        }
        val statement = compileStatement(sql.toString())

        return try {
            SimpleSQLiteQuery.bind(statement, bindArgs)
            statement.executeUpdateDelete()
        } finally {
            try {
                statement.close()
            } catch (e: java.lang.Exception) {
                throw java.lang.RuntimeException("Exception attempting to close statement", e)
            }
        }
    }

    override fun execSQL(sql: String) = database.execSQL(sql)

    override fun execSQL(sql: String, bindArgs: Array<out Any?>) = database.execSQL(sql, bindArgs)

    override val isReadOnly: Boolean
        get() = database.isReadOnly

    override val isOpen: Boolean
        get() = database.isOpen

    override fun needUpgrade(newVersion: Int): Boolean = database.needUpgrade(newVersion)

    override val path: String?
        get() = database.path

    override fun setLocale(locale: Locale) = database.setLocale(locale)

    override fun setMaxSqlCacheSize(cacheSize: Int) = database.setMaxSqlCacheSize(cacheSize)

    override fun setForeignKeyConstraintsEnabled(enabled: Boolean) = database.setForeignKeyConstraintsEnabled(enabled)

    override fun enableWriteAheadLogging(): Boolean = database.enableWriteAheadLogging()

    override fun disableWriteAheadLogging() = database.disableWriteAheadLogging()

    override val isWriteAheadLoggingEnabled: Boolean
        get() = database.isWriteAheadLoggingEnabled

    override val attachedDbs: List<Pair<String, String>>? = database.attachedDbs

    override val isDatabaseIntegrityOk: Boolean
        get() = database.isDatabaseIntegrityOk

    override fun close() = database.close()
}