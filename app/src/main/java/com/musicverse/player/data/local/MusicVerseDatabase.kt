package com.musicverse.player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.musicverse.player.data.local.dao.TrackDao
import com.musicverse.player.data.local.entities.TrackEntity

@Database(entities = [TrackEntity::class], version = 1, exportSchema = false)
abstract class MusicVerseDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
