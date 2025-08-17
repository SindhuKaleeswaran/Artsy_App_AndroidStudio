package com.example.notsure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.notsure.ui.theme.NotsureTheme
import androidx.navigation.compose.rememberNavController
import com.example.notsure.data.remote.CookieHandler
import com.example.notsure.data.remote.RetrofitInstance
import com.example.notsure.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CookieHandler.initialize(applicationContext)
        RetrofitInstance.initialize("http://192.168.1.165:3000/")

        setContent{
            NotsureTheme{
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    NotsureTheme {
//        Greeting("Android")
//    }
//}