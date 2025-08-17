package com.example.notsure.ui.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.notsure.R
import com.example.notsure.viewmodel.UserViewModel

@Composable
fun SplashScreen(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    val context = LocalContext.current
    var isChecking by remember{mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1L)
        userViewModel.restoreSession { isRestored ->
            isChecking = false
            navController.navigate(
                if (isRestored) "home" else "home"
            ){popUpTo("splash"){inclusive=true}}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.artsy_logo),
            contentDescription = "Splash Logo",
            modifier = Modifier.size(150.dp)
        )

    }
}

