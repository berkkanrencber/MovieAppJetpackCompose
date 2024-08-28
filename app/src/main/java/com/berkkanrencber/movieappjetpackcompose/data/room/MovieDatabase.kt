package com.berkkanrencber.movieappjetpackcompose.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteMovie::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}