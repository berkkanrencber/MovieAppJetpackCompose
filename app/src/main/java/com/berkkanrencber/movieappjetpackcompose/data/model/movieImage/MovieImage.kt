package com.berkkanrencber.movieappjetpackcompose.data.model.movieImage

import com.google.gson.annotations.SerializedName

data class MovieImage(
    @SerializedName("id") val id: Int,
    @SerializedName("backdrops") val backdrops: List<Backdrops>?,
)