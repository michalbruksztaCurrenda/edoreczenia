package com.edoreczenia.feature.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.edoreczenia.feature.auth.presentation.login.LoginScreen
import com.edoreczenia.feature.auth.presentation.registration.RegistrationScreen

object AuthRoutes {
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN = "login"
    const val REGISTRATION = "registration"
    const val VERIFY_EMAIL = "verify_email/{username}"
    fun verifyEmailRoute(username: String) = "verify_email/$username"
}

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onNavigateToMain: () -> Unit
) {
    navigation(
        startDestination = AuthRoutes.LOGIN,
        route = AuthRoutes.AUTH_GRAPH
    ) {
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateToMain = onNavigateToMain,
                onNavigateToRegistration = {
                    navController.navigate(AuthRoutes.REGISTRATION)
                },
                onNavigateToVerifyEmail = { username ->
                    navController.navigate(AuthRoutes.verifyEmailRoute(username)) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = false }
                    }
                }
            )
        }

        composable(AuthRoutes.REGISTRATION) {
            RegistrationScreen(
                onNavigateToVerifyEmail = { username, _ ->
                    navController.navigate(AuthRoutes.verifyEmailRoute(username)) {
                        popUpTo(AuthRoutes.REGISTRATION) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.AUTH_GRAPH) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Placeholder dla weryfikacji e-mail — US3
        composable(
            route = AuthRoutes.VERIFY_EMAIL,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) {
            // TODO: VerifyEmailScreen — zaimplementowany w US3
        }
    }
}


