package com.berkkanrencber.movieappjetpackcompose.ui.screen.homepage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberAsyncImagePainter
import com.berkkanrencber.movieappjetpackcompose.utils.formatVoteAverage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.berkkanrencber.movieappjetpackcompose.ui.MainActivity
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Charcoal
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Gray
import com.berkkanrencber.movieappjetpackcompose.ui.theme.LightBlack
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Transparent
import com.berkkanrencber.movieappjetpackcompose.utils.Constants.IMAGE_URL
import com.berkkanrencber.movieappjetpackcompose.utils.toFavoriteMovie
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen.CustomToastComposable
import com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen.NoInternetDialog
import com.berkkanrencber.movieappjetpackcompose.ui.theme.SoftGray
import com.berkkanrencber.movieappjetpackcompose.ui.theme.White
import com.berkkanrencber.movieappjetpackcompose.utils.NetworkUtil
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class HomePage : Fragment() {

    private val viewModel: HomepageViewModel by viewModels()

    private var isGridLayout = false

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

                HomepageScreen(navController = navController,viewModel = viewModel, showNoInternetDialog = showNoInternetDialog, context = context)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            val isGridLayout = it.getBoolean("isGridLayout", false)
            viewModel.setGridLayout(isGridLayout)
        }
        changeVisibilityBottomBar(true)
        viewModel.getPopularMovieList()
        viewModel.getTopRatedMovieList()
        viewModel.getUpcomingMovieList()
        viewModel.getNowPlayingMovieList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isGridLayout", viewModel.isGridLayout.value)
    }

    private fun changeVisibilityBottomBar(isVisible: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch{
            delay(100L)
            (activity as MainActivity).changeVisibilityBottomBar(isVisible)
        }
    }

}

