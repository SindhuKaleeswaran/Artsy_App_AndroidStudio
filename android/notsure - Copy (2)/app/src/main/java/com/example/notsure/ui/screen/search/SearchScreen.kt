package com.example.notsure.ui.screen.search

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.notsure.R
import com.example.notsure.data.model.ArtistSearchResult
import com.example.notsure.viewmodel.SearchViewModel
import com.example.notsure.viewmodel.UserViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
    userViewModel: UserViewModel
) {

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val searchResults by viewModel.searchResults.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.search(it.text)
            },
            label = { Text("Search for an artist") },
            trailingIcon = {
                if (searchQuery.text.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = TextFieldValue("")
                        viewModel.clearSearch()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
        )

        Spacer(modifier = Modifier.height(16.dp).background(MaterialTheme.colorScheme.background))

        errorMessage?.let {
            Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
        }
        val favourites by userViewModel.favourites.collectAsState()
        LazyColumn {

            items(searchResults) { artist ->
                val isFav = favourites.any { it.artistId == artist.id }

                ArtistResultCard(
                    artist = artist.copy(isFavourite = isFav),
                    isLoggedIn = isLoggedIn,
                    onClick = { navController.navigate("artistDetail/${artist.id}") },
                    onFavoriteClick = {
                        viewModel.toggleFavorite(
                            artist = artist,
                            userEmail = userViewModel.emailID,
                            onGlobalFavoriteUpdate = { artistId, isNowFav ->
                                userViewModel.updateFavoriteLocally(artistId, isNowFav)
                            }
                        )
                    }

                )
            }

        }
    }
}


@Composable
fun ArtistResultCard(
    artist: ArtistSearchResult,
    isLoggedIn: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val painter = if (artist.image.isNullOrBlank()) {
                painterResource(id = R.drawable.artsy_logo)
            } else {
                rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artist.image)
                        .crossfade(true)
                        .error(R.drawable.artsy_logo)
                        .build()
                )
            }

            Image(painter =painter, contentDescription = "Artist Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            if (isLoggedIn) {
                IconButton(
                    onClick = {
                        Log.d("ICON_CLICK", "Clicked star for ${artist.name}")
                        onFavoriteClick()
                    },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = if (artist.isFavourite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (artist.isFavourite) "Remove from Favorites" else "Add to Favorites",
                        tint = Color.Black
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(text = artist.name, color = Color.White, fontSize = 18.sp)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Go",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

