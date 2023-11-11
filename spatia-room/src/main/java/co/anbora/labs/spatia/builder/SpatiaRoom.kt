package co.anbora.labs.spatia.builder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.ExperimentalRoomApi
import androidx.room.MultiInstanceInvalidationService
import androidx.room.ProvidedTypeConverter
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Builder
import androidx.room.RoomDatabase.Callback
import androidx.room.RoomDatabase.JournalMode
import androidx.room.RoomDatabase.QueryCallback
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import java.io.File
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

object SpatiaRoom {

    fun <T : RoomDatabase> databaseBuilder(
        context: Context, klass: Class<T>, name: String
    ): Builder<T> {
        require(name.trim { it <= ' ' }.isNotEmpty()) {
            ("Cannot build a database with null or empty name."
                    + " If you are trying to create an in memory database, use Room"
                    + ".inMemoryDatabaseBuilder")
        }
        return SpatiaBuilder(
            context.applicationContext,
            klass,
            name
        )
    }

    fun <T : RoomDatabase> inMemoryDatabaseBuilder(
        context: Context, klass: Class<T>
    ): Builder<T> {
        return SpatiaBuilder(
            context.applicationContext,
            klass,
            null
        )
    }

    /**
     * Builder for SpatiaDatabase.
     *
     * @param <T> The type of the abstract database class.
     */
    interface Builder<T : RoomDatabase?> {

        /**
         * Configures Room to create and open the database using a pre-packaged database located in
         * the application 'assets/' folder.
         * <p>
         * Room does not open the pre-packaged database, instead it copies it into the internal
         * app database folder and then opens it. The pre-packaged database file must be located in
         * the "assets/" folder of your application. For example, the path for a file located in
         * "assets/databases/products.db" would be "databases/products.db".
         * <p>
         * The pre-packaged database schema will be validated. It might be best to create your
         * pre-packaged database schema utilizing the exported schema files generated when
         * {@link Database#exportSchema()} is enabled.
         * <p>
         * This method is not supported for an in memory database {@link Builder}.
         *
         * @param databaseFilePath The file path within the 'assets/' directory of where the
         *                         database file is located.
         *
         * @return This {@link Builder} instance.
         */
        @Deprecated(message = "This API is experimental. It may be changed in the future without notice.", level = DeprecationLevel.WARNING)
        fun createFromAsset(databaseFilePath: String): Builder<T>

        /**
         * Configures Room to create and open the database using a pre-packaged database located in
         * the application 'assets/' folder.
         *
         * Room does not open the pre-packaged database, instead it copies it into the internal
         * app database folder and then opens it. The pre-packaged database file must be located in
         * the "assets/" folder of your application. For example, the path for a file located in
         * "assets/databases/products.db" would be "databases/products.db".
         *
         * The pre-packaged database schema will be validated. It might be best to create your
         * pre-packaged database schema utilizing the exported schema files generated when
         * [Database.exportSchema] is enabled.
         *
         * This method is not supported for an in memory database [Builder].
         *
         * @param databaseFilePath The file path within the 'assets/' directory of where the
         * database file is located.
         * @param callback The pre-packaged callback.
         *
         * @return This builder instance.
         */
        @SuppressLint("BuilderSetStyle") // To keep naming consistency.
        fun createFromAsset(
            databaseFilePath: String,
            callback: RoomDatabase.PrepackagedDatabaseCallback
        ): Builder<T>

        /**
         * Configures Room to create and open the database using a pre-packaged database file.
         *
         * Room does not open the pre-packaged database, instead it copies it into the internal
         * app database folder and then opens it. The given file must be accessible and the right
         * permissions must be granted for Room to copy the file.
         *
         * The pre-packaged database schema will be validated. It might be best to create your
         * pre-packaged database schema utilizing the exported schema files generated when
         * [Database.exportSchema] is enabled.
         *
         * The [Callback.onOpen] method can be used as an indicator
         * that the pre-packaged database was successfully opened by Room and can be cleaned up.
         *
         * This method is not supported for an in memory database [Builder].
         *
         * @param databaseFile The database file.
         *
         * @return This builder instance.
         */
        fun createFromFile(databaseFile: File): Builder<T>

