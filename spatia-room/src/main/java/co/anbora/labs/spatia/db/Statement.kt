package co.anbora.labs.spatia.db

import androidx.sqlite.db.SupportSQLiteStatement
import org.spatialite.database.SQLiteStatement

class Statement(
    private val delegate: SQLiteStatement
): Program(delegate), SupportSQLiteStatement {

    override fun simpleQueryForLong(): Long = delegate.simpleQueryForLong()

    override fun simpleQueryForString(): String = delegate.simpleQueryForString()

    override fun execute() = delegate.execute()

    override fun executeInsert(): Long = delegate.executeInsert()

    override fun executeUpdateDelete(): Int = delegate.executeUpdateDelete()
}