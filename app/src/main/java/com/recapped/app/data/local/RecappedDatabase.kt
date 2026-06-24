package com.recapped.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.recapped.app.data.local.dao.ArtistDao
import com.recapped.app.data.local.dao.RecapDao
import com.recapped.app.data.local.entity.ArtistEntity
import com.recapped.app.data.local.entity.RecapEntity

@Database(
    entities = [
        ArtistEntity::class,
        RecapEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RecappedDatabase : RoomDatabase() {

    abstract fun artistDao(): ArtistDao

    abstract fun recapDao(): RecapDao
}