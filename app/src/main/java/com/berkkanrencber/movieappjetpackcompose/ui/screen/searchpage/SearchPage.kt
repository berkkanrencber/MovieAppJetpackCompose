package com.berkkanrencber.movieappjetpackcompose.ui.screen.searchpage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.berkkanrencber.movieappjetpackcompose.data.model.movie.Movie
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.berkkanrencber.movieappjetpackcompose.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.berkkanrencber.movieappjetpackcompose.utils.formatVoteAverage
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.berkkanrencber.movieappjetpackcompose.ui.MainActivity
import com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen.CustomToastComposable
import com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen.NoInternetDialog
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Charcoal
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Gray
import com.berkkanrencber.movieappjetpackcompose.ui.theme.LightBlack
import com.berkkanrencber.movieappjetpackcompose.ui.theme.SoftGray
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Transparent
import com.berkkanrencber.movieappjetpackcompose.utils.Constants.IMAGE_URL
import com.berkkanrencber.movieappjetpackcompose.utils.NetworkUtil
import com.berkkanrencber.movieappjetpackcompose.utils.toFavoriteMovie
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchPage : Fragment() {

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                var showNoInternetDialog by remember { mutableStateOf(false) }

                if (!NetworkUtil.isNetworkAvailable(context)) {
                    showNoInternetDialog = true
                }
                SearchScreenContent(navController = navController,viewModel = viewModel,showNoInternetDialog=showNoInternetDialog,context= context)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSearchedMovieList("")
        changeVisibilityBottomBar(true)
    }

    private fun changeVisibilityBottomBar(isVisible: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch{
            delay(100L)
            (activity as MainActivity).changeVisibilityBottomBar(isVisible)
        }
    }
}

@Composable
fun SearchScreenContent(navController: NavController, viewModel: SearchViewModel, showNoInternetDialog: Boolean, context: Context) {
    var query by remember { mutableStateOf("") }
    val movies by viewModel.movieList.collectAsState()
    val showNoInternetDialog by viewModel.showNoInternetDialog.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showCustomToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastIconResId by remember { mutableStateOf(R.drawable.ic_love) }

    var showNoInternetPopUp by remember { mutableStateOf(showNoInternetDialog) }

    if (showNoInternetPopUp) {
        NoInternetDialog(
            onRetry = {
                if (!NetworkUtil.isNetworkAvailable(context)){
                    showNoInternetPopUp = true
                }else{
                    showNoInternetPopUp = false
                }
            }
        )
    }

    val scrollState = rememberLazyGridState()

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == movies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMore()) {
                    viewModel.getSearchedMovieList(query,isNextPage = true)
                }
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(LightBlack)) {
        SearchBar(
            query = query,
            onQueryChanged = { newQuery ->
                query = newQuery
                if (!NetworkUtil.isNetworkAvailable(context)) {
                    viewModel.resetMovieList()
                    showNoInternetPopUp = true
                } else {
                    viewModel.getSearchedMovieList(newQuery)
                }
            }
        )
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
        when {
            query.isEmpty() -> EmptyState()
            movies.isNotEmpty() -> MovieList(navController,movies, scrollState, viewModel,onShowCustomToast = { message, iconResId ->
                toastMessage = message
                toastIconResId = iconResId
                showCustomToast = true
            })
            else -> NotFoundState()
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}


@Composable
fun MovieList(navController: NavController,movies: List<Movie>, scrollState: LazyGridState, viewModel: SearchViewModel, onShowCustomToast: (String, Int) -> Unit) {
    LazyVerticalGrid(
        state = scrollState,
        columns = GridCells.Adaptive(120.dp),
        contentPadding = PaddingValues(8.dp),
        content = {
            items(movies.size) { index ->
                MovieGridItem(
                    movie = movies[index],
                    onAddFavorite = {
                        viewModel.addFavoriteMovie(it.toFavoriteMovie())
                        onShowCustomToast("Added to favorites", R.drawable.ic_love)
                                    },
                    onRemoveFavorite = {
                        viewModel.removeFavoriteMovie(movies[index].id ?: 0)
                        onShowCustomToast("Removed from favorites", R.drawable.ic_broken_heart)
                                       },
                    onClick = {
                        navController.navigate(R.id.action_searchPage_to_detailPage, Bundle().apply {
                            putString("movieId", movies[index].id.toString() ?: "")
                        })
                    }
                )
            }
        }
    )
}



@Composable
fun MovieGridItem(
    movie: Movie,
    onAddFavorite: (Movie) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    onClick: () -> Unit
) {
    var isFavoriteState by remember { mutableStateOf(movie.isFavorite) }
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
            val (image, title, year, votingLayout, favoriteIcon) = createRefs()

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

            Image(
                painter = painterResource(id = if (isFavoriteState) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
                    .constrainAs(favoriteIcon) {
                        top.linkTo(image.top, margin = 4.dp)
                        end.linkTo(image.end, margin = 4.dp)
                    }
                    .clickable {
                        if (isFavoriteState) {
                            onRemoveFavorite(movie.id ?: 0)
                            isFavoriteState = false
                        } else {
                            onAddFavorite(movie)
                            isFavoriteState = true
                        }
                    }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(text = stringResource(id = R.string.query_hint), color = Color.LightGray) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = Charcoal,
                shape = RoundedCornerShape(24.dp)
            ),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 18.sp
        ),
        singleLine = true
    )
}




@Composable
fun NotFoundState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_search_blur),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(id = R.string.no_results_found),
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_search_blur),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(id = R.string.no_searched_movie_yet),
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.initial_search_message),
            fontSize = 14.sp,
            color = SoftGray,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


