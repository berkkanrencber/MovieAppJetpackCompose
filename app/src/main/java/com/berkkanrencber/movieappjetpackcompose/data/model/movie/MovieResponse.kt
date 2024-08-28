package com.berkkanrencber.movieappjetpackcompose.data.model.movie

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    @SerializedName("page") val page: Int?,
    @SerializedName("results") var results: List<Movie>,
    @SerializedName("total_pages") val totalPages: Int?,
    @SerializedName("total_results") val totalResults: Int?,
)