package com.recapped.app

import com.recapped.app.data.local.dao.ArtistDao
import com.recapped.app.data.local.entity.ArtistEntity
import com.recapped.app.data.remote.DeezerApi
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.data.remote.dto.ArtistDto
import com.recapped.app.data.remote.dto.ArtistsWrapper
import com.recapped.app.data.remote.dto.ImageDto
import com.recapped.app.data.remote.dto.UserTopArtistsResponse
import com.recapped.app.data.repository.ArtistRepositoryImpl
import com.recapped.app.domain.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ArtistRepositoryCacheTest {

    private lateinit var lastFmApi: LastFmApi
    private lateinit var deezerApi: DeezerApi
    private lateinit var artistDao: ArtistDao
    private lateinit var repository: ArtistRepositoryImpl

    @Before
    fun setUp() {
        lastFmApi = mockk()
        deezerApi = mockk(relaxed = true)
        artistDao = mockk()

        repository = ArtistRepositoryImpl(
            api = lastFmApi,
            deezerApi = deezerApi,
            artistDao = artistDao
        )
    }

    @Test
    fun `devuelve artistas almacenados en Room`() = runTest {
        val cachedArtists = listOf(
            createEntity(
                name = "Radiohead",
                rank = 1
            )
        )

        every {
            artistDao.observeArtists("eva", "1month")
        } returns flowOf(cachedArtists)

        coEvery {
            lastFmApi.getUserTopArtists(
                user = "eva",
                period = "1month",
                limit = 12,
                apiKey = any()
            )
        } throws IOException("Sin conexión")

        val results = repository
            .getUserTopArtists("eva", "1month")
            .toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)

        val artists = (results[1] as Resource.Success).data

        assertEquals(1, artists.size)
        assertEquals("Radiohead", artists.first().name)
        assertEquals(1, artists.first().rank)
    }

    @Test
    fun `si API falla mantiene cache sin devolver error`() = runTest {
        val cachedArtists = listOf(
            createEntity(
                name = "Muse",
                rank = 1
            )
        )

        every {
            artistDao.observeArtists("eva", "6month")
        } returns flowOf(cachedArtists)

        coEvery {
            lastFmApi.getUserTopArtists(
                user = "eva",
                period = "6month",
                limit = 12,
                apiKey = any()
            )
        } throws IOException("Sin conexión")

        val results = repository
            .getUserTopArtists("eva", "6month")
            .toList()

        assertEquals(2, results.size)
        assertTrue(results.none { it is Resource.Error })

        val cachedResult = results[1] as Resource.Success

        assertEquals(
            "Muse",
            cachedResult.data.first().name
        )

        coVerify(exactly = 0) {
            artistDao.replaceArtists(any(), any(), any())
        }
    }

    @Test
    fun `respuesta de API actualiza Room y devuelve datos nuevos`() =
        runTest {
            val remoteResponse = UserTopArtistsResponse(
                topArtists = ArtistsWrapper(
                    artist = listOf(
                        ArtistDto(
                            name = "Arctic Monkeys",
                            playcount = "250",
                            listeners = "100",
                            mbid = "mbid-1",
                            url = null,
                            image = listOf(
                                ImageDto(
                                    url = "https://imagen.test/arctic.jpg",
                                    size = "extralarge"
                                )
                            )
                        )
                    ),
                    attr = null
                )
            )

            val updatedCache = listOf(
                createEntity(
                    name = "Arctic Monkeys",
                    rank = 1
                )
            )

            every {
                artistDao.observeArtists("eva", "12month")
            } returnsMany listOf(
                flowOf(emptyList()),
                flowOf(updatedCache)
            )

            coEvery {
                lastFmApi.getUserTopArtists(
                    user = "eva",
                    period = "12month",
                    limit = 12,
                    apiKey = any()
                )
            } returns remoteResponse

            coEvery {
                artistDao.replaceArtists(
                    username = "eva",
                    period = "12month",
                    artists = any()
                )
            } returns Unit

            val results = repository
                .getUserTopArtists("eva", "12month")
                .toList()

            assertTrue(results[0] is Resource.Loading)
            assertTrue(results[1] is Resource.Success)

            val artists = (results[1] as Resource.Success).data

            assertEquals(1, artists.size)
            assertEquals("Arctic Monkeys", artists.first().name)

            coVerify(exactly = 1) {
                artistDao.replaceArtists(
                    username = "eva",
                    period = "12month",
                    artists = any()
                )
            }
        }

    private fun createEntity(
        name: String,
        rank: Int
    ): ArtistEntity {
        return ArtistEntity(
            username = "eva",
            period = "1month",
            name = name,
            mbid = "",
            playcount = 100,
            listeners = 50,
            imageUrl = "https://imagen.test/$name.jpg",
            rank = rank
        )
    }
}