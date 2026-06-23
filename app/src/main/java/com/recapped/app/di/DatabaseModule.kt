package com.recapped.app.di

import android.content.Context
import androidx.room.Room
import com.recapped.app.data.local.RecappedDatabase
import com.recapped.app.data.local.dao.ArtistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        ).build()
    }

    @Provides
    fun provideArtistDao(
        database: RecappedDatabase
    ): ArtistDao {
        return database.artistDao()
    }
}