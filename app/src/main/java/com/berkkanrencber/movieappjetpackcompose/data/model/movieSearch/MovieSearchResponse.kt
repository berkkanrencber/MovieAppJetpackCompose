package com.berkkanrencber.movieappjetpackcompose.data.model.movieSearch

import com.berkkanrencber.movieappjetpackcompose.data.model.movie.Movie
import com.google.gson.annotations.SerializedName

data class MovieSearchResponse(
    @SerializedName("results") var results: List<Movie>,
    @SerializedName("total_pages") val totalPages: Int?,
)