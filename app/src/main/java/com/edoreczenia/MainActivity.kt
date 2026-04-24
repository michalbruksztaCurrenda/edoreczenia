package com.edoreczenia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edoreczenia.feature.auth.presentation.navigation.AuthRoutes
import com.edoreczenia.feature.auth.presentation.navigation.authNavGraph
import com.edoreczenia.ui.theme.EDoreczeniaTheme

private const val MAIN_ROUTE = "main"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EDoreczeniaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.AUTH_GRAPH
    ) {
        authNavGraph(
            navController = navController,
            onNavigateToMain = {
                navController.navigate(MAIN_ROUTE) {
                    popUpTo(AuthRoutes.AUTH_GRAPH) { inclusive = true }
                }
            }
        )

        // Placeholder głównego ekranu aplikacji (po zalogowaniu)
        composable(MAIN_ROUTE) {
            // TODO: MainScreen — poza zakresem US1
        }
    }
}