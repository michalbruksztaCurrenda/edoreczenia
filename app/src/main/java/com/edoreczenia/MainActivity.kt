package com.edoreczenia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.edoreczenia.feature.auth.presentation.navigation.AuthRoutes
import com.edoreczenia.feature.auth.presentation.navigation.authNavGraph
import com.edoreczenia.feature.inbox.data.repository.FakeInboxRepository
import com.edoreczenia.feature.inbox.domain.usecase.GetInboxMessagesUseCase
import com.edoreczenia.feature.inbox.domain.usecase.ToggleStarUseCase
import com.edoreczenia.feature.inbox.presentation.InboxDetailPlaceholderScreen
import com.edoreczenia.feature.inbox.presentation.InboxScreen
import com.edoreczenia.feature.inbox.presentation.InboxViewModel
import com.edoreczenia.feature.inbox.presentation.InboxViewModelFactory
import com.edoreczenia.ui.theme.EDoreczeniaTheme

private const val INBOX_ROUTE = "inbox"
private const val INBOX_DETAIL_ROUTE = "inbox_detail/{messageId}"

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

    // Composition of dependencies for Inbox feature
    val inboxRepository = FakeInboxRepository()
    val getInboxMessagesUseCase = GetInboxMessagesUseCase(inboxRepository)
    val toggleStarUseCase = ToggleStarUseCase(inboxRepository)
    val inboxViewModelFactory = InboxViewModelFactory(getInboxMessagesUseCase, toggleStarUseCase)

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.AUTH_GRAPH
    ) {
        authNavGraph(
            navController = navController,
            onNavigateToMain = {
                navController.navigate(INBOX_ROUTE) {
                    popUpTo(AuthRoutes.AUTH_GRAPH) { inclusive = true }
                }
            }
        )

        composable(INBOX_ROUTE) {
            val inboxViewModel: InboxViewModel = viewModel(factory = inboxViewModelFactory)
            InboxScreen(
                navController = navController,
                viewModel = inboxViewModel
            )
        }

        composable(
            route = INBOX_DETAIL_ROUTE,
            arguments = listOf(navArgument("messageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getString("messageId") ?: ""
            InboxDetailPlaceholderScreen(
                messageId = messageId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

