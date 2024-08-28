package com.berkkanrencber.movieappjetpackcompose.data.repository

import com.berkkanrencber.movieappjetpackcompose.data.model.actor.Actor
import com.berkkanrencber.movieappjetpackcompose.data.model.actorMovie.ActorMovieResponse
import com.berkkanrencber.movieappjetpackcompose.data.model.credit.Credit
import com.berkkanrencber.movieappjetpackcompose.data.model.movie.Movie
import com.berkkanrencber.movieappjetpackcompose.data.model.movie.MovieResponse
import com.berkkanrencber.movieappjetpackcompose.data.model.movieDetail.MovieDetail
import com.berkkanrencber.movieappjetpackcompose.data.model.movieImage.MovieImage
import com.berkkanrencber.movieappjetpackcompose.data.model.movieSearch.MovieSearchResponse
import com.berkkanrencber.movieappjetpackcompose.data.model.movieTrailer.TrailerResponse
import com.berkkanrencber.movieappjetpackcompose.data.model.review.ReviewResponse
import com.berkkanrencber.movieappjetpackcompose.data.network.MovieApiService
import com.berkkanrencber.movieappjetpackcompose.data.room.FavoriteMovie
import com.berkkanrencber.movieappjetpackcompose.data.room.MovieDao
import com.berkkanrencber.movieappjetpackcompose.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
class MovieRepository
@Inject
constructor(
    private val apiService: MovieApiService,
    private val movieDao: MovieDao,
) {
    suspend fun getTopRatedMovieList(page: Int):MovieResponse? =
        try {
            val response = apiService.getTopRatedMovieList(page)
            response.results = updateMoviesWithFavoriteStatus(response.results)
            response
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getPopularMovieList(page: Int): MovieResponse? =
        try {
            val response = apiService.getPopularMovieList(page)
            response.results = updateMoviesWithFavoriteStatus(response.results)
            response
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getUpcomingMovieList(page: Int): MovieResponse? =
        try {
            val response = apiService.getUpcomingMovieList(page)
            response.results = updateMoviesWithFavoriteStatus(response.results)
            response
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getNowPlayingMovieList(page: Int): MovieResponse? =
        try {
            val response = apiService.getNowPlayingMovieList(page)
            response.results = updateMoviesWithFavoriteStatus(response.results)
            response
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getMovieImages(movieId: Int): MovieImage? =
        try {
            apiService.getMovieImages(movieId)
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getMovieDetail(movieId: Int): MovieDetail? =
        try {
            apiService.getMovieDetail(movieId)
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getSearchedMovieList(
        query: String?,
        page: Int,
    ): MovieSearchResponse? =
        try {
            val response = apiService.searchMovies(query, page)
            response.results = updateMoviesWithFavoriteStatus(response.results)
            response
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getMovieCredits(movieId: Int): Credit? =
        try {
            apiService.getMovieCredits(movieId)
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getActorDetail(personId: Int): Actor? =
        try {
            apiService.getActorDetail(personId)
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getActorMovies(personId: Int): ActorMovieResponse? =
        try {
            apiService.getActorMovies(personId)
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getMovieReviews(movieId: Int): ReviewResponse? =
        try {
            apiService.getMovieReviews(movieId)
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getMovieTrailer(movieId: Int): TrailerResponse? =
        try {
            apiService.getMovieTrailers(movieId)
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun getSimilarMovies(movieId: Int): MovieResponse? =
        try {
            apiService.getSimilarMovies(movieId)
        } catch (e: Exception) {
            Result.Error(500, "No Internet Connection")
            null
        }

    suspend fun addFavorite(movie: FavoriteMovie) = movieDao.addFavorite(movie)

    suspend fun removeFavorite(movieId: Int) = movieDao.removeFavorite(movieId)

    fun getAllFavorites(): Flow<List<FavoriteMovie>> = movieDao.getAllFavorites()

    suspend fun isFavorite(movieId: Int): FavoriteMovie? = movieDao.isFavorite(movieId)

    suspend fun updateMoviesWithFavoriteStatus(movies: List<Movie>): List<Movie> {
        val favoriteMovies = movieDao.getAllFavorites().first()
        return movies.map { movie ->
            movie.copy(isFavorite = favoriteMovies.any { it.id == movie.id })
        }
    }
}