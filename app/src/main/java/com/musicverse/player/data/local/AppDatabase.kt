package com.musicverse.player.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database — Local cache for imported tracks and discovered versions.
 *
 * Version 2: Added versions table for AI-discovered alternate versions.
 */
@Database(
    entities = [TrackEntity::class, VersionEntity::class, OfflineTrackEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun versionDao(): VersionDao
    abstract fun offlineTrackDao(): OfflineTrackDao

    companion object {
        private const val DB_NAME = "musicverse_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
