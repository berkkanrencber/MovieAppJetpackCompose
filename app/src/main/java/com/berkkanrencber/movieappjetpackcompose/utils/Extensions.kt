package com.berkkanrencber.movieappjetpackcompose.utils

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import coil.load
import com.berkkanrencber.movieappjetpackcompose.R
import com.berkkanrencber.movieappjetpackcompose.data.model.movie.Movie
import com.berkkanrencber.movieappjetpackcompose.data.model.movieDetail.MovieDetail
import com.berkkanrencber.movieappjetpackcompose.data.room.FavoriteMovie
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Double?.formatVoteAverage(): String =
    this?.let {
        String.format("%.1f", it)
    } ?: "N/A"

fun Double?.formatRuntime(): String =
    if (this != null) {
        val totalMinutes = this.toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        String.format("%d h %02d m", hours, minutes)
    } else {
        "N/A"
    }

fun TextView.setMargins(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
) {
    val params =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
    params.setMargins(left, top, right, bottom)
    layoutParams = params
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toFormattedDate(): String {
    val instant = Instant.parse(this)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return localDateTime.format(formatter)
}

fun String.getImageUrl(): String = "${Constants.IMAGE_URL}$this"

fun ImageView.loadImage(
    progressBar: ProgressBar,
    url: String?,
) {
    if (url.isNullOrEmpty()) {
        this.setImageResource(R.drawable.ic_missing)
        progressBar.visibility = View.GONE
    } else {
        this.load(url) {
            placeholder(R.drawable.ic_empty_image)
            error(R.drawable.ic_empty_image)
            target { drawable ->

                this@loadImage.setImageDrawable(drawable)
            }
            listener(
                onStart = {
                    progressBar.visibility = View.VISIBLE
                },
                onSuccess = { _, _ ->
                    progressBar.visibility = View.GONE
                },
                onError = { _, _ ->
                    progressBar.visibility = View.GONE
                    this@loadImage.setImageResource(R.drawable.ic_missing)
                },
            )
        }
    }
}

fun TextView.updateText(text: String?) {
    text?.let {
        this.text = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
    } ?: run {
        this.text = "-"
    }
}

fun String?.fromHtmlToPlainText(): String {
    if (this == null) return ""
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(this).toString()
    }
}

fun <T> checkAndFetch(
    value: T?,
    fetchFunction: () -> Unit,
) {
    if (value == null || (value is Collection<*> && value.isEmpty())) {
        fetchFunction()
    }
}

fun pxToDp(
    context: Context,
    px: Int,
): Float = px / context.resources.displayMetrics.density

fun Movie.toFavoriteMovie(): FavoriteMovie {
    return FavoriteMovie(
        favoriteId = 0,
        id = this.id,
        title = this.title,
        posterPath = this.posterPath,
        averageVote = this.voteAverage,
        releaseDate = this.releaseDate,
        overview = this.overview
    )
}

fun MovieDetail.toFavoriteMovie(): FavoriteMovie {
    return FavoriteMovie(
        favoriteId = 0,
        id = this.id,
        title = this.title,
        posterPath = this.posterPath,
        averageVote = this.voteAverage,
        releaseDate = this.releaseDate,
        overview = this.overview
    )
}