@Composable
fun HomepageScreen(navController: NavController, viewModel: HomepageViewModel,showNoInternetDialog: Boolean, context: Context) {
    val popularMovies by viewModel.popularMovieList.collectAsState()
    val topRatedMovies by viewModel.topRatedMovieList.collectAsState()
    val nowPlayingMovies by viewModel.nowPlayingMovieList.collectAsState()
    val upcomingMovies by viewModel.upcomingMovieList.collectAsState()
    val searchMovies by viewModel.searchMovieList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGridLayout by viewModel.isGridLayout.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("POPULAR") }
    var isSearchActive by remember { mutableStateOf(false) }

    var showCustomToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastIconResId by remember { mutableStateOf(R.drawable.ic_love) }

    var showNoInternetDialog by remember { mutableStateOf(showNoInternetDialog) }

    val gridScrollState = rememberLazyGridState()
    val listScrollState = rememberLazyListState()

    var selectedCategoryMovies = remember(selectedCategory, popularMovies, topRatedMovies, upcomingMovies, nowPlayingMovies) {
        when (selectedCategory) {
            "POPULAR" -> popularMovies
            "TOP RATED" -> topRatedMovies
            "UPCOMING" -> upcomingMovies
            "LIVE" -> nowPlayingMovies
            else -> popularMovies
        }
    }

    LaunchedEffect(gridScrollState) {
        snapshotFlow { gridScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == searchMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMore()) {
                    viewModel.getSearchedMovieList(query, isNextPage = true)
                }
            }
    }

    LaunchedEffect(gridScrollState) {
        snapshotFlow { gridScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == popularMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMorePopular()) {
                    viewModel.getPopularMovieList(isNextPage = true)
                }
            }
    }

    LaunchedEffect(gridScrollState) {
        snapshotFlow { gridScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == topRatedMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMoreTopRated()) {
                    viewModel.getTopRatedMovieList(isNextPage = true)
                }
            }
    }

    LaunchedEffect(gridScrollState) {
        snapshotFlow { gridScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == upcomingMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMoreUpcoming()) {
                    viewModel.getUpcomingMovieList(isNextPage = true)
                }
            }
    }

    LaunchedEffect(gridScrollState) {
        snapshotFlow { gridScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == nowPlayingMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMoreNowPlaying()) {
                    viewModel.getNowPlayingMovieList(isNextPage = true)
                }
            }
    }

    LaunchedEffect(listScrollState) {
        snapshotFlow { listScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == searchMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMore()) {
                    viewModel.getSearchedMovieList(query, isNextPage = true)
                }
            }
    }

    LaunchedEffect(listScrollState) {
        snapshotFlow { listScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == popularMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMorePopular()) {
                    viewModel.getPopularMovieList(isNextPage = true)
                }
            }
    }

    LaunchedEffect(listScrollState) {
        snapshotFlow { listScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == topRatedMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMoreTopRated()) {
                    viewModel.getTopRatedMovieList(isNextPage = true)
                }
            }
    }

    LaunchedEffect(listScrollState) {
        snapshotFlow { listScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == upcomingMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMoreUpcoming()) {
                    viewModel.getUpcomingMovieList(isNextPage = true)
                }
            }
    }

    LaunchedEffect(listScrollState) {
        snapshotFlow { listScrollState.layoutInfo.visibleItemsInfo }
            .filter { items -> items.isNotEmpty() }
            .map { items -> items.last().index == nowPlayingMovies.size - 1 }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && viewModel.canLoadMoreNowPlaying()) {
                    viewModel.getNowPlayingMovieList(isNextPage = true)
                }
            }
    }

    if (showNoInternetDialog) {
        NoInternetDialog(
            onRetry = {
                if (!NetworkUtil.isNetworkAvailable(context)){
                    showNoInternetDialog = true
                }else{
                    viewModel.retryPopularFetchingData()
                    viewModel.retryTopRatedFetchingData()
                    viewModel.retryNowPlayingFetchingData()
                    viewModel.retryUpcomingFetchingData()
                    showNoInternetDialog = false
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlack)
    ) {
        TopBar(
            isGridLayout = isGridLayout,
            onToggleLayout = { viewModel.toggleLayout() }
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

        if (isSearchActive) {
            SearchBar(
                onCloseSearch = {
                    isSearchActive = false
                    query = ""
                },
                query = query,
                onQueryChanged = { newQuery ->
                    query = newQuery
                    if (!NetworkUtil.isNetworkAvailable(context)) {
                        viewModel.resetMovieList()
                        showNoInternetDialog = true
                    } else {
                        viewModel.getSearchedMovieList(newQuery)
                    }
                }
            )
            when {
                query.isEmpty() -> EmptyState()
                searchMovies.isNotEmpty() -> MovieList(navController, searchMovies, gridScrollState, listScrollState, isGridLayout, isLoading, viewModel,onShowCustomToast = { message, iconResId ->
                    toastMessage = message
                    toastIconResId = iconResId
                    showCustomToast = true
                })
                else -> NotFoundState()
            }
        } else {
            CategoryAndSearchBar(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                },
                onSearchClick = {
                    isSearchActive = true
                }
            )
        }

        if (selectedCategoryMovies.isNotEmpty() && !isSearchActive) {
            MovieList(navController, selectedCategoryMovies, gridScrollState, listScrollState, isGridLayout,isLoading, viewModel,onShowCustomToast = { message, iconResId ->
                toastMessage = message
                toastIconResId = iconResId
                showCustomToast = true
            })
        } else if (!isSearchActive) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}


@Composable
fun CategoryAndSearchBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBlack)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        val (searchButton, toggleGroup) = createRefs()

        SearchButton(
            onSearchClick = onSearchClick,
            modifier = Modifier.constrainAs(searchButton) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )

        ToggleButtonGroup(
            options = listOf("POPULAR", "TOP RATED", "UPCOMING", "LIVE"),
            selectedOption = selectedCategory,
            onOptionSelected = onCategorySelected,
            modifier = Modifier
                .constrainAs(toggleGroup) {
                    start.linkTo(searchButton.end, margin = 16.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
        )
    }
}

@Composable
fun ToggleButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Text(
                text = option,
                fontSize = 12.sp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color(0xFF304FFE) else Color.Transparent
                    )
                    .padding(vertical = 8.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = { onOptionSelected(option) }
                    ),
                color = if (isSelected) Color.White else Color.Black,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(onCloseSearch: () -> Unit,query: String, onQueryChanged: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBlack)
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onCloseSearch() },
            modifier = Modifier
                .size(32.dp)
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
                .background(
                    color = Charcoal,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Close Search",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

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
                .padding(8.dp)
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
                fontSize = 14.sp
            ),
            singleLine = true
        )
    }
}


