package com.example.notsure.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notsure.data.model.FavoriteArtist
import com.example.notsure.viewmodel.FavoritesUiState
import com.example.notsure.viewmodel.FavoritesViewModel
@Composable
fun FavoritesSection(
    isLoggedIn: Boolean,
    viewModel: FavoritesViewModel?,
    onArtistClick: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        when {
            !isLoggedIn -> {
                Button(onClick = onLoginClick, modifier = Modifier.align(Alignment.CenterHorizontally)
                ){Text("Log in to see favorites")}
                Spacer(modifier = Modifier.height(16.dp))
            }

            viewModel == null -> {
                Text("Loading...", modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
            }

            else -> {
                val uiState by viewModel.uiState.collectAsState()

                when (uiState) {
                    is FavoritesUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is FavoritesUiState.Empty -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Text("No favorites", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is FavoritesUiState.Success -> {
                        val favorites = (uiState as FavoritesUiState.Success).favorites
                        LazyColumn{items(favorites) { fav ->
                                FavoriteArtistCard(
                                    artist = fav,
                                    onClick = { onArtistClick(fav.artistId) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is FavoritesUiState.Error -> {
                        Text("Error loading favorites", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

    }
}