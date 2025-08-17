package com.example.notsure.ui.screen.register


import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notsure.navigation.NavigationRoutes
import com.example.notsure.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, userViewModel: UserViewModel) {
    var fullName by remember { mutableStateOf("") }
    var emailID by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var fullNameTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    var emailExists by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    val isFullNameValid = fullName.isNotBlank()
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailID).matches()
    val isPasswordValid = password.isNotBlank()

    LaunchedEffect(fullName, emailID, password) {
        showError = null
        emailExists = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Register", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))

            // Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Enter full name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) fullNameTouched = true },
                isError =fullNameTouched && !isFullNameValid,
                supportingText = {
                    if(fullNameTouched && !isFullNameValid) {
                        Text("Full name is required", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Email
            OutlinedTextField(
                value = emailID,
                onValueChange = {
                    emailID = it
                    emailExists = false
                },
                label = { Text("Enter email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) emailTouched = true },
                isError = emailTouched && (!isEmailValid || emailExists),
                supportingText = {
                    if (emailTouched) {
                        when {
                            emailID.isBlank() -> Text("Email is required", color = MaterialTheme.colorScheme.error)
                            !isEmailValid -> Text("Enter a valid email", color = MaterialTheme.colorScheme.error)
                            emailExists -> Text("Email already exists", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) passwordTouched = true },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordTouched && !isPasswordValid,
                supportingText = {
                    if (passwordTouched && !isPasswordValid) {
                        Text("Password is required", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    fullNameTouched = true
                    emailTouched = true
                    passwordTouched = true

                    if (isFullNameValid && isEmailValid && isPasswordValid) {
                        isRegistering = true
                        userViewModel.register(
                            fullName=fullName,
                            email=emailID,
                            password=password,
                            onSuccess={
                                isRegistering = false
                                navController.navigate(NavigationRoutes.HOME) {
                                    popUpTo(NavigationRoutes.REGISTER) { inclusive = true }
                                }
                            },
                            onError = {
                                isRegistering = false
                                if (it.contains("User already exists", ignoreCase = true)) {
                                    emailExists = true
                                } else {
                                    showError = it
                                }
                            }
                        )
                    }
                },
                enabled=!isRegistering,
                modifier=Modifier.fillMaxWidth()
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(NavigationRoutes.LOGIN) }) {
                Text("Already have an account? Login")
            }

            showError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}
