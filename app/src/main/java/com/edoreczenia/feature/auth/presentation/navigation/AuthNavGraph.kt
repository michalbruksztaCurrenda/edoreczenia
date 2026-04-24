package com.edoreczenia.feature.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.edoreczenia.feature.auth.presentation.login.LoginScreen

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
                    // TODO: nawigacja do rejestracji — US2
                    navController.navigate(AuthRoutes.REGISTRATION)
                },
                onNavigateToVerifyEmail = { username ->
                    navController.navigate(AuthRoutes.verifyEmailRoute(username)) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = false }
                    }
                }
            )
        }

        // Placeholder dla rejestracji — US2
        composable(AuthRoutes.REGISTRATION) {
            // TODO: RegistrationScreen — zaimplementowany w US2
            LoginScreen(
                onNavigateToMain = onNavigateToMain,
                onNavigateToRegistration = {},
                onNavigateToVerifyEmail = {}
            )
        }

        // Placeholder dla weryfikacji e-mail — US3
        composable(AuthRoutes.VERIFY_EMAIL) {
            // TODO: VerifyEmailScreen — zaimplementowany w US3
        }
    }
}