        /**
         * Configures Room to create and open the database using a pre-packaged database file.
         *
         * Room does not open the pre-packaged database, instead it copies it into the internal
         * app database folder and then opens it. The given file must be accessible and the right
         * permissions must be granted for Room to copy the file.
         *
         * The pre-packaged database schema will be validated. It might be best to create your
         * pre-packaged database schema utilizing the exported schema files generated when
         * [Database.exportSchema] is enabled.
         *
         * The [Callback.onOpen] method can be used as an indicator
         * that the pre-packaged database was successfully opened by Room and can be cleaned up.
         *
         * This method is not supported for an in memory database [Builder].
         *
         * @param databaseFile The database file.
         * @param callback The pre-packaged callback.
         *
         * @return This builder instance.
         */
        @SuppressLint("BuilderSetStyle", "StreamFiles") // To keep naming consistency.
        fun createFromFile(
            databaseFile: File,
            callback: RoomDatabase.PrepackagedDatabaseCallback
        ): Builder<T>

        /**
         * Configures Room to create and open the database using a pre-packaged database via an
         * [InputStream].
         *
         * This is useful for processing compressed database files. Room does not open the
         * pre-packaged database, instead it copies it into the internal app database folder, and
         * then open it. The [InputStream] will be closed once Room is done consuming it.
         *
         * The pre-packaged database schema will be validated. It might be best to create your
         * pre-packaged database schema utilizing the exported schema files generated when
         * [Database.exportSchema] is enabled.
         *
         * The [Callback.onOpen] method can be used as an indicator
         * that the pre-packaged database was successfully opened by Room and can be cleaned up.
         *
         * This method is not supported for an in memory database [Builder].
         *
         * @param inputStreamCallable A callable that returns an InputStream from which to copy
         * the database. The callable will be invoked in a thread from
         * the Executor set via [setQueryExecutor]. The
         * callable is only invoked if Room needs to create and open the
         * database from the pre-package database, usually the first time
         * it is created or during a destructive migration.
         *
         * @return This builder instance.
         */
        @SuppressLint("BuilderSetStyle") // To keep naming consistency.
        fun createFromInputStream(
            inputStreamCallable: Callable<InputStream>
        ): Builder<T>

        /**
         * Configures Room to create and open the database using a pre-packaged database via an
         * [InputStream].
         *
         * This is useful for processing compressed database files. Room does not open the
         * pre-packaged database, instead it copies it into the internal app database folder, and
         * then open it. The [InputStream] will be closed once Room is done consuming it.
         *
         * The pre-packaged database schema will be validated. It might be best to create your
         * pre-packaged database schema utilizing the exported schema files generated when
         * [Database.exportSchema] is enabled.
         *
         * The [Callback.onOpen] method can be used as an indicator
         * that the pre-packaged database was successfully opened by Room and can be cleaned up.
         *
         * This method is not supported for an in memory database [Builder].
         *
         * @param inputStreamCallable A callable that returns an InputStream from which to copy
         * the database. The callable will be invoked in a thread from
         * the Executor set via [setQueryExecutor]. The
         * callable is only invoked if Room needs to create and open the
         * database from the pre-package database, usually the first time
         * it is created or during a destructive migration.
         * @param callback The pre-packaged callback.
         *
         * @return This builder instance.
         */
        @SuppressLint("BuilderSetStyle", "LambdaLast") // To keep naming consistency.
        fun createFromInputStream(
            inputStreamCallable: Callable<InputStream>,
            callback: RoomDatabase.PrepackagedDatabaseCallback
        ): Builder<T>

        /**
         * Sets the database factory. If not set, it defaults to
         * [FrameworkSQLiteOpenHelperFactory].
         *
         * @param factory The factory to use to access the database.
         * @return This [Builder] instance.
         */
        fun openHelperFactory(factory: SupportSQLiteOpenHelper.Factory?): Builder<T>

