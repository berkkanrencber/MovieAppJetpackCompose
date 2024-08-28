package com.berkkanrencber.movieappjetpackcompose.data.model.movieTrailer

import com.google.gson.annotations.SerializedName

data class TrailerResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("results") val results: List<Trailer>?
)