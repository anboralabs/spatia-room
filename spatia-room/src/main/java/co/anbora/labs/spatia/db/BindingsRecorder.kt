package co.anbora.labs.spatia.db

import android.util.SparseArray
import androidx.sqlite.db.SupportSQLiteProgram

class BindingsRecorder: SupportSQLiteProgram {

    private val bindings = SparseArray<Any>()

    override fun bindBlob(index: Int, value: ByteArray?) = bindings.put(index, value)

    override fun bindLong(index: Int, value: Long) = bindings.put(index, value)

    override fun bindString(index: Int, value: String?) = bindings.put(index, value)

    override fun bindDouble(index: Int, value: Double) = bindings.put(index, value)

    override fun bindNull(index: Int) = bindings.put(index, null)

    override fun close() = clearBindings()

    override fun clearBindings() = bindings.clear()

    fun getBindings(): Array<String?>? {
        val result = arrayOfNulls<String>(bindings.size())
        for (i in 0 until bindings.size()) {
            val key = bindings.keyAt(i)
            result[i] = bindings[key]?.toString()
        }
        return result
    }

}