        /**
         * Adds a migration to the builder.
         *
         *
         * Each Migration has a start and end versions and Room runs these migrations to bring the
         * database to the latest version.
         *
         *
         * If a migration item is missing between current version and the latest version, Room
         * will clear the database and recreate so even if you have no changes between 2 versions,
         * you should still provide a Migration object to the builder.
         *
         *
         * A migration can handle more than 1 version (e.g. if you have a faster path to choose when
         * going version 3 to 5 without going to version 4). If Room opens a database at version
         * 3 and latest version is &gt;= 5, Room will use the migration object that can migrate from
         * 3 to 5 instead of 3 to 4 and 4 to 5.
         *
         * @param migrations The migration object that can modify the database and to the necessary
         * changes.
         * @return This [Builder] instance.
         */
        fun addMigrations(vararg migrations: Migration): Builder<T>

        /**
         * Adds an auto migration spec to the builder.
         *
         * @param autoMigrationSpec The auto migration object that is annotated with
         * [AutoMigrationSpec] and is declared in an [AutoMigration] annotation.
         * @return This builder instance.
         */
        @Suppress("MissingGetterMatchingBuilder")
        fun addAutoMigrationSpec(autoMigrationSpec: AutoMigrationSpec): Builder<T>

        /**
         * Disables the main thread query check for Room.
         *
         *
         * Room ensures that Database is never accessed on the main thread because it may lock the
         * main thread and trigger an ANR. If you need to access the database from the main thread,
         * you should always use async alternatives or manually move the call to a background
         * thread.
         *
         *
         * You may want to turn this check off for testing.
         *
         * @return This [Builder] instance.
         */
        fun allowMainThreadQueries(): Builder<T>

        /**
         * Sets the journal mode for this database.
         *
         *
         *
         * This value is ignored if the builder is initialized with
         * [Room.inMemoryDatabaseBuilder].
         *
         *
         * The journal mode should be consistent across multiple instances of
         * [RoomDatabase] for a single SQLite database file.
         *
         *
         * The default value is [JournalMode.AUTOMATIC].
         *
         * @param journalMode The journal mode.
         * @return This [Builder] instance.
         */
        fun setJournalMode(journalMode: JournalMode): Builder<T>

        /**
         * Sets the [Executor] that will be used to execute all non-blocking asynchronous
         * queries and tasks, including `LiveData` invalidation, `Flowable` scheduling
         * and `ListenableFuture` tasks.
         *
         *
         * When both the query executor and transaction executor are unset, then a default
         * `Executor` will be used. The default `Executor` allocates and shares threads
         * amongst Architecture Components libraries. If the query executor is unset but a
         * transaction executor was set, then the same `Executor` will be used for queries.
         *
         *
         * For best performance the given `Executor` should be bounded (max number of threads
         * is limited).
         *
         *
         * The input `Executor` cannot run tasks on the UI thread.
         *
         * @return This [Builder] instance.
         *
         * @see .setTransactionExecutor
         */
        fun setQueryExecutor(executor: Executor): Builder<T>

        /**
         * Sets the [Executor] that will be used to execute all non-blocking asynchronous
         * transaction queries and tasks, including `LiveData` invalidation, `Flowable`
         * scheduling and `ListenableFuture` tasks.
         *
         *
         * When both the transaction executor and query executor are unset, then a default
         * `Executor` will be used. The default `Executor` allocates and shares threads
         * amongst Architecture Components libraries. If the transaction executor is unset but a
         * query executor was set, then the same `Executor` will be used for transactions.
         *
         *
         * If the given `Executor` is shared then it should be unbounded to avoid the
         * possibility of a deadlock. Room will not use more than one thread at a time from this
         * executor since only one transaction at a time can be executed, other transactions will
         * be queued on a first come, first serve order.
         *
         *
         * The input `Executor` cannot run tasks on the UI thread.
         *
         * @return This [Builder] instance.
         *
         * @see .setQueryExecutor
         */
        fun setTransactionExecutor(executor: Executor): Builder<T>

