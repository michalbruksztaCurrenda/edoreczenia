package com.edoreczenia.feature.inbox.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.edoreczenia.feature.inbox.data.repository.FakeInboxRepository
import com.edoreczenia.feature.inbox.domain.usecase.GetInboxMessagesUseCase
import com.edoreczenia.feature.inbox.domain.usecase.ToggleStarUseCase
import com.edoreczenia.feature.inbox.presentation.InboxDetailPlaceholderScreen
import com.edoreczenia.feature.inbox.presentation.InboxScreen
import com.edoreczenia.feature.inbox.presentation.InboxViewModel
import com.edoreczenia.feature.inbox.presentation.InboxViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

object InboxRoutes {
    const val INBOX_GRAPH = "inbox_graph"
    const val INBOX = "inbox"
    const val INBOX_DETAIL = "inbox_detail/{messageId}"
    fun inboxDetailRoute(messageId: String) = "inbox_detail/$messageId"
}

fun NavGraphBuilder.inboxNavGraph(navController: NavController) {
    navigation(
        startDestination = InboxRoutes.INBOX,
        route = InboxRoutes.INBOX_GRAPH
    ) {
        composable(InboxRoutes.INBOX) {
            val factory = InboxViewModelFactory(
                getInboxMessagesUseCase = GetInboxMessagesUseCase(FakeInboxRepository()),
                toggleStarUseCase = ToggleStarUseCase(FakeInboxRepository())
            )
            val viewModel: InboxViewModel = viewModel(factory = factory)
            InboxScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = InboxRoutes.INBOX_DETAIL,
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

