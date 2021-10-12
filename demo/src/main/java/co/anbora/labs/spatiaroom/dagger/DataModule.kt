package co.anbora.labs.spatiaroom.dagger

import android.content.Context
import co.anbora.labs.spatia.builder.SpatiaRoom
import co.anbora.labs.spatiaroom.data.AppDatabase
import co.anbora.labs.spatiaroom.data.dao.ContractDao
import co.anbora.labs.spatiaroom.data.dao.PostsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object DataModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return SpatiaRoom.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "database"
        ).build()
    }

    @Provides
    fun provideContractDao(appDatabase: AppDatabase): ContractDao {
        return appDatabase.contractDao()
    }

    @Provides
    fun providePostDao(appDatabase: AppDatabase): PostsDao {
        return appDatabase.getPostsDao()
    }

    @Singleton
    @Provides
    fun provideJob(): Job {
        return Job()
    }

    @Singleton
    @Provides
    fun provideUiScope(job: Job): CoroutineScope {
        return CoroutineScope(Dispatchers.Main + job)
    }

}