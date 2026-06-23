package com.recapped.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.recapped.app.data.local.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query(
        """
        SELECT * FROM artists
        WHERE username = :username AND period = :period
        ORDER BY rank ASC
        """
    )
    fun observeArtists(
        username: String,
        period: String
    ): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Query(
        """
        DELETE FROM artists
        WHERE username = :username AND period = :period
        """
    )
    suspend fun clearArtists(
        username: String,
        period: String
    )

    @Transaction
    suspend fun replaceArtists(
        username: String,
        period: String,
        artists: List<ArtistEntity>
    ) {
        clearArtists(username, period)
        insertArtists(artists)
    }
}