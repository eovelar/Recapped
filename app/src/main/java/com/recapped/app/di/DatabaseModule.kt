package com.recapped.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.recapped.app.data.local.RecappedDatabase
import com.recapped.app.data.local.dao.ArtistDao
import com.recapped.app.data.local.dao.RecapDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recaps` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `username` TEXT NOT NULL,
                `period` TEXT NOT NULL,
                `generatedAt` INTEGER NOT NULL,
                `totalScrobbles` INTEGER NOT NULL,
                `uniqueArtists` INTEGER NOT NULL,
                `uniqueTracks` INTEGER NOT NULL,
                `topArtist` TEXT NOT NULL,
                `headline` TEXT NOT NULL,
                `recapJson` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_recaps_userId_generatedAt`
            ON `recaps` (`userId`, `generatedAt`)
            """.trimIndent()
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RecappedDatabase {
        return Room.databaseBuilder(
            context,
            RecappedDatabase::class.java,
            "recapped_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideArtistDao(
        database: RecappedDatabase
    ): ArtistDao {
        return database.artistDao()
    }

    @Provides
    fun provideRecapDao(
        database: RecappedDatabase
    ): RecapDao {
        return database.recapDao()
    }
}