package com.recapped.app.data.repository

import com.recapped.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Stream con el usuario logueado actual (null = no logueado). */
    val currentUser: Flow<User?>

    /**
     * Intercambia un ID token de Google por una sesión de Firebase Auth.
     * Devuelve el User si fue exitoso; lanza excepción si no.
     */
    suspend fun signInWithGoogleIdToken(idToken: String): User

    suspend fun signOut()
}
