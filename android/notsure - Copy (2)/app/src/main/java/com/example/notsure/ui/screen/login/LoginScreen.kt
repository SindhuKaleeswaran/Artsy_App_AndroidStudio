package com.example.notsure.ui.screen.login

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notsure.viewmodel.UserViewModel
import com.example.notsure.navigation.NavigationRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, userViewModel: UserViewModel) {
    var emailID by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailID).matches()
    val isPasswordValid = password.isNotBlank()

    // Clear error when typing
    LaunchedEffect(emailID, password) {
        showError = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = emailID,
                onValueChange = {
                    emailID = it
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().onFocusChanged{ focusState ->
                        if (focusState.isFocused) {
                            emailTouched = true
                        }
                        if (emailTouched && !focusState.isFocused) {
                            emailTouched = true
                        }
                    },
                isError = emailTouched && (emailID.isBlank() || !isEmailValid),

                supportingText = {
                    if (emailTouched) {
                        when {
                            emailID.isBlank() -> Text("Email cannot be empty", color = MaterialTheme.colorScheme.error)
                            !isEmailValid -> Text("Enter a valid email", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )


            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            passwordTouched = true
                        }
                        if (passwordTouched && !focusState.isFocused) {
                            passwordTouched = true
                        }
                    },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordTouched && password.isBlank(),
                supportingText = {
                    if (passwordTouched && password.isBlank()) {
                        Text("Password cannot be empty", color = MaterialTheme.colorScheme.error)
                    }
                }
            )


            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    emailTouched = true
                    passwordTouched = true

                    if (isEmailValid && isPasswordValid) {
                        isLoggingIn = true
                        userViewModel.login(
                            emailID, password,
                            onSuccess = {
                                isLoggingIn = false
                                navController.navigate(NavigationRoutes.HOME) {
                                    popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                                }
                            },
                            onError = {
                                isLoggingIn = false
                                showError = "Username or password is incorrect"
                            }
                        )
                    }
                },
                enabled = !isLoggingIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate(NavigationRoutes.REGISTER)
            }) {
                Text("Don't have an account? Register")
            }

            showError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

