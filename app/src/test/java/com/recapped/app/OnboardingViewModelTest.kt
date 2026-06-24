package com.recapped.app

import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.OnboardingRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.User
import com.recapped.app.ui.onboarding.OnboardingViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var onboardingRepository: OnboardingRepository
    private lateinit var artistRepository: ArtistRepository
    private lateinit var currentUser: MutableStateFlow<User?>
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        authRepository = mockk()
        onboardingRepository = mockk()
        artistRepository = mockk()

        currentUser = MutableStateFlow(
            User(
                uid = "usuario-1",
                displayName = "Eva",
                email = "eva@test.com",
                photoUrl = null
            )
        )

        every {
            authRepository.currentUser
        } returns currentUser

        viewModel = OnboardingViewModel(
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            artistRepository = artistRepository
        )
    }

    @Test
    fun `carga el nombre del usuario autenticado`() = runTest {
        advanceUntilIdle()

        assertEquals(
            "Eva",
            viewModel.state.value.displayName
        )
    }

    @Test
    fun `elimina espacios del usuario de LastFm`() {
        viewModel.onLastFmUsernameChange("eva music")

        assertEquals(
            "evamusic",
            viewModel.state.value.lastFmUsername
        )
    }

    @Test
    fun `finish con usuario vacio muestra error`() = runTest {
        advanceUntilIdle()

        var completed = false

        viewModel.finish {
            completed = true
        }

        assertFalse(completed)
        assertEquals(
            "Ingresá tu usuario de Last.fm para vincularlo.",
            viewModel.state.value.error
        )

        coVerify(exactly = 0) {
            onboardingRepository.completeOnboarding(any())
        }
    }

    @Test
    fun `finish valido guarda usuario y completa navegacion`() = runTest {
        advanceUntilIdle()

        coEvery {
            artistRepository.validateLastFmUsername("eva_lastfm")
        } returns Resource.Success(Unit)

        coEvery {
            onboardingRepository.saveLastFmUsername(
                "usuario-1",
                "eva_lastfm"
            )
        } returns Unit

        coEvery {
            onboardingRepository.completeOnboarding("usuario-1")
        } returns Unit

        viewModel.onLastFmUsernameChange("eva_lastfm")

        var completed = false

        viewModel.finish {
            completed = true
        }

        advanceUntilIdle()

        assertTrue(completed)
        assertFalse(viewModel.state.value.isSaving)
        assertEquals(null, viewModel.state.value.error)

        coVerify(exactly = 1) {
            artistRepository.validateLastFmUsername("eva_lastfm")
        }

        coVerify(exactly = 1) {
            onboardingRepository.saveLastFmUsername(
                "usuario-1",
                "eva_lastfm"
            )
        }

        coVerify(exactly = 1) {
            onboardingRepository.completeOnboarding("usuario-1")
        }
    }

    @Test
    fun `usuario inexistente muestra error y no navega`() = runTest {
        advanceUntilIdle()

        coEvery {
            artistRepository.validateLastFmUsername("inexistente")
        } returns Resource.Error(
            "No encontramos ese usuario de Last.fm."
        )

        viewModel.onLastFmUsernameChange("inexistente")

        var completed = false

        viewModel.finish {
            completed = true
        }

        advanceUntilIdle()

        assertFalse(completed)
        assertFalse(viewModel.state.value.isSaving)
        assertEquals(
            "No encontramos ese usuario de Last.fm.",
            viewModel.state.value.error
        )

        coVerify(exactly = 0) {
            onboardingRepository.saveLastFmUsername(any(), any())
        }

        coVerify(exactly = 0) {
            onboardingRepository.completeOnboarding(any())
        }
    }

    @Test
    fun `skip completa onboarding y navega`() = runTest {
        advanceUntilIdle()

        coEvery {
            onboardingRepository.completeOnboarding("usuario-1")
        } returns Unit

        var completed = false

        viewModel.skip {
            completed = true
        }

        advanceUntilIdle()

        assertTrue(completed)
        assertFalse(viewModel.state.value.isSaving)

        coVerify(exactly = 1) {
            onboardingRepository.completeOnboarding("usuario-1")
        }
    }
}