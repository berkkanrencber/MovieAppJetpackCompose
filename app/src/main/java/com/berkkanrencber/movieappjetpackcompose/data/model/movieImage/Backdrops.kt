package com.berkkanrencber.movieappjetpackcompose.data.model.movieImage

import com.google.gson.annotations.SerializedName

data class Backdrops(
    @SerializedName("file_path") val filePath: String?,
)