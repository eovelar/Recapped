package com.recapped.app.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.UserProfileRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChartsContentType {
    ARTISTS,
    SONGS
}

enum class ChartsViewMode {
    GRID,
    LIST
}

data class ChartSong(
    val rank: Int,
    val name: String,
    val artistName: String,
    val playcount: Long,
    val imageUrl: String?
)

sealed interface ChartsPhase {
    data object Loading : ChartsPhase

    data class Success(
        val artists: List<Artist>,
        val songs: List<ChartSong> = emptyList()
    ) : ChartsPhase

    data class Error(
        val message: String
    ) : ChartsPhase
}

data class ChartsUiState(
    val phase: ChartsPhase = ChartsPhase.Loading,
    val query: String = "",
    val contentType: ChartsContentType =
        ChartsContentType.ARTISTS,
    val viewMode: ChartsViewMode = ChartsViewMode.GRID,
    val isLoadingSongs: Boolean = false,
    val isSearching: Boolean = false,
    val remoteArtists: List<Artist> = emptyList(),
    val remoteSongs: List<ChartSong> = emptyList(),
    val searchError: String? = null
) {
    val isRemoteSearch: Boolean
        get() = query.trim().length >= MIN_SEARCH_LENGTH

    val filteredArtists: List<Artist>
        get() {
            if (isRemoteSearch) {
                return remoteArtists
            }

            val success = phase as? ChartsPhase.Success
                ?: return emptyList()

            return if (query.isBlank()) {
                success.artists
            } else {
                success.artists.filter { artist ->
                    artist.name.contains(
                        query,
                        ignoreCase = true
                    )
                }
            }
        }

    val filteredSongs: List<ChartSong>
        get() {
            if (isRemoteSearch) {
                return remoteSongs
            }

            val success = phase as? ChartsPhase.Success
                ?: return emptyList()

            return if (query.isBlank()) {
                success.songs
            } else {
                success.songs.filter { song ->
                    song.name.contains(
                        query,
                        ignoreCase = true
                    ) ||
                            song.artistName.contains(
                                query,
                                ignoreCase = true
                            )
                }
            }
        }

    companion object {
        private const val MIN_SEARCH_LENGTH = 2
    }
}

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChartsUiState()
    )

    val state: StateFlow<ChartsUiState> =
        _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { current ->
                current.copy(
                    phase = ChartsPhase.Loading,
                    isLoadingSongs = true
                )
            }

            val username = try {
                userProfileRepository
                    .getLastFmUsername()
            } catch (error: Exception) {
                showError(
                    error.message
                        ?: "No se pudo leer tu usuario de Last.fm."
                )
                return@launch
            }

            if (username.isNullOrBlank()) {
                showError(
                    "No hay una cuenta de Last.fm vinculada."
                )
                return@launch
            }

            loadTopSongs(username)
            loadTopArtists(username)
        }
    }

    private suspend fun loadTopArtists(
        username: String
    ) {
        artistRepository
            .getUserTopArtists(
                username = username,
                period = CHART_PERIOD
            )
            .collect { result ->
                when (result) {
                    Resource.Loading -> Unit

                    is Resource.Error -> {
                        val currentPhase =
                            _state.value.phase

                        if (
                            currentPhase !is ChartsPhase.Success ||
                            currentPhase.artists.isEmpty()
                        ) {
                            showError(result.message)
                        }
                    }

                    is Resource.Success -> {
                        _state.update { current ->
                            val songs =
                                (current.phase
                                        as? ChartsPhase.Success)
                                    ?.songs
                                    .orEmpty()

                            current.copy(
                                phase = ChartsPhase.Success(
                                    artists = result.data,
                                    songs = songs
                                )
                            )
                        }
                    }
                }
            }
    }

    private fun loadTopSongs(
        username: String
    ) {
        viewModelScope.launch {
            when (
                val result =
                    artistRepository.getUserTopSongs(
                        username = username,
                        period = CHART_PERIOD,
                        limit = SONG_LIMIT
                    )
            ) {
                Resource.Loading -> Unit

                is Resource.Error -> {
                    _state.update { current ->
                        current.copy(
                            isLoadingSongs = false
                        )
                    }
                }

                is Resource.Success -> {
                    val songs = result.data.map { song ->
                        ChartSong(
                            rank = song.rank,
                            name = song.name,
                            artistName = song.artistName,
                            playcount = song.playcount,
                            imageUrl = song.imageUrl
                        )
                    }

                    _state.update { current ->
                        val artists =
                            (current.phase
                                    as? ChartsPhase.Success)
                                ?.artists
                                .orEmpty()

                        current.copy(
                            phase = ChartsPhase.Success(
                                artists = artists,
                                songs = songs
                            ),
                            isLoadingSongs = false
                        )
                    }
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        val cleanQuery = query.take(MAX_QUERY_LENGTH)

        _state.update { current ->
            current.copy(
                query = cleanQuery,
                remoteArtists = emptyList(),
                remoteSongs = emptyList(),
                searchError = null
            )
        }

        scheduleRemoteSearch()
    }

    fun onContentTypeChange(
        type: ChartsContentType
    ) {
        _state.update { current ->
            current.copy(
                contentType = type,
                remoteArtists = emptyList(),
                remoteSongs = emptyList(),
                searchError = null
            )
        }

        scheduleRemoteSearch()
    }

    private fun scheduleRemoteSearch() {
        searchJob?.cancel()

        val query = _state.value.query.trim()

        if (query.length < MIN_SEARCH_LENGTH) {
            _state.update { current ->
                current.copy(isSearching = false)
            }
            return
        }

        searchJob = viewModelScope.launch {
            _state.update { current ->
                current.copy(isSearching = true)
            }

            delay(SEARCH_DEBOUNCE_MS)

            when (_state.value.contentType) {
                ChartsContentType.ARTISTS -> {
                    searchArtists(query)
                }

                ChartsContentType.SONGS -> {
                    searchSongs(query)
                }
            }
        }
    }

    private suspend fun searchArtists(
        query: String
    ) {
        when (
            val result = artistRepository.searchArtists(
                query = query,
                limit = SEARCH_LIMIT
            )
        ) {
            Resource.Loading -> Unit

            is Resource.Success -> {
                _state.update { current ->
                    current.copy(
                        remoteArtists = result.data,
                        isSearching = false,
                        searchError = null
                    )
                }
            }

            is Resource.Error -> {
                showSearchError(result.message)
            }
        }
    }

    private suspend fun searchSongs(
        query: String
    ) {
        when (
            val result = artistRepository.searchSongs(
                query = query,
                limit = SEARCH_LIMIT
            )
        ) {
            Resource.Loading -> Unit

            is Resource.Success -> {
                val songs = result.data.map { song ->
                    ChartSong(
                        rank = song.rank,
                        name = song.name,
                        artistName = song.artistName,
                        playcount = song.playcount,
                        imageUrl = song.imageUrl
                    )
                }

                _state.update { current ->
                    current.copy(
                        remoteSongs = songs,
                        isSearching = false,
                        searchError = null
                    )
                }
            }

            is Resource.Error -> {
                showSearchError(result.message)
            }
        }
    }

    fun onViewModeChange(
        mode: ChartsViewMode
    ) {
        _state.update { current ->
            current.copy(viewMode = mode)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    private fun showError(message: String) {
        _state.update { current ->
            current.copy(
                phase = ChartsPhase.Error(message),
                isLoadingSongs = false
            )
        }
    }

    private fun showSearchError(message: String) {
        _state.update { current ->
            current.copy(
                remoteArtists = emptyList(),
                remoteSongs = emptyList(),
                isSearching = false,
                searchError = message
            )
        }
    }

    companion object {
        private const val CHART_PERIOD = "1month"
        private const val SONG_LIMIT = 15
        private const val SEARCH_LIMIT = 10
        private const val MIN_SEARCH_LENGTH = 2
        private const val MAX_QUERY_LENGTH = 60
        private const val SEARCH_DEBOUNCE_MS = 400L
    }
}
