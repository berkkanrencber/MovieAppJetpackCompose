package com.berkkanrencber.movieappjetpackcompose.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movie")
data class FavoriteMovie(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "favorite_id")
    var favoriteId: Int,
    @ColumnInfo(name = "movie_id")
    var id: Int?,
    @ColumnInfo(name = "title")
    var title: String?,
    @ColumnInfo(name = "posterPath")
    var posterPath: String?,
    @ColumnInfo(name = "averageVote")
    var averageVote: Double?,
    @ColumnInfo(name = "releaseDate")
    var releaseDate: String?,
    @ColumnInfo(name = "overview")
    var overview: String?,
)