        /**
         * Sets whether table invalidation in this instance of [RoomDatabase] should be
         * broadcast and synchronized with other instances of the same [RoomDatabase],
         * including those in a separate process. In order to enable multi-instance invalidation,
         * this has to be turned on both ends.
         *
         *
         * This is not enabled by default.
         *
         *
         * This does not work for in-memory databases. This does not work between database instances
         * targeting different database files.
         *
         * @return This [Builder] instance.
         */
        fun enableMultiInstanceInvalidation(): Builder<T>

        /**
         * Allows Room to destructively recreate database tables if [Migration]s that would
         * migrate old database schemas to the latest schema version are not found.
         *
         *
         * When the database version on the device does not match the latest schema version, Room
         * runs necessary [Migration]s on the database.
         *
         *
         * If it cannot find the set of [Migration]s that will bring the database to the
         * current version, it will throw an [IllegalStateException].
         *
         *
         * You can call this method to change this behavior to re-create the database instead of
         * crashing.
         *
         *
         * If the database was create from an asset or a file then Room will try to use the same
         * file to re-create the database, otherwise this will delete all of the data in the
         * database tables managed by Room.
         *
         *
         * To let Room fallback to destructive migration only during a schema downgrade then use
         * [.fallbackToDestructiveMigrationOnDowngrade].
         *
         * @return This [Builder] instance.
         *
         * @see .fallbackToDestructiveMigrationOnDowngrade
         */
        fun fallbackToDestructiveMigration(): Builder<T>

        /**
         * Allows Room to destructively recreate database tables if [Migration]s are not
         * available when downgrading to old schema versions.
         *
         * @return This [Builder] instance.
         *
         * @see Builder.fallbackToDestructiveMigration
         */
        fun fallbackToDestructiveMigrationOnDowngrade(): Builder<T>

        /**
         * Informs Room that it is allowed to destructively recreate database tables from specific
         * starting schema versions.
         *
         *
         * This functionality is the same as that provided by
         * [.fallbackToDestructiveMigration], except that this method allows the
         * specification of a set of schema versions for which destructive recreation is allowed.
         *
         *
         * Using this method is preferable to [.fallbackToDestructiveMigration] if you want
         * to allow destructive migrations from some schema versions while still taking advantage
         * of exceptions being thrown due to unintentionally missing migrations.
         *
         *
         * Note: No versions passed to this method may also exist as either starting or ending
         * versions in the [Migration]s provided to [.addMigrations]. If a
         * version passed to this method is found as a starting or ending version in a Migration, an
         * exception will be thrown.
         *
         * @param startVersions The set of schema versions from which Room should use a destructive
         * migration.
         * @return This [Builder] instance.
         */
        fun fallbackToDestructiveMigrationFrom(vararg startVersions: Int): Builder<T>

        /**
         * Adds a [Callback] to this database.
         *
         * @param callback The callback.
         * @return This [Builder] instance.
         */
        fun addCallback(callback: RoomDatabase.Callback): Builder<T>

        /**
         * Sets a [QueryCallback] to be invoked when queries are executed.
         *
         * The callback is invoked whenever a query is executed, note that adding this callback
         * has a small cost and should be avoided in production builds unless needed.
         *
         * A use case for providing a callback is to allow logging executed queries. When the
         * callback implementation logs then it is recommended to use an immediate executor.
         *
         * @param queryCallback The query callback.
         * @param executor The executor on which the query callback will be invoked.
         * @return This builder instance.
         */
        @Suppress("MissingGetterMatchingBuilder")
        fun setQueryCallback(
            queryCallback: QueryCallback,
            executor: Executor
        ): Builder<T>

        /**
         * Adds a type converter instance to this database.
         *
         * @param typeConverter The converter. It must be an instance of a class annotated with
         * [ProvidedTypeConverter] otherwise Room will throw an exception.
         * @return This builder instance.
         */
        fun addTypeConverter(typeConverter: Any): Builder<T>

        /**
         * Creates the databases and initializes it.
         *
         *
         * By default, all RoomDatabases use in memory storage for TEMP tables and enables recursive
         * triggers.
         *
         * @return A new database instance.
         */
        @SuppressLint("RestrictedApi")
        fun build(): T
    }

}