@Composable
fun SearchButton(onSearchClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = { onSearchClick() },
        modifier = modifier
            .size(32.dp)
            .border(
                width = 2.dp,
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = Charcoal,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
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
    movies: List<Movie>,
    gridScrollState: LazyGridState,
    listScrollState: LazyListState,
    isGridLayout: Boolean,
    isLoading: Boolean,
    viewModel: HomepageViewModel,
    onShowCustomToast: (String, Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isGridLayout) {
            LazyVerticalGrid(
                state = gridScrollState,
                columns = GridCells.Adaptive(120.dp),
                contentPadding = PaddingValues(8.dp),
                content = {
                    itemsIndexed(movies) { index, movie ->
                        MovieGridItem(
                            movie = movie,
                            onAddFavorite = { updatedMovie ->
                                viewModel.addFavoriteMovie(updatedMovie.toFavoriteMovie())
                                onShowCustomToast("Added to favorites", R.drawable.ic_love)
                            },
                            onRemoveFavorite = { movieId ->
                                viewModel.removeFavoriteMovie(movieId)
                                onShowCustomToast("Removed from favorites", R.drawable.ic_broken_heart)
                            },
                            onClick = {
                                navController.navigate(R.id.action_homePage_to_detailPage, Bundle().apply {
                                    putString("movieId", movie.id.toString() ?: "")
                                })
                            }
                        )
                    }
                }
            )
        } else {
            LazyColumn(
                state = listScrollState,
                contentPadding = PaddingValues(8.dp),
                content = {
                    items(movies) { movie ->
                        MovieLinearItem(
                            movie = movie,
                            onAddFavorite = { updatedMovie ->
                                viewModel.addFavoriteMovie(updatedMovie.toFavoriteMovie())
                                onShowCustomToast("Added to favorites", R.drawable.ic_love)
                            },
                            onRemoveFavorite = { movieId ->
                                viewModel.removeFavoriteMovie(movieId)
                                onShowCustomToast("Removed from favorites", R.drawable.ic_broken_heart)
                            },
                            onClick = {
                                navController.navigate(R.id.action_homePage_to_detailPage, Bundle().apply {
                                    putString("movieId", movie.id.toString() ?: "")
                                })
                            }
                        )
                    }
                }
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
}




@Composable
fun MovieGridItem(
    movie: Movie,
    onAddFavorite: (Movie) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    onClick: () -> Unit
) {
    var isFavoriteState by remember { mutableStateOf(movie.isFavorite) }
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
                    .scale(scale)
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
    movie: Movie,
    onAddFavorite: (Movie) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    onClick: () -> Unit
) {
    var isFavoriteState by remember { mutableStateOf(movie.isFavorite) }
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
                    model = "$IMAGE_URL${movie.posterPath}",
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
                        text = movie.voteAverage.formatVoteAverage() ?: "-",
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
                        painter = painterResource(id = if (isFavoriteState) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale)
                            .clickable {
                                if (isFavoriteState) {
                                    onRemoveFavorite(movie.id ?: 0)
                                    isFavoriteState = false
                                } else {
                                    onAddFavorite(movie)
                                    isFavoriteState = true
                                }
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

@Preview(showBackground = true)
@Composable
fun PreviewMovieLinearItem() {
    MovieLinearItem(
        movie = Movie(
            id = 1,
            title = "Example Movie",
            overview = "This is a sample overview of the movie to showcase the layout in the preview.",
            posterPath = "/path/to/image.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 7.8,
            adult = false,
            genreIds = emptyList(),
            isFavorite = false,
            originalTitle = null,
            popularity = null,
            video = null,
            voteCount = null
        ),
        onAddFavorite = {  },
        onRemoveFavorite = { },
        onClick = {  }
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


