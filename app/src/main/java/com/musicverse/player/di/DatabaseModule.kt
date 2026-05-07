package com.musicverse.player.di

import android.content.Context
import com.musicverse.player.data.local.AppDatabase
import com.musicverse.player.data.local.TrackDao
import com.musicverse.player.data.local.VersionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing Room database and DAO instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AppDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun provideVersionDao(database: AppDatabase): VersionDao {
        return database.versionDao()
    }
}

