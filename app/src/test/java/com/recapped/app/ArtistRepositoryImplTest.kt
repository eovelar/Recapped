package com.recapped.app

import com.recapped.app.data.local.dao.ArtistDao
import com.recapped.app.data.remote.DeezerApi
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.data.remote.dto.DeezerArtistDto
import com.recapped.app.data.remote.dto.DeezerArtistSearchResponse
import com.recapped.app.data.repository.ArtistRepositoryImpl
import com.recapped.app.domain.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ArtistRepositoryImplTest {

    private lateinit var lastFmApi: LastFmApi
    private lateinit var deezerApi: DeezerApi
    private lateinit var artistDao: ArtistDao
    private lateinit var repository: ArtistRepositoryImpl

    @Before
    fun setUp() {
        lastFmApi = mockk(relaxed = true)
        deezerApi = mockk()
        artistDao = mockk(relaxed = true)

        repository = ArtistRepositoryImpl(
            api = lastFmApi,
            deezerApi = deezerApi,
            artistDao = artistDao
        )
    }

    @Test
    fun `buscar artistas devuelve resultados de Deezer`() = runTest {
        val response = DeezerArtistSearchResponse(
            data = listOf(
                createArtist(
                    id = 1,
                    name = "Radiohead",
                    imageUrl = "radiohead.jpg"
                ),
                createArtist(
                    id = 2,
                    name = "Muse",
                    imageUrl = "muse.jpg"
                )
            )
        )

        coEvery {
            deezerApi.searchArtist("rock")
        } returns response

        val result = repository.searchArtists("rock")

        assertTrue(result is Resource.Success)

        val artists = (result as Resource.Success).data

        assertEquals(2, artists.size)
        assertEquals("Radiohead", artists[0].name)
        assertEquals(1, artists[0].rank)
        assertEquals("radiohead.jpg", artists[0].imageUrl)
        assertEquals("Muse", artists[1].name)
        assertEquals(2, artists[1].rank)

        coVerify(exactly = 1) {
            deezerApi.searchArtist("rock")
        }
    }

    @Test
    fun `buscar artistas elimina resultados duplicados`() = runTest {
        val response = DeezerArtistSearchResponse(
            data = listOf(
                createArtist(1, "Coldplay", "imagen-1.jpg"),
                createArtist(2, "COLDPLAY", "imagen-2.jpg")
            )
        )

        coEvery {
            deezerApi.searchArtist("coldplay")
        } returns response

        val result = repository.searchArtists("coldplay")

        assertTrue(result is Resource.Success)

        val artists = (result as Resource.Success).data

        assertEquals(1, artists.size)
        assertEquals("Coldplay", artists.first().name)
    }

    @Test
    fun `error de conexion devuelve Resource Error`() = runTest {
        coEvery {
            deezerApi.searchArtist("queen")
        } throws IOException("Sin conexión")

        val result = repository.searchArtists("queen")

        assertTrue(result is Resource.Error)
        assertEquals(
            "No hay conexión a Internet",
            (result as Resource.Error).message
        )
    }

    @Test
    fun `consulta demasiado corta devuelve lista vacia sin llamar API`() =
        runTest {
            val result = repository.searchArtists("a")

            assertTrue(result is Resource.Success)
            assertTrue((result as Resource.Success).data.isEmpty())

            coVerify(exactly = 0) {
                deezerApi.searchArtist(any())
            }
        }

    private fun createArtist(
        id: Long,
        name: String,
        imageUrl: String
    ): DeezerArtistDto {
        return DeezerArtistDto(
            id = id,
            name = name,
            picture = null,
            pictureSmall = null,
            pictureMedium = null,
            pictureBig = null,
            pictureXl = imageUrl
        )
    }
}