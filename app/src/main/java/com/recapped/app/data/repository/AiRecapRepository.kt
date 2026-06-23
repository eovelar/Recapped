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
            Generá un análisis musical personalizado para mostrarle directamente a la persona que usa la app.

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

            Artistas que no deben recomendarse porque ya aparecen entre los principales:
            $existingArtists

            Devolvé solamente un objeto JSON válido con esta estructura:

            {
              "headline": "retrato creativo de la personalidad musical",
              "summary": "análisis personalizado de 2 o 3 oraciones",
              "recommendations": [
                {
                  "name": "nombre del artista",
                  "genre": "género principal",
                  "reason": "motivo breve de la recomendación"
                }
              ]
            }

            Requisitos generales:
            - Respondé en español rioplatense neutro.
            - Hablale directamente a la persona en segunda persona.
            - Todos los títulos y textos deben comenzar con mayúscula.
            - Usá expresiones naturales como "te gusta", "escuchaste", "tu sonido", "tu período" y "venís escuchando".
            - Nunca escribas "te venís escuchando".
            - No escribas "el usuario", "la persona", "este usuario", "su perfil" ni "sus gustos".
            - No inventes estadísticas ni cantidades.

            Requisitos para el headline:
            - Debe ser un retrato creativo de la personalidad musical de la persona.
            - Tiene que funcionar como un apodo musical elaborado, divertido y personalizado.
            - Debe tener entre 7 y 12 palabras.
            - Debe comenzar con mayúscula.
            - Basalo realmente en los artistas, canciones y géneros recibidos.
            - Puede combinar rasgos de personalidad, actitudes, emociones, géneros, hábitos, contradicciones y elementos sonoros.
            - Podés usar humor suave, ironía, dramatismo o una exageración simpática.
            - No debe parecer el nombre de una playlist.
            - No debe limitarse a describir un ambiente, una hora del día o un género.
            - No debe ser una simple enumeración de géneros o adjetivos.
            - No debe comenzar siempre con "Un" o "Una".
            - Variá libremente la estructura, el vocabulario y el enfoque.
            - Generá un título original para este recap.

            Referencias del tono buscado:
            - "Un nostálgico profesional que encuentra refugio en el dream pop"
            - "Romántico con distorsión y debilidad por las guitarras"
            - "Dramático elegante que convierte cada canción en una película"
            - "Un inconformista sonoro que siempre busca algo diferente"
            - "Melómano intenso con debilidad por la tristeza elegante"
            - "Especialista en emociones fuertes disfrazadas de canciones tranquilas"
            - "Corazón alternativo que nunca le escapa a una buena crisis"

            Estos ejemplos solamente indican el tono:
            - No los copies.
            - No los completes.
            - No los reformules mecánicamente.
            - No uses siempre las mismas palabras.
            - No repitas siempre la misma construcción gramatical.

            No uses títulos genéricos como:
            - "Un viaje musical"
            - "Tu perfil musical"
            - "Análisis de tus gustos"
            - "Resumen musical"
            - "Tu sonido musical"
            - "Una mezcla de géneros"
            - "Sonido nocturno y sensible"
            - "Melancólico bailable después de medianoche"

            Requisitos para el summary:
            - Debe tener 2 o 3 oraciones.
            - Debe comenzar con mayúscula.
            - Tiene que hablar directamente sobre lo que escuchaste.
            - Debe sonar natural, cercano y personalizado.
            - No debe parecer un informe técnico.
            - Usá "venís escuchando", nunca "te venís escuchando".

            Requisitos para las recomendaciones:
            - Generá exactamente 3 recomendaciones.
            - No recomiendes artistas ya presentes en el top.
            - Cada descripción debe comenzar con mayúscula.
            - Usá frases naturales como "te puede gustar", "podría entrar bien en tus escuchas", "va con lo que venís escuchando" o "tiene algo de ese clima que aparece en tu recap".
            - No uses "te venís escuchando".
            - Evitá frases robóticas como "encaja con tu mezcla", "perfil musical del usuario" o "basado en tus preferencias".

            Formato:
            - Basate únicamente en el período seleccionado.
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

                val genre = item
                    .optString("genre")
                    .trim()
                    .ifBlank {
                        "Recomendación musical"
                    }

                val reason = item
                    .optString("reason")
                    .trim()
                    .ifBlank {
                        "Puede ir con lo que venís escuchando."
                    }

                add(
                    AiArtistRecommendation(
                        name = name,
                        genre = capitalizeFirstLetter(genre),
                        reason = capitalizeFirstLetter(
                            normalizeSecondPersonText(reason)
                        )
                    )
                )
            }
        }.take(3)

        if (recommendations.size < 3) {
            throw IllegalStateException(
                "La IA no generó suficientes recomendaciones."
            )
        }

        val rawHeadline = json
            .optString("headline")
            .trim()
            .ifBlank {
                DEFAULT_HEADLINE
            }

        val rawSummary = json
            .optString("summary")
            .trim()
            .ifBlank {
                "Tu escucha de este período muestra una combinación de artistas, canciones y géneros que marcaron tu sonido."
            }

        return AiRecapContent(
            headline = capitalizeFirstLetter(
                normalizeHeadline(
                    normalizeSecondPersonText(rawHeadline)
                )
            ),
            summary = capitalizeFirstLetter(
                normalizeSecondPersonText(rawSummary)
            ),
            recommendations = recommendations
        )
    }

    private fun normalizeHeadline(
        text: String
    ): String {
        val cleanText = text
            .removeSuffix(".")
            .trim()

        val lowerText = cleanText.lowercase()

        val genericHeadlines = listOf(
            "un viaje musical",
            "tu perfil musical",
            "análisis de tus gustos",
            "resumen musical",
            "tu sonido musical",
            "una mezcla de géneros",
            "sonido nocturno y sensible",
            "melancólico bailable después de medianoche"
        )

        val isGeneric = genericHeadlines.any {
            lowerText.contains(it)
        }

        return if (isGeneric) {
            DEFAULT_HEADLINE
        } else {
            cleanText
        }
    }

    private fun normalizeSecondPersonText(
        text: String
    ): String {
        return text
            .replace(
                "El perfil musical de este usuario",
                "Tu perfil musical",
                ignoreCase = true
            )
            .replace(
                "El perfil musical del usuario",
                "Tu perfil musical",
                ignoreCase = true
            )
            .replace(
                "El perfil musical de la persona",
                "Tu perfil musical",
                ignoreCase = true
            )
            .replace(
                "este usuario",
                "vos",
                ignoreCase = true
            )
            .replace(
                "el usuario",
                "vos",
                ignoreCase = true
            )
            .replace(
                "la persona",
                "vos",
                ignoreCase = true
            )
            .replace(
                "su perfil musical",
                "tu perfil musical",
                ignoreCase = true
            )
            .replace(
                "sus gustos",
                "tus gustos",
                ignoreCase = true
            )
            .replace(
                "su música",
                "tu música",
                ignoreCase = true
            )
            .replace(
                "encaja con tu mezcla",
                "va con lo que venís escuchando",
                ignoreCase = true
            )
            .replace(
                "te venís escuchando",
                "venís escuchando",
                ignoreCase = true
            )
            .replace(
                "te vienes escuchando",
                "venís escuchando",
                ignoreCase = true
            )
            .trim()
    }

    private fun capitalizeFirstLetter(
        text: String
    ): String {
        val cleanText = text.trim()

        if (cleanText.isBlank()) {
            return cleanText
        }

        return cleanText.replaceFirstChar { character ->
            character.uppercaseChar()
        }
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
        const val DEFAULT_HEADLINE =
            "Corazón alternativo con debilidad por las emociones intensas"

        const val SYSTEM_PROMPT =
            "Sos el analista musical de Recapped. " +
                    "Interpretás estadísticas reales de Last.fm y generás análisis breves, personalizados y claros. " +
                    "El título debe ser un retrato original y divertido de la personalidad musical, como un apodo elaborado. " +
                    "No debe parecer el nombre de una playlist ni copiar las referencias proporcionadas. " +
                    "Todos los títulos y descripciones deben comenzar con mayúscula. " +
                    "Siempre hablás directamente en segunda persona dentro del análisis y las recomendaciones. " +
                    "Usás la expresión 'venís escuchando', nunca 'te venís escuchando'. " +
                    "Nunca decís 'el usuario', 'este usuario' ni hablás en tercera persona. " +
                    "Nunca usás frases robóticas como 'encaja con tu mezcla'. " +
                    "Nunca modificás ni inventás los datos recibidos."
    }
}