package com.example.notsure.ui.screen.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.notsure.R
import com.example.notsure.data.model.Category
import com.example.notsure.data.model.SimilarArtist
import com.example.notsure.viewmodel.ArtistDetailViewModel
import com.example.notsure.viewmodel.UserViewModel
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.pager.ExperimentalPagerApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    navController: NavController,
    viewModel: ArtistDetailViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val artist by viewModel.artist.collectAsState()
    val artworks by viewModel.artworks.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showDialogForArtwork by remember { mutableStateOf<String?>(null) }
    val isLoggedIn = userViewModel.isLoggedIn.collectAsState().value

    LaunchedEffect(artistId) {
        viewModel.load(artistId)
    }

    val tabItems = remember(isLoggedIn) {
        mutableListOf(Pair("Details",Icons.Default.Info), Pair("Artworks",Icons.Default.AccountBox)
        ).apply {
            if (isLoggedIn) add(Pair("Similar",Icons.Default.PersonSearch))
        }
    }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title={Text(text = artist?.artistName ?: "Artist Details")},
            navigationIcon={IconButton(onClick={ navController.popBackStack()}){Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
            actions={
                if(isLoggedIn&&artist != null) {
                    IconButton(onClick={viewModel.toggleFavoriteFromDetails()}) {
                        Icon(imageVector = if (artist!!.isFavourite) Icons.Filled.Star else Icons.Outlined.StarBorder, contentDescription = "Favorite", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabItems.forEachIndexed { index, (title, icon) ->
                Tab(selected = selectedTab == index,onClick = { selectedTab = index },text = { Text(title) },icon = { Icon(imageVector = icon, contentDescription = title) })
            }
        }

        when (selectedTab) {
            0 -> {
                if (artist != null) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(scrollState).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text =artist!!.artistName,style= MaterialTheme.typography.headlineMedium, color= MaterialTheme.colorScheme.onBackground, modifier= Modifier.align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text =listOfNotNull(artist!!.nationality, artist!!.birthday, artist!!.deathday).joinToString(" · "),
                            style= MaterialTheme.typography.titleMedium,
                            color= MaterialTheme.colorScheme.onBackground,
                            modifier =Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier =Modifier.height(20.dp))
                        Text(
                            text =artist!!.biography ?: "Biography not available.",
                            style= MaterialTheme.typography.bodyLarge,
                            color =MaterialTheme.colorScheme.onBackground,
                            modifier =Modifier.fillMaxWidth(),
                            lineHeight= MaterialTheme.typography.bodyLarge.lineHeight,
                            textAlign=TextAlign.Justify
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            1 -> if (artworks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 32.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        Text(
                            text = "No Artworks",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    items(artworks) { artwork ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(artwork.artworkImage),
                                    contentDescription = artwork.artworkTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(420.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = artwork.artworkTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(horizontal = 12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { showDialogForArtwork = artwork.artworkID },
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .defaultMinSize(minHeight = 36.dp)
                                        .padding(bottom = 12.dp)
                                ) {
                                    Text("View categories")
                                }
                            }
                        }
                    }
                }
                showDialogForArtwork?.let { artworkId ->
                    viewModel.loadCategories(artworkId)
                    AlertDialog(
                        onDismissRequest = { showDialogForArtwork = null },
                        title = { Text("Categories") },
                        confirmButton = {
                            Button(
                                onClick = { showDialogForArtwork = null },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Close", color = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        text = {
                            val isLoading by viewModel.isLoadingCategories.collectAsState()
                            val categories by viewModel.categories.collectAsState()
                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else if (categories.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No categories available",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                CategoryCarousel(categories)
                            }
                        }
                    )
                }
            }

            2 -> {
                val similarList = artist?.similarArtists
                if (isLoggedIn && !similarList.isNullOrEmpty()) {
                    SimilarArtistsTabContent(
                        similarArtists = similarList,
                        navController = navController,
                        isLoggedIn = isLoggedIn,
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCarousel(categories: List<Category>) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { categories.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp, max = 460.dp)
            .padding(horizontal = 12.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val category = categories[page]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp), // wider to make space for arrows
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                category.categoryThumbnail?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = category.categoryName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = category.categoryDescription,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }

        IconButton(
            onClick = {
                coroutineScope.launch {
                    if (pagerState.currentPage > 0)
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            enabled = pagerState.currentPage > 0,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
        ) {
            Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = "Previous")
        }

        IconButton(
            onClick = {
                coroutineScope.launch {
                    if (pagerState.currentPage < categories.lastIndex)
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            enabled = pagerState.currentPage < categories.lastIndex,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
        ) {
            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}


@Composable
fun SimilarArtistsTabContent(
    similarArtists: List<SimilarArtist>,
    navController: NavController,
    isLoggedIn: Boolean,
    onToggleFavorite: (SimilarArtist) -> Unit
) {
    if (similarArtists.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No similar artists found.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(similarArtists) { artist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { navController.navigate("artistDetail/${artist.id}") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = artist.thumbnail,
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            error = painterResource(R.drawable.artsy_logo)
                        )
                        if (isLoggedIn) {
                            IconButton(
                                onClick = { onToggleFavorite(artist) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = if (artist.isFavourite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = artist.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Go",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}
