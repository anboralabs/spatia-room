package co.anbora.labs.spatia.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper

class SpatiaHelperFactory: SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        require(configuration.name != null) {
            "Name database null"
        }
        return create(configuration.context, configuration.name as String, configuration.callback)
    }

    private fun create(
        context: Context, name: String,
        callback: SupportSQLiteOpenHelper.Callback
    ): SupportSQLiteOpenHelper {
        return Helper(context, name, callback)
    }
}
