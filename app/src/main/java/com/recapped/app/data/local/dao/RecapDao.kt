package com.recapped.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.recapped.app.data.local.entity.RecapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecapDao {

    @Query(
        """
        SELECT * FROM recaps
        WHERE userId = :userId
        ORDER BY generatedAt DESC
        """
    )
    fun observeRecaps(
        userId: String
    ): Flow<List<RecapEntity>>

    @Query(
        """
        SELECT * FROM recaps
        WHERE id = :recapId AND userId = :userId
        LIMIT 1
        """
    )
    suspend fun getRecapById(
        recapId: String,
        userId: String
    ): RecapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecap(
        recap: RecapEntity
    )

    @Delete
    suspend fun deleteRecap(
        recap: RecapEntity
    )

    @Query(
        """
        DELETE FROM recaps
        WHERE userId = :userId
        """
    )
    suspend fun deleteUserRecaps(
        userId: String
    )
}