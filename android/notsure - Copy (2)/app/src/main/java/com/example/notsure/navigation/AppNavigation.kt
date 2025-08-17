package com.example.notsure.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.notsure.ui.screen.splash.SplashScreen
import com.example.notsure.ui.screen.home.HomeScreen
import com.example.notsure.ui.screen.search.SearchScreen
import com.example.notsure.viewmodel.SearchViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notsure.data.remote.RetrofitInstance
import com.example.notsure.ui.screen.details.ArtistDetailScreen
import com.example.notsure.ui.screen.login.LoginScreen
import com.example.notsure.ui.screen.register.RegisterScreen
import com.example.notsure.viewmodel.ArtistDetailViewModel
import com.example.notsure.viewmodel.ArtistDetailViewModelFactory
import com.example.notsure.viewmodel.SearchViewModelFactory
import com.example.notsure.viewmodel.UserViewModel


@Composable
fun AppNavigation(navController: NavHostController) {
    val userViewModel: UserViewModel = viewModel()
    val user = userViewModel.user.collectAsState().value
    val isLoggedIn = userViewModel.isLoggedIn.collectAsState().value

    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(
            apiService = RetrofitInstance.api,
            userEmail = user?.username,
        )
    )

    NavHost(navController = navController, startDestination = NavigationRoutes.SPLASH) {
        composable(NavigationRoutes.SPLASH) {
            SplashScreen(navController)
        }
        composable(NavigationRoutes.HOME) {
            HomeScreen(navController, userViewModel)
        }
        composable(NavigationRoutes.SEARCH) {
            SearchScreen(navController, searchViewModel, userViewModel)
        }
        composable("artistDetail/{artistId}") { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            val detailViewModel: ArtistDetailViewModel = viewModel(
                factory = ArtistDetailViewModelFactory(apiService = RetrofitInstance.api, userEmail = user?.username)
            )

            ArtistDetailScreen(artistId = artistId, navController = navController, viewModel = detailViewModel, userViewModel = userViewModel)
        }


        composable(NavigationRoutes.LOGIN){ LoginScreen(navController, userViewModel)
        }
        composable(NavigationRoutes.REGISTER) {RegisterScreen(navController, userViewModel)}
    }
}
