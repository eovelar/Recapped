package com.recapped.app

import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.domain.model.User
import com.recapped.app.ui.login.LoginViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        authRepository = mockk()
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `login exitoso desactiva loading y no muestra error`() = runTest {
        val user = User(
            uid = "usuario-1",
            displayName = "Eva",
            email = "eva@test.com",
            photoUrl = null
        )

        coEvery {
            authRepository.signInWithGoogleIdToken("token-valido")
        } returns user

        viewModel.onGoogleIdToken("token-valido")

        assertTrue(viewModel.state.value.loading)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.loading)
        assertNull(viewModel.state.value.error)

        coVerify(exactly = 1) {
            authRepository.signInWithGoogleIdToken("token-valido")
        }
    }

    @Test
    fun `login fallido desactiva loading y muestra error`() = runTest {
        coEvery {
            authRepository.signInWithGoogleIdToken("token-invalido")
        } throws IllegalStateException("Credenciales inválidas")

        viewModel.onGoogleIdToken("token-invalido")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.loading)
        assertEquals(
            "Credenciales inválidas",
            viewModel.state.value.error
        )
    }

    @Test
    fun `error externo actualiza el estado`() {
        viewModel.onSignInError("No se pudo iniciar sesión")

        assertFalse(viewModel.state.value.loading)
        assertEquals(
            "No se pudo iniciar sesión",
            viewModel.state.value.error
        )
    }

    @Test
    fun `clearError elimina el mensaje de error`() {
        viewModel.onSignInError("Error de autenticación")
        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }
}