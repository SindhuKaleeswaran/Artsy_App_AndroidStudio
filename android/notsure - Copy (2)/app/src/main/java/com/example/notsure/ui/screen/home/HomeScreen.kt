package com.example.notsure.ui.screen.home

//Start from fav on homescreen, and everything related to favs

import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import coil.compose.AsyncImage
import com.example.notsure.navigation.NavigationRoutes
import com.example.notsure.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.ImageRequest
import com.example.notsure.R
import com.example.notsure.data.remote.RetrofitArtsyInstance
import com.example.notsure.data.remote.RetrofitInstance
import com.example.notsure.ui.components.FavoritesSection
import com.example.notsure.viewmodel.FavoritesViewModel
import com.example.notsure.viewmodel.FavoritesViewModelFactory
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, userViewModel: UserViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isSnackShown = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val justLoggedOut by userViewModel.justLoggedOut.collectAsState()


    val user = userViewModel.user.collectAsState().value
    val isLoggedIn = userViewModel.isLoggedIn.collectAsState().value
    var showMenu by remember { mutableStateOf(false) }

    val favoritesViewModel:FavoritesViewModel? = if (isLoggedIn && user != null) {
        viewModel(
            factory =FavoritesViewModelFactory(
                apiService =RetrofitInstance.api,
                emailID =user.username
            )
        )
    } else null

    LaunchedEffect(key1 =isLoggedIn,key2 = user?.username) {
        if (isLoggedIn && user != null) {
            favoritesViewModel?.loadFavorites()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Artist Search") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(NavigationRoutes.SEARCH)
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    }

                    if (isLoggedIn && user != null) {
                        val safeProfileUrl = if (user.prfpic.startsWith("//")) {
                            "https:${user.prfpic}"
                        } else user.prfpic

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                AsyncImage(
                                    model =safeProfileUrl,
                                    contentDescription = "User Avatar",
                                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.background)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Log Out") },
                                    onClick = {
                                        showMenu = false
                                        userViewModel.logout()
                                        navController.navigate(NavigationRoutes.HOME)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Account") },
                                    onClick = {
                                        showMenu = false
                                        userViewModel.deleteUser(user.username) { success ->
                                            if (success) {
                                                userViewModel.logout()
                                                navController.navigate(NavigationRoutes.HOME)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = {
                            navController.navigate(NavigationRoutes.LOGIN)
                        }) {
                            Icon(Icons.Default.Person, contentDescription = "Login")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        val justLoggedIn by userViewModel.justLoggedIn.collectAsState()

        LaunchedEffect(justLoggedIn) {
            if (justLoggedIn && !isSnackShown.value) {
                snackbarHostState.showSnackbar("Logged in successfully")
                isSnackShown.value = true
                userViewModel.markLoginSnackbarShown()
            }
        }

        LaunchedEffect(justLoggedOut) {
            if (justLoggedOut) {
                snackbarHostState.showSnackbar("Logged out successfully")
                isSnackShown.value = true
                userViewModel.markLogoutToastShown()
            }
        }

        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(currentDate, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp).background(MaterialTheme.colorScheme.background))

            Text("Favorites", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp).background(MaterialTheme.colorScheme.background))

            if (!isLoggedIn) {
                Button(
                    onClick = { navController.navigate(NavigationRoutes.LOGIN) },
                    modifier = Modifier.align(Alignment.CenterHorizontally).background(MaterialTheme.colorScheme.background)
                ) {
                    Text("Log in to see favorites")
                }

                Spacer(modifier = Modifier.height(12.dp).background(MaterialTheme.colorScheme.background))
                val context = LocalContext.current

                Text(
                    text = "Powered by Artsy",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.artsy.net/"))
                            context.startActivity(intent)
                        },
                    style= MaterialTheme.typography.labelSmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

            } else {
                favoritesViewModel?.let{ favVM ->
                    FavoritesSection(
                        isLoggedIn =true,
                        viewModel =favVM,
                        onArtistClick= { artistId ->
                            navController.navigate("artistDetail/$artistId")
                        },
                        onLoginClick = {
                            navController.navigate(NavigationRoutes.LOGIN)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp).background(MaterialTheme.colorScheme.background))
                    val context = LocalContext.current

                    Text(
                        text = "Powered by Artsy",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .background(MaterialTheme.colorScheme.background)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.artsy.net/"))
                                context.startActivity(intent)
                            },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                }
            }
        }
    }
}
