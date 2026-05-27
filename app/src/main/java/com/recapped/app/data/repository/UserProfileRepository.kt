package com.recapped.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("No hay un usuario autenticado.")
    }

    suspend fun createOrUpdateUserProfile() {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("No hay un usuario autenticado.")

        val userData = mapOf(
            "uid" to currentUser.uid,
            "displayName" to currentUser.displayName,
            "email" to currentUser.email,
            "photoUrl" to currentUser.photoUrl?.toString(),
            "updatedAt" to System.currentTimeMillis()
        )

        firestore
            .collection("users")
            .document(currentUser.uid)
            .set(userData, SetOptions.merge())
            .await()
    }

    suspend fun saveLastFmUsername(username: String) {
        val uid = getCurrentUserId()
        val cleanUsername = username.trim()

        if (cleanUsername.isBlank()) {
            throw IllegalArgumentException("El usuario de Last.fm no puede estar vacío.")
        }

        val userData = mapOf(
            "lastFmUsername" to cleanUsername,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore
            .collection("users")
            .document(uid)
            .set(userData, SetOptions.merge())
            .await()
    }

    suspend fun getLastFmUsername(): String? {
        val uid = getCurrentUserId()

        val document = firestore
            .collection("users")
            .document(uid)
            .get()
            .await()

        return document.getString("lastFmUsername")
    }

    suspend fun hasLinkedLastFmAccount(): Boolean {
        return !getLastFmUsername().isNullOrBlank()
    }

    suspend fun removeLastFmUsername() {
        val uid = getCurrentUserId()

        val userData = mapOf(
            "lastFmUsername" to null,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore
            .collection("users")
            .document(uid)
            .set(userData, SetOptions.merge())
            .await()
    }
}