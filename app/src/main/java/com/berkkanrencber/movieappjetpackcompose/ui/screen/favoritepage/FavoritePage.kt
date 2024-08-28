package com.berkkanrencber.movieappjetpackcompose.ui.screen.favoritepage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberAsyncImagePainter
import com.berkkanrencber.movieappjetpackcompose.R
import com.berkkanrencber.movieappjetpackcompose.data.room.FavoriteMovie
import com.berkkanrencber.movieappjetpackcompose.utils.formatVoteAverage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.berkkanrencber.movieappjetpackcompose.data.model.movie.Movie
import com.berkkanrencber.movieappjetpackcompose.ui.MainActivity
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Charcoal
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Gray
import com.berkkanrencber.movieappjetpackcompose.ui.theme.LightBlack
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Transparent
import com.berkkanrencber.movieappjetpackcompose.utils.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen.CustomToastComposable

@AndroidEntryPoint
class FavoritePage : Fragment() {

    private val viewModel: FavoriteViewModel by viewModels()

    private var isGridLayout = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                FavoriteScreen(navController = navController,viewModel = viewModel, isGridLayout = isGridLayout)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isGridLayout = savedInstanceState?.getBoolean("isGridLayout") ?: isGridLayout
        changeVisibilityBottomBar(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isGridLayout", isGridLayout)
    }

    private fun changeVisibilityBottomBar(isVisible: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch{
            delay(100L)
            (activity as MainActivity).changeVisibilityBottomBar(isVisible)
        }
    }
}


@Composable
fun FavoriteScreen(navController: NavController,viewModel: FavoriteViewModel, isGridLayout: Boolean) {
    val favoriteMovies by viewModel.favoriteMovieList.collectAsState()
    var gridLayout by remember { mutableStateOf(isGridLayout) }

    var showCustomToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastIconResId by remember { mutableStateOf(R.drawable.ic_broken_heart) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlack)
    ) {
        TopBar(
            isGridLayout = gridLayout,
            onToggleLayout = { gridLayout = !gridLayout }
        )
        if (favoriteMovies.isEmpty()) {
            FavoriteEmptyState()
        } else {
            if (showCustomToast) {
                CustomToastComposable(
                    message = "Removed from favorites",
                    iconResId = R.drawable.ic_broken_heart
                )
                LaunchedEffect(Unit) {
                    delay(2000L)
                    showCustomToast = false
                }
            }
            MovieList(
                navController = navController,
                movies = favoriteMovies,
                isGridLayout = gridLayout,
                onRemove = { movieId ->
                    viewModel.removeFavoriteMovie(movieId)
                    toastMessage = "Removed from favorites"
                    toastIconResId = R.drawable.ic_broken_heart
                    showCustomToast = true
                }
            )
        }
    }
}

@Composable
fun TopBar(isGridLayout: Boolean, onToggleLayout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(id = R.string.app_name),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textDecoration = TextDecoration.Underline
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onToggleLayout) {
            Icon(
                painter = painterResource(id = if (isGridLayout) R.drawable.ic_linear else R.drawable.ic_grid),
                contentDescription = "Toggle Layout"
            )
        }
    }
}

@Composable
fun MovieList(
    navController: NavController,
    movies: List<FavoriteMovie>,
    isGridLayout: Boolean,
    onRemove: (Int) -> Unit
) {
    if (isGridLayout) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(movies.size) { index ->
                MovieGridItem(
                    movie = movies[index],
                    onRemove = {
                        movies[index].id?.let { it1 -> onRemove(it1) }
                    },
                    onClick = {
                        navController.navigate(R.id.action_favoritePage_to_detailPage, Bundle().apply {
                            putString("movieId", movies[index].id.toString() ?: "")
                        })
                    }
                )
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(8.dp)) {
            items(movies.size) { index ->
                MovieLinearItem(
                    movie = movies[index],
                    onRemove = {
                        movies[index].id?.let { it1 -> onRemove(it1) }
                    },
                    onClick = {
                        navController.navigate(R.id.action_favoritePage_to_detailPage, Bundle().apply {
                            putString("movieId", movies[index].id.toString() ?: "")
                        })
                    }
                )
            }
        }
    }
}

@Composable
fun FavoriteEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_empty_favorites),
            contentDescription = null,
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.favorite_empty_state_icon_width))
                .height(dimensionResource(id = R.dimen.favorite_empty_state_icon_height))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.no_favorite_yet),
            fontSize = dimensionResource(id = R.dimen.favorite_empty_state_no_favorite_text_size).value.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.start_adding_favorite),
            fontSize = dimensionResource(id = R.dimen.favorite_empty_state_add_favorite_text_size).value.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun MovieGridItem(
    movie: FavoriteMovie,
    onRemove: (Int) -> Unit,
    onClick: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
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
            val (image, progressBar, title, year, votingLayout, favoriteIcon) = createRefs()

            Image(
                painter = rememberAsyncImagePainter(model = "${Constants.IMAGE_URL}${movie.posterPath}"),
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
                    text = movie.averageVote.formatVoteAverage() ?: "-",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_favorite_filled),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
                    .scale(scale)
                    .constrainAs(favoriteIcon) {
                        top.linkTo(image.top, margin = 4.dp)
                        end.linkTo(image.end, margin = 4.dp)
                    }
                    .clickable {
                        onRemove(movie.id ?: 0)
                        scale = 1.2f
                        GlobalScope.launch {
                            delay(300)
                            scale = 1f
                        }
                    }
            )
        }
    }
}

@Composable
fun MovieLinearItem(
    movie: FavoriteMovie,
    onRemove: (Int) -> Unit,
    onClick: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .border(
                width = 2.dp,
                color = Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val (image, title, overview, infoLayout, favoriteIcon) = createRefs()

            Box(
                modifier = Modifier
                    .size(100.dp, 150.dp)
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
            ) {
                AsyncImage(
                    model = "${Constants.IMAGE_URL}${movie.posterPath}",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = movie.title ?: "-",
                fontSize = 16.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(image.top)
                    start.linkTo(image.end, margin = 16.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                text = movie.overview ?: "-",
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.constrainAs(overview) {
                    top.linkTo(title.bottom, margin = 6.dp)
                    start.linkTo(image.end, margin = 16.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                    width = Dimension.fillToConstraints
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .constrainAs(infoLayout) {
                        start.linkTo(image.end, margin = 16.dp)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                        .background(
                            color = Gray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = movie.releaseDate?.take(4) ?: "-",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = Gray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .padding(6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = movie.averageVote.formatVoteAverage() ?: "-",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
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
                        painter = painterResource(id = R.drawable.ic_favorite_filled),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale)
                            .clickable {
                                onRemove(movie.id ?: 0)
                                scale = 1.2f
                                GlobalScope.launch {
                                    delay(300)
                                    scale = 1f
                                }
                            }
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Movie Grid Item Preview",
    showBackground = true,
    device = Devices.PIXEL_4
)
@Composable
fun PreviewMovieGridItem() {
    MovieGridItem(
        movie = FavoriteMovie(
            id = 1,
            title = "Sample Movie",
            posterPath = null,
            releaseDate = "2023-05-20",
            overview = "This is a sample overview for the movie.",
            averageVote = 8.5,
            favoriteId = 1,
        ),
        onRemove = {},
        onClick = {}
    )
}

@Preview(
    name = "Movie Linear Item Preview",
    showBackground = true,
    device = Devices.PIXEL_4
)
@Composable
fun PreviewMovieLinearItem() {
    MovieLinearItem(
        movie = FavoriteMovie(
            id = 1,
            title = "Sample Movie",
            posterPath = null,
            releaseDate = "2023-05-20",
            overview = "This is a sample overview for the movie.",
            averageVote = 8.5,
            favoriteId = 2
        ),
        onRemove = {},
        onClick = {}
    )
}
