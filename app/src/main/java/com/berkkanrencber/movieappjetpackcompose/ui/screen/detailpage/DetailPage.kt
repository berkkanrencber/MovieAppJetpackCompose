package com.berkkanrencber.movieappjetpackcompose.ui.screen.detailpage

import android.content.Intent
import android.graphics.Paint.Style
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.berkkanrencber.movieappjetpackcompose.data.model.movie.Movie
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.berkkanrencber.movieappjetpackcompose.R
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberAsyncImagePainter
import com.berkkanrencber.movieappjetpackcompose.utils.formatVoteAverage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.berkkanrencber.movieappjetpackcompose.data.model.actor.Actor
import com.berkkanrencber.movieappjetpackcompose.data.model.actorMovie.ActorMovie
import com.berkkanrencber.movieappjetpackcompose.data.model.credit.Cast
import com.berkkanrencber.movieappjetpackcompose.data.model.movieDetail.MovieDetail
import com.berkkanrencber.movieappjetpackcompose.data.model.movieImage.Backdrops
import com.berkkanrencber.movieappjetpackcompose.data.model.review.Review
import com.berkkanrencber.movieappjetpackcompose.data.room.FavoriteMovie
import com.berkkanrencber.movieappjetpackcompose.ui.MainActivity
import com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen.CustomToastComposable
import com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen.NoInternetDialog
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Black
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Charcoal
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Gray
import com.berkkanrencber.movieappjetpackcompose.ui.theme.LightBlack
import com.berkkanrencber.movieappjetpackcompose.ui.theme.MetallicSilver
import com.berkkanrencber.movieappjetpackcompose.ui.theme.SoftGray
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Transparent
import com.berkkanrencber.movieappjetpackcompose.utils.Constants
import com.berkkanrencber.movieappjetpackcompose.utils.Constants.IMAGE_URL
import com.berkkanrencber.movieappjetpackcompose.utils.NetworkUtil
import com.berkkanrencber.movieappjetpackcompose.utils.checkAndFetch
import com.berkkanrencber.movieappjetpackcompose.utils.formatRuntime
import com.berkkanrencber.movieappjetpackcompose.utils.fromHtmlToPlainText
import com.berkkanrencber.movieappjetpackcompose.utils.getImageUrl
import com.berkkanrencber.movieappjetpackcompose.utils.toFavoriteMovie
import com.berkkanrencber.movieappjetpackcompose.utils.toFormattedDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
@AndroidEntryPoint
class DetailPage : Fragment() {
    private val viewModel: DetailViewModel by viewModels()
    private var isFavorite = false
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                val movieId = arguments?.getString("movieId")?.toIntOrNull() ?: return@setContent
                getInitialData(movieId)
                val movieDetail by viewModel.movieDetails.collectAsState()
                val similarMovies by viewModel.similarMovies.collectAsState()
                val castList by viewModel.movieCredits.collectAsState()
                val backdrops by viewModel.movieImageList.collectAsState()
                val movieReviews by viewModel.movieReviews.collectAsState()

                var showCustomToast by remember { mutableStateOf(false) }
                var toastMessage by remember { mutableStateOf("") }
                var toastIconResId by remember { mutableStateOf(R.drawable.ic_love) }

                var showNoInternetDialog by remember { mutableStateOf(false) }

                var isLoading by remember { mutableStateOf(true) }

                if(!NetworkUtil.isNetworkAvailable(context)){
                    showNoInternetDialog = true
                    NoInternetDialog(
                        onRetry = {
                            viewModel.retryDetailPageData(movieId)
                            showNoInternetDialog = false
                            navController.navigate(R.id.action_detailPage_self, Bundle().apply {
                                putString("movieId", movieId.toString() ?: "")
                            })
                        }
                    )
                }else{
                    if (showCustomToast) {
                        CustomToastComposable(
                            message = toastMessage,
                            iconResId = toastIconResId
                        )
                        LaunchedEffect(Unit) {
                            delay(2000L)
                            showCustomToast = false
                        }
                    }

                    fun showCustomToast(message: String, iconResId: Int) {
                        toastMessage = message
                        toastIconResId = iconResId
                        showCustomToast = true
                    }

                    val watchTrailer: (MovieDetail) -> Unit = { movieDetail ->
                        val trailerKey = getTrailerKey(movieDetail)
                        if (trailerKey != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${Constants.YOUTUBE_BASE_URL}$trailerKey"))
                            startActivity(intent)
                        } else {
                            showCustomToast("Trailer not found", R.drawable.ic_youtube)
                        }
                    }

                    checkFavorite(movieId)
                    movieDetail?.let { movie ->
                        movieReviews?.let {
                            DetailScreen(
                                movieDetail = movie,
                                onBackClick = { findNavController().navigateUp() },
                                onFavoriteClick = {
                                    if (!isFavorite) {
                                        viewModel.addFavoriteMovie(movie.toFavoriteMovie())
                                        showCustomToast("Added to favorites", R.drawable.ic_love)
                                    } else {
                                        viewModel.removeFavoriteMovie(movie.id?:0)
                                        showCustomToast("Removed from favorites", R.drawable.ic_broken_heart)
                                    }
                                },
                                onShareClick = { shareMovie(movie) },
                                onTrailerClick = { watchTrailer(movie) },
                                similarMovies = similarMovies ?: emptyList(),
                                castList = castList,
                                backdrops = backdrops ?: emptyList(),
                                isFavorite = isFavorite,
                                navController = navController,
                                reviews = it,
                                viewModel = viewModel
                            )
                        }
                    }
                }


            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeVisibilityBottomBar(false)
    }

    private fun checkFavorite(movieId: Int){
        viewLifecycleOwner.lifecycleScope.launch {
            isFavorite = viewModel.isFavorite(movieId)
        }
    }

    private fun shareMovie(movieDetail: MovieDetail) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            val trailerKey = getTrailerKey(movieDetail)
            val shareText = if (trailerKey != null) {
                getString(R.string.share_trailer_text, movieDetail.title, "${Constants.YOUTUBE_BASE_URL}$trailerKey")
            } else {
                getString(R.string.share_trailer_fallback, movieDetail.title)
            }
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }

    private fun getTrailerKey(movieDetail: MovieDetail): String? {
        return viewModel.movieTrailers.value?.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.key
    }

    private fun getInitialData(movieId: Int) {
        checkAndFetch(viewModel.movieDetails.value) {
            viewModel.getMovieDetail(movieId)
        }
        checkAndFetch(viewModel.movieImageList.value) {
            viewModel.getMovieImages(movieId)
        }
        checkAndFetch(viewModel.movieCredits.value) {
            viewModel.getMovieCast(movieId)
        }
        checkAndFetch(viewModel.similarMovies.value) {
            viewModel.getSimilarMovies(movieId)
        }
        checkAndFetch(viewModel.movieReviews.value) {
            viewModel.getMovieReviews(movieId)
        }
        checkAndFetch(viewModel.movieTrailers.value) {
            viewModel.getMovieTrailer(movieId)
        }
    }

    private fun changeVisibilityBottomBar(isVisible: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch{
            delay(100L)
            (activity as MainActivity).changeVisibilityBottomBar(isVisible)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    reviews: List<Review>,
    navController: NavController,
    movieDetail: MovieDetail,
    onBackClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onTrailerClick: () -> Unit,
    similarMovies: List<Movie>,
    castList: List<Cast>,
    backdrops: List<Backdrops>,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel
) {

    var isFavoriteState by remember { mutableStateOf(isFavorite) }
    var movieImage by remember { mutableStateOf("") }
    if (backdrops.isNotEmpty()) {
        movieImage = backdrops[0].filePath.toString()
    }else{
        movieImage = movieDetail.posterPath
    }
    var scale by remember { mutableStateOf(1f) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet = remember { mutableStateOf(false) }

    var selectedActor by remember { mutableStateOf<Actor?>(null) }
    val sheetStateActor = rememberModalBottomSheetState()
    val scopeActor = rememberCoroutineScope()
    var showBottomSheetActor = remember { mutableStateOf(false) }

    val actorDetails by viewModel.actorDetails.collectAsState()
    val actorMovies by viewModel.actorMovies.collectAsState()

    if(showBottomSheet.value){
        ReviewBottomSheet(reviews = reviews,showBottomSheet = showBottomSheet, sheetState = sheetState, scope = scope)
    }

    if(showBottomSheetActor.value && actorDetails!=null){
        ActorDetailBottomSheet(actor = actorDetails!!,showBottomSheet = showBottomSheetActor, sheetState = sheetStateActor, scope = scopeActor, actorMovies = actorMovies ?: emptyList(), navController = navController)
    }

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .background(LightBlack)
            .verticalScroll(rememberScrollState())
    ) {
        val (backgroundImage, gradientOverlay, backButton, title, detailsRow, genres, overview, loadMore, buttonsRow, scenesLabel, scenesRow, castLabel, castRow, similarMoviesLabel, similarMoviesRow) = createRefs()

        // Movie Background with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .constrainAs(backgroundImage) {
                    top.linkTo(parent.top)
                }
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = if (movieImage.isNotEmpty()) "${IMAGE_URL}${movieImage}" else null,
                    placeholder = painterResource(id = R.drawable.ic_empty_image),
                    error = painterResource(id = R.drawable.ic_missing)
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, LightBlack),
                        startY = 0f,
                        endY = 600f
                    )
                )
                .constrainAs(gradientOverlay) {
                    top.linkTo(backgroundImage.top)
                    bottom.linkTo(backgroundImage.bottom)
                }
        )

        // Back Button
        IconButton(
            onClick = { onBackClick() },
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp)
                .background(
                    color = Color(0x88FFFFFF),
                    shape = RoundedCornerShape(24.dp)
                )
                .constrainAs(backButton) {
                    top.linkTo(backgroundImage.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = null,
                tint = Color.White
            )
        }

        // Voting, Duration, Release Date Row
        Row(
            modifier = Modifier
                .constrainAs(detailsRow) {
                    bottom.linkTo(title.top)
                    start.linkTo(parent.start, margin = 16.dp)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row( modifier = Modifier.clickable {
                    showBottomSheet.value = true
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${movieDetail.voteAverage.formatVoteAverage()} (${movieDetail.voteCount} reviews)",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clock),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = movieDetail.runtime.formatRuntime(),
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row {
                Icon(
                    painter = painterResource(id = R.drawable.ic_date),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = movieDetail.releaseDate?.take(4) ?: "N/A",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        // Title
        Text(
            text = movieDetail.title ?: "-",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .constrainAs(title) {
                    bottom.linkTo(backgroundImage.bottom, margin = 8.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        )

        // Genres
        FlowRow(
            modifier = Modifier
                .constrainAs(genres) {
                    top.linkTo(backgroundImage.bottom, margin = 4.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            movieDetail.genres?.forEach { genre ->
                Text(
                    text = genre.name ?: "-",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Gray, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Overview
        var isExpanded by remember { mutableStateOf(false) }
        Text(
            text = movieDetail.overview ?: "",
            fontSize = 14.sp,
            color = Color.White,
            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Justify,
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp)
                .constrainAs(overview) {
                    top.linkTo(genres.bottom, margin = 8.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                }
        )

        TextButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier
                .constrainAs(loadMore) {
                    top.linkTo(overview.bottom, margin = (-4).dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = if (isExpanded) "Show Less" else "Load More",
                color = SoftGray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Buttons
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .constrainAs(buttonsRow) {
                    top.linkTo(loadMore.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onTrailerClick() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
            ) {
                Text(
                    text = stringResource(id = R.string.watch_trailer),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 6.dp, horizontal = 8.dp)
                .background(
                    color = Gray,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(8.dp)
            ){
                Image(
                    painter = painterResource(id = if (isFavoriteState) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scale)
                        .clickable {
                            onFavoriteClick(!isFavoriteState)
                            isFavoriteState = !isFavoriteState
                            scale = 1.2f
                            GlobalScope.launch {
                                delay(300)
                                scale = 1f
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = { onShareClick() },
                modifier = Modifier
                    .size(40.dp)
                    .wrapContentSize()
                    .background(Gray, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        // Scenes
        Text(
            text = stringResource(id = R.string.scenes),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .constrainAs(scenesLabel) {
                    top.linkTo(buttonsRow.bottom)
                    start.linkTo(parent.start)
                }
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.constrainAs(scenesRow) {
                top.linkTo(scenesLabel.bottom)
                start.linkTo(parent.start)
            }
        ) {
            items(backdrops) { backdrop ->
                MovieImageItem(backdrop) {
                    movieImage = backdrop.filePath.toString()
                }
            }
        }

        // Cast
        Text(
            text = stringResource(id = R.string.cast),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .constrainAs(castLabel) {
                    top.linkTo(scenesRow.bottom)
                    start.linkTo(parent.start)
                }
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.constrainAs(castRow) {
                top.linkTo(castLabel.bottom)
                start.linkTo(parent.start)
            }
        ) {
            items(castList) { cast ->
                CastItem(cast) {
                    showBottomSheetActor.value = true
                    cast.id?.let { it1 -> viewModel.getActorDetail(it1) }
                    cast.id?.let { it1 -> viewModel.getActorMovies(it1) }
                }
            }
        }

        // Similar Movies
        Text(
            text = stringResource(id = R.string.similar_movies),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .constrainAs(similarMoviesLabel) {
                    top.linkTo(castRow.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                }
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.constrainAs(similarMoviesRow) {
                top.linkTo(similarMoviesLabel.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom, margin = 8.dp)
                height = Dimension.preferredWrapContent
            }
        ) {
            items(similarMovies) { movie ->
                SimilarMovieItem(movie){
                    navController.navigate(R.id.action_detailPage_self, Bundle().apply {
                        putString("movieId", movie.id.toString() ?: "")
                    })
                }
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(
    reviews: List<Review>,
    showBottomSheet: MutableState<Boolean>,
    sheetState:  SheetState,
    scope: CoroutineScope,
){
    ModalBottomSheet(
        onDismissRequest = {
            showBottomSheet.value = false
        },
        sheetState = sheetState
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Reviews", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ReviewList(reviews)
            // Sheet content
            Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet.value = false
                    }
                }
            }) {
                Text("Hide bottom sheet")
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewList(reviews: List<Review>) {
    LazyColumn {
        items(reviews.size) { index ->
            ReviewItem(review = reviews[index])
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(text = review.author ?: "-", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = review.createdAt.toFormattedDate() ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = review.content.fromHtmlToPlainText() ?: "-", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorDetailBottomSheet(
    actor: Actor,
    showBottomSheet: MutableState<Boolean>,
    sheetState:  SheetState,
    scope: CoroutineScope,
    actorMovies: List<ActorMovie>,
    navController: NavController
){
    ModalBottomSheet(
        onDismissRequest = {
            showBottomSheet.value = false
        },
        sheetState = sheetState
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // Actor Image
                Image(
                    painter = rememberAsyncImagePainter(
                        model = if (actor.profilePath?.isNotEmpty() == true) "${IMAGE_URL}${actor.profilePath}" else null,
                        placeholder = painterResource(id = R.drawable.ic_profile_placeholder),
                        error = painterResource(id = R.drawable.ic_missing)
                    ),
                    contentDescription = "Actor Image",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )


                // Actor Name
                actor.name?.let {
                    Text(
                        text = it,
                        fontSize = 20.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                // Divider
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 32.dp)
                )

                // Birthday
                actor.birthday?.let { ActorDetailRow(iconRes = R.drawable.ic_birthday, detailText = it) }

                // Place of birth
                actor.placeOfBirth?.let { ActorDetailRow(iconRes = R.drawable.ic_home, detailText = it) }

                // Deathday
                actor.deathday?.let { ActorDetailRow(iconRes = R.drawable.ic_deathday, detailText = it) }

                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 32.dp)
                )

                // Actor Movies
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(actorMovies.size) { index ->
                        ActorMovieItem(actorMovie = actorMovies[index], onClick = {
                            navController.navigate(R.id.action_detailPage_self, Bundle().apply {
                                putString("movieId", actorMovies[index].id.toString() ?: "")
                            })
                        })
                    }
                }

                // Biography
                actor.biography?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
            // Sheet content
            Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet.value = false
                    }
                }
            }) {
                Text("Hide bottom sheet")
            }
        }

    }
}

@Composable
fun ActorDetailRow(iconRes: Int, detailText: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 32.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = detailText,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun ActorMovieItem(actorMovie: ActorMovie, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .wrapContentHeight()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .background(
                color = Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth().clickable { onClick() }
        ) {
            val (image, character) = createRefs()

            Image(
                painter = rememberAsyncImagePainter(model = "$IMAGE_URL${actorMovie.posterPath}"),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                contentScale = ContentScale.Crop
            )
            Text(
                text = actorMovie.character?: "Unknown",
                color = Black,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(character) {
                    top.linkTo(image.bottom, margin = 8.dp)
                    start.linkTo(image.start)
                    end.linkTo(image.end)
                },
                textAlign = TextAlign.Center
            )
        }
    }
}



@Composable
fun CastItem(cast: Cast, onClick: (Cast) -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .padding(8.dp)
            .width(200.dp)
            .clickable { onClick(cast) }
            .background(Gray, RoundedCornerShape(12.dp))
    ) {
        val (image, textColumn) = createRefs()

        Image(
            painter = rememberAsyncImagePainter(cast.profilePath?.getImageUrl()),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .constrainAs(image) {
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(parent.top, margin = 8.dp)
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                },
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .constrainAs(textColumn) {
                    start.linkTo(image.end, margin = 16.dp)
                    top.linkTo(image.top)
                    bottom.linkTo(image.bottom)
                    end.linkTo(parent.end, margin = 8.dp)
                    width = Dimension.fillToConstraints
                },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = cast.name ?: "-",
                fontSize = 14.sp,
                color = Color.White
            )

            Text(
                text = cast.character ?: "-",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}




@Composable
fun SimilarMovieItem(movie: Movie, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .wrapContentHeight()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .background(
                color = Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (image, title, year, votingLayout) = createRefs()

            Image(
                painter = rememberAsyncImagePainter(model = "$IMAGE_URL${movie.posterPath}"),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                contentScale = ContentScale.Crop
            )
            Text(
                text = movie.title ?: "-",
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(image.bottom, margin = 8.dp)
                    start.linkTo(image.start)
                    end.linkTo(image.end)
                },
                textAlign = TextAlign.Center
            )

            Text(
                text = movie.releaseDate?.take(4) ?: "-",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.constrainAs(year) {
                    top.linkTo(title.bottom, margin = 8.dp)
                    start.linkTo(parent.start, margin = 8.dp)
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.constrainAs(votingLayout) {
                    top.linkTo(title.bottom, margin = 8.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = movie.voteAverage.formatVoteAverage() ?: "-",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
fun MovieImageItem(
    backdrop: Backdrops,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .width(120.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = "${IMAGE_URL}${backdrop.filePath}",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}


