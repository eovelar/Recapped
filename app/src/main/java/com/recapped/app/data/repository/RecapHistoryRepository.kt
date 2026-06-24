package com.recapped.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.recapped.app.data.local.RecapLocalMapper
import com.recapped.app.data.local.dao.RecapDao
import com.recapped.app.domain.model.RecapResult
import com.recapped.app.domain.model.StoredRecap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecapHistoryRepository @Inject constructor(
    private val recapDao: RecapDao,
    private val mapper: RecapLocalMapper,
    private val auth: FirebaseAuth
) {
    fun observeRecaps(): Flow<List<StoredRecap>> {
        val userId = getCurrentUserId()

        return recapDao.observeRecaps(userId).map { entities ->
            entities.mapNotNull { entity ->
                mapper.toDomain(entity)?.let { recap ->
                    StoredRecap(
                        id = entity.id,
                        recap = recap
                    )
                }
            }
        }
    }

    suspend fun saveRecap(recap: RecapResult) {
        val entity = mapper.toEntity(
            recap = recap,
            userId = getCurrentUserId()
        )

        recapDao.insertRecap(entity)
    }

    suspend fun getRecapById(
        recapId: String
    ): StoredRecap? {
        val entity = recapDao.getRecapById(
            recapId = recapId,
            userId = getCurrentUserId()
        ) ?: return null

        val recap = mapper.toDomain(entity) ?: return null

        return StoredRecap(
            id = entity.id,
            recap = recap
        )
    }

    suspend fun deleteRecap(recapId: String) {
        val entity = recapDao.getRecapById(
            recapId = recapId,
            userId = getCurrentUserId()
        ) ?: return

        recapDao.deleteRecap(entity)
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException(
                "No hay un usuario autenticado."
            )
    }
}