package com.recapped.app.domain.model

enum class RecapPeriod(
    val key: String,
    val title: String,
    val subtitle: String,
    val lastFmValue: String,
    val days: Long
) {
    Week(
        key = "week",
        title = "Semana",
        subtitle = "últimos 7 días",
        lastFmValue = "7day",
        days = 7
    ),
    Month(
        key = "month",
        title = "Mes",
        subtitle = "últimos 30 días",
        lastFmValue = "1month",
        days = 30
    ),
    Quarter(
        key = "quarter",
        title = "Trimestre",
        subtitle = "últimos 3 meses",
        lastFmValue = "3month",
        days = 90
    ),
    Year(
        key = "year",
        title = "Año",
        subtitle = "últimos 12 meses",
        lastFmValue = "12month",
        days = 365
    );

    companion object {
        fun fromKey(key: String): RecapPeriod {
            return entries.firstOrNull { it.key == key } ?: Month
        }
    }
}

data class RecapResult(
    val username: String,
    val period: RecapPeriod,
    val generatedAt: Long,
    val totalScrobbles: Int,
    val uniqueArtists: Int,
    val uniqueTracks: Int,
    val topArtists: List<RecapArtist>,
    val topTracks: List<RecapTrack>,
    val genres: List<RecapGenre>,
    val aiHeadline: String,
    val aiSummary: String,
    val recommendations: List<RecapRecommendation>
)

data class RecapArtist(
    val rank: Int,
    val name: String,
    val playcount: Int,
    val imageUrl: String?,
    val tags: List<String>
)

data class RecapTrack(
    val rank: Int,
    val name: String,
    val artistName: String,
    val playcount: Int,
    val imageUrl: String?
)

data class RecapGenre(
    val name: String,
    val percentage: Int
)

data class RecapRecommendation(
    val name: String,
    val genre: String,
    val reason: String,
    val imageUrl: String?
)

data class AiRecapContent(
    val headline: String,
    val summary: String,
    val recommendations: List<AiArtistRecommendation>
)

data class AiArtistRecommendation(
    val name: String,
    val genre: String,
    val reason: String
)