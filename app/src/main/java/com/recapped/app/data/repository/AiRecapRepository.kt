package com.recapped.app.data.repository

import com.recapped.app.BuildConfig
import com.recapped.app.data.remote.GroqApi
import com.recapped.app.data.remote.dto.GroqChatRequest
import com.recapped.app.data.remote.dto.GroqMessageDto
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.AiArtistRecommendation
import com.recapped.app.domain.model.AiRecapContent
import com.recapped.app.domain.model.RecapArtist
import com.recapped.app.domain.model.RecapGenre
import com.recapped.app.domain.model.RecapPeriod
import com.recapped.app.domain.model.RecapTrack
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRecapRepository @Inject constructor(
    private val groqApi: GroqApi
) {

    suspend fun generateRecapContent(
        period: RecapPeriod,
        totalScrobbles: Int,
        uniqueArtists: Int,
        uniqueTracks: Int,
        topArtists: List<RecapArtist>,
        topTracks: List<RecapTrack>,
        genres: List<RecapGenre>
    ): Resource<AiRecapContent> {
        if (BuildConfig.GROQ_API_KEY.isBlank()) {
            return Resource.Error(
                "No se configuró la API key de Groq."
            )
        }

        return try {
            val response = groqApi.generateRecap(
                authorization = "Bearer ${BuildConfig.GROQ_API_KEY}",
                request = GroqChatRequest(
                    messages = listOf(
                        GroqMessageDto(
                            role = "system",
                            content = SYSTEM_PROMPT
                        ),
                        GroqMessageDto(
                            role = "user",
                            content = buildPrompt(
                                period = period,
                                totalScrobbles = totalScrobbles,
                                uniqueArtists = uniqueArtists,
                                uniqueTracks = uniqueTracks,
                                topArtists = topArtists,
                                topTracks = topTracks,
                                genres = genres
                            )
                        )
                    )
                )
            )

            val content = response.choices
                .firstOrNull()
                ?.message
                ?.content
                ?.trim()

            if (content.isNullOrBlank()) {
                Resource.Error(
                    "La IA no generó un análisis."
                )
            } else {
                Resource.Success(
                    parseResponse(content)
                )
            }
        } catch (error: Exception) {
            Resource.Error(
                message = mapError(error),
                cause = error
            )
        }
    }

    private fun buildPrompt(
        period: RecapPeriod,
        totalScrobbles: Int,
        uniqueArtists: Int,
        uniqueTracks: Int,
        topArtists: List<RecapArtist>,
        topTracks: List<RecapTrack>,
        genres: List<RecapGenre>
    ): String {
        val artistsText = topArtists.joinToString("\n") {
            "${it.rank}. ${it.name}: ${it.playcount} reproducciones. " +
                    "Géneros: ${it.tags.joinToString(", ")}"
        }

        val tracksText = topTracks.joinToString("\n") {
            "${it.rank}. ${it.name} - ${it.artistName}: " +
                    "${it.playcount} reproducciones"
        }

        val genresText = genres.joinToString("\n") {
            "${it.name}: ${it.percentage}%"
        }

        val existingArtists = topArtists.joinToString(", ") {
            it.name
        }

        return """
            Generá el análisis musical personalizado del usuario.

            Período seleccionado:
            ${period.title} (${period.subtitle})

            Estadísticas reales:
            - Reproducciones: $totalScrobbles
            - Artistas únicos: $uniqueArtists
            - Canciones únicas: $uniqueTracks

            Artistas principales:
            $artistsText

            Canciones principales:
            $tracksText

            Géneros:
            $genresText

            Artistas que no deben recomendarse porque ya aparecen
            entre los principales:
            $existingArtists

            Devolvé solamente un objeto JSON válido con esta estructura:

            {
              "headline": "frase breve sobre el perfil musical",
              "summary": "análisis personalizado de 2 o 3 oraciones",
              "recommendations": [
                {
                  "name": "nombre del artista",
                  "genre": "género principal",
                  "reason": "motivo breve de la recomendación"
                }
              ]
            }

            Requisitos:
            - Respondé en español.
            - Generá exactamente 3 recomendaciones.
            - No recomiendes artistas ya presentes en el top.
            - Basate únicamente en el período seleccionado.
            - No inventes estadísticas ni cantidades.
            - No uses Markdown.
            - No agregues texto fuera del JSON.
        """.trimIndent()
    }

    private fun parseResponse(
        rawContent: String
    ): AiRecapContent {
        val jsonText = extractJson(rawContent)
        val json = JSONObject(jsonText)

        val recommendationsJson =
            json.getJSONArray("recommendations")

        val recommendations = buildList {
            for (index in 0 until recommendationsJson.length()) {
                val item = recommendationsJson.getJSONObject(index)

                val name = item
                    .optString("name")
                    .trim()

                if (name.isBlank()) {
                    continue
                }

                add(
                    AiArtistRecommendation(
                        name = name,
                        genre = item
                            .optString("genre")
                            .trim()
                            .ifBlank {
                                "Recomendación musical"
                            },
                        reason = item
                            .optString("reason")
                            .trim()
                            .ifBlank {
                                "Podría encajar con tus gustos."
                            }
                    )
                )
            }
        }.take(3)

        if (recommendations.size < 3) {
            throw IllegalStateException(
                "La IA no generó suficientes recomendaciones."
            )
        }

        return AiRecapContent(
            headline = json
                .optString("headline")
                .trim()
                .ifBlank {
                    "Tu período tuvo una identidad musical propia."
                },
            summary = json
                .optString("summary")
                .trim()
                .ifBlank {
                    "Tus artistas y canciones principales muestran " +
                            "un perfil musical variado."
                },
            recommendations = recommendations
        )
    }

    private fun extractJson(
        content: String
    ): String {
        val cleaned = content
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')

        if (start == -1 || end == -1 || end <= start) {
            throw IllegalStateException(
                "La respuesta de la IA no tiene un formato válido."
            )
        }

        return cleaned.substring(start, end + 1)
    }

    private fun mapError(
        error: Throwable
    ): String {
        return when (error) {
            is IOException -> {
                "No hay conexión a Internet."
            }

            is HttpException -> {
                when (error.code()) {
                    400 -> "Groq rechazó la solicitud."
                    401 -> "La API key de Groq no es válida."
                    403 -> "Groq no autorizó la solicitud."
                    429 -> "Se alcanzó temporalmente el límite de Groq."
                    else -> {
                        "Error de Groq (${error.code()})."
                    }
                }
            }

            else -> {
                error.message
                    ?: "No pudimos generar el análisis con IA."
            }
        }
    }

    private companion object {
        const val SYSTEM_PROMPT =
            "Sos el analista musical de Recapped. " +
                    "Interpretás estadísticas reales de Last.fm y " +
                    "generás análisis breves, personalizados y claros. " +
                    "Nunca modificás ni inventás los datos recibidos."
    }
}