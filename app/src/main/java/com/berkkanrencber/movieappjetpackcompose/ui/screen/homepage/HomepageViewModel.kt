package com.berkkanrencber.movieappjetpackcompose.ui.screen.homepage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkkanrencber.movieappjetpackcompose.data.model.movie.Movie
import com.berkkanrencber.movieappjetpackcompose.data.repository.MovieRepository
import com.berkkanrencber.movieappjetpackcompose.data.room.FavoriteMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class HomepageViewModel
@Inject
constructor(
    private val repository: MovieRepository,
) : ViewModel() {
    private val _topRatedMovieList = MutableStateFlow<List<Movie>>(emptyList())
    val topRatedMovieList: StateFlow<List<Movie>> get() = _topRatedMovieList

    private val _popularMovieList = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovieList: StateFlow<List<Movie>> get() = _popularMovieList

    private val _upcomingMovieList = MutableStateFlow<List<Movie>>(emptyList())
    val upcomingMovieList: StateFlow<List<Movie>> get() = _upcomingMovieList

    private val _nowPlayingMovieList = MutableStateFlow<List<Movie>>(emptyList())
    val nowPlayingMovieList: StateFlow<List<Movie>> get() = _nowPlayingMovieList

    private val _searchMovieList = MutableStateFlow<List<Movie>>(emptyList())
    val searchMovieList: StateFlow<List<Movie>> get() = _searchMovieList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _showNoInternetDialog = MutableStateFlow(false)
    val showNoInternetDialog: StateFlow<Boolean> get() = _showNoInternetDialog

    private val _isGridLayout = MutableStateFlow(false)
    val isGridLayout: StateFlow<Boolean> get() = _isGridLayout

    private var currentPage = 1
    private var currentPageTopRated = 1
    private var currentPagePopular = 1
    private var currentPageUpcoming = 1
    private var currentPageNowPlaying = 1
    private var totalPages = 1
    private var totalPagesTopRated = 1
    private var totalPagesPopular = 1
    private var totalPagesUpcoming = 1
    private var totalPagesNowPlaying = 1

    fun getTopRatedMovieList(isNextPage: Boolean = false) {
        checkIsNextPageTopRated(isNextPage)
        _isLoading.value = true
        viewModelScope.launch {
            val response = repository.getTopRatedMovieList(currentPageTopRated)
            response?.let {
                totalPagesTopRated = it.totalPages!!
                _topRatedMovieList.value = _topRatedMovieList.value + response.results
            } ?: run {
                if (currentPageTopRated <= 500) {
                    _showNoInternetDialog.value = true
                }
            }
        }
        _isLoading.value = false
    }

    fun getPopularMovieList(isNextPage: Boolean = false) {
        checkIsNextPagePopular(isNextPage)
        _isLoading.value = true
        viewModelScope.launch {
            val response = repository.getPopularMovieList(currentPagePopular)
            response?.let {
                totalPagesPopular = it.totalPages!!
                _popularMovieList.value = _popularMovieList.value + response.results
            } ?: run {
                if (currentPagePopular <= 500) {
                    _showNoInternetDialog.value = true
                }
            }
        }
        _isLoading.value = false
    }

    fun getUpcomingMovieList(isNextPage: Boolean = false) {
        checkIsNextPageUpcoming(isNextPage)
        _isLoading.value = true
        viewModelScope.launch {
            val response = repository.getUpcomingMovieList(currentPageUpcoming)
            response?.let {
                totalPagesUpcoming = it.totalPages!!
                _upcomingMovieList.value = _upcomingMovieList.value + response.results
            } ?: run {
                if (currentPageUpcoming < 500) {
                    _showNoInternetDialog.value = true
                }
            }
        }
        _isLoading.value = false
    }

    fun getNowPlayingMovieList(isNextPage: Boolean = false) {
        checkIsNextPageNowPlaying(isNextPage)
        _isLoading.value = true
        viewModelScope.launch {
            val response = repository.getNowPlayingMovieList(currentPageNowPlaying)
            response?.let {
                totalPagesNowPlaying = it.totalPages!!
                _nowPlayingMovieList.value = _nowPlayingMovieList.value + response.results
            } ?: run {
                if (currentPageNowPlaying < 500) {
                    _showNoInternetDialog.value = true
                }
            }
        }
        _isLoading.value = false
    }

    fun getSearchedMovieList(
        query: String?,
        isNextPage: Boolean = false,
    ) {
        checkIsNextPage(isNextPage)
        _isLoading.value = true
        viewModelScope.launch {
            val response = repository.getSearchedMovieList(query, currentPage)
            response?.let {
                totalPages = it.totalPages!!
                _searchMovieList.value = _searchMovieList.value + response.results
            } ?: run {
                _showNoInternetDialog.value = true
            }
        }
        _isLoading.value = false
    }

    private fun checkIsNextPage(isNextPage: Boolean) {
        if (isNextPage) {
            currentPage++
        } else {
            currentPage = 1
            _searchMovieList.value = emptyList()
        }
    }

    private fun checkIsNextPageTopRated(isNextPage: Boolean) {
        if (isNextPage) {
            currentPageTopRated++
        } else {
            currentPageTopRated = 1
            _topRatedMovieList.value = emptyList()
        }
    }

    private fun checkIsNextPagePopular(isNextPage: Boolean) {
        if (isNextPage) {
            currentPagePopular++
        } else {
            currentPagePopular = 1
            _popularMovieList.value = emptyList()
        }
    }

    private fun checkIsNextPageUpcoming(isNextPage: Boolean) {
        if (isNextPage) {
            currentPageUpcoming++
        } else {
            currentPageUpcoming = 1
            _upcomingMovieList.value = emptyList()
        }
    }

    private fun checkIsNextPageNowPlaying(isNextPage: Boolean) {
        if (isNextPage) {
            currentPageNowPlaying++
        } else {
            currentPageNowPlaying = 1
            _nowPlayingMovieList.value = emptyList()
        }
    }




    fun addFavoriteMovie(movie: FavoriteMovie) {
        viewModelScope.launch {
            repository.addFavorite(movie)
        }
    }

    fun removeFavoriteMovie(movieId: Int) {
        viewModelScope.launch {
            repository.removeFavorite(movieId)
        }
    }

    fun retryTopRatedFetchingData() {
        _showNoInternetDialog.value = false
        getTopRatedMovieList()
    }

    fun retryPopularFetchingData() {
        _showNoInternetDialog.value = false
        getPopularMovieList()
    }

    fun retryUpcomingFetchingData() {
        _showNoInternetDialog.value = false
        getUpcomingMovieList()
    }

    fun retryNowPlayingFetchingData() {
        _showNoInternetDialog.value = false
        getNowPlayingMovieList()
    }

    fun retryFetchingData() {
        _showNoInternetDialog.value = false
    }

    fun canLoadMore(): Boolean = currentPage < totalPages

    fun canLoadMoreTopRated(): Boolean = currentPageTopRated < totalPagesTopRated
    fun canLoadMorePopular(): Boolean {
        return currentPagePopular < totalPagesPopular
    }
    fun canLoadMoreUpcoming(): Boolean = currentPageUpcoming < totalPagesUpcoming
    fun canLoadMoreNowPlaying(): Boolean = currentPageNowPlaying < totalPagesNowPlaying


    fun resetMovieList() {
        _searchMovieList.value = emptyList()
    }

    fun toggleLayout() {
        _isGridLayout.value = !_isGridLayout.value
    }

    fun setGridLayout(isGrid: Boolean) {
        _isGridLayout.value = isGrid
    }
}