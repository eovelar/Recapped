package com.recapped.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.recapped.app.data.local.dao.ArtistDao
import com.recapped.app.data.local.entity.ArtistEntity

@Database(
    entities = [ArtistEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RecappedDatabase : RoomDatabase() {

    abstract fun artistDao(): ArtistDao
}