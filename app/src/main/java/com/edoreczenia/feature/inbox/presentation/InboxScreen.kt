package com.edoreczenia.feature.inbox.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.edoreczenia.R
import com.edoreczenia.feature.inbox.domain.model.InboxFilter
import com.edoreczenia.feature.inbox.presentation.components.InboxBottomNavBar
import com.edoreczenia.feature.inbox.presentation.components.InboxErrorState
import com.edoreczenia.feature.inbox.presentation.components.InboxLoadingState
import com.edoreczenia.feature.inbox.presentation.components.InboxMessageItem
import com.edoreczenia.feature.inbox.presentation.components.InboxTopAppBar

@Composable
fun InboxScreen(
    navController: NavController,
    viewModel: InboxViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is InboxEffect.NavigateToMessageDetail ->
                    navController.navigate("inbox_detail/${effect.messageId}")
                is InboxEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { InboxTopAppBar() },
        bottomBar = { InboxBottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onAction(
                        InboxAction.MessageClicked("new")
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.inbox_fab_compose)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.inbox_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                val unreadCount = (uiState as? InboxUiState.Success)?.unreadCount ?: 0
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.inbox_new_badge, unreadCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Search bar
            val searchQuery = (uiState as? InboxUiState.Success)?.searchQuery ?: ""
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onAction(InboxAction.SearchQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text(stringResource(R.string.inbox_search_placeholder)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            // Filter chips
            val activeFilter = (uiState as? InboxUiState.Success)?.activeFilter ?: InboxFilter.ALL
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                InboxFilter.entries.forEach { filter ->
                    val label = when (filter) {
                        InboxFilter.ALL -> stringResource(R.string.inbox_filter_all)
                        InboxFilter.UNREAD -> stringResource(R.string.inbox_filter_unread)
                        InboxFilter.STARRED -> stringResource(R.string.inbox_filter_starred)
                    }
                    FilterChip(
                        selected = filter == activeFilter,
                        onClick = { viewModel.onAction(InboxAction.FilterChanged(filter)) },
                        label = { Text(label) },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            HorizontalDivider()

            // Content area
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is InboxUiState.Initial, is InboxUiState.Loading -> {
                        InboxLoadingState()
                    }
                    is InboxUiState.Success -> {
                        PullToRefreshBox(
                            isRefreshing = state.isRefreshing,
                            onRefresh = { viewModel.onAction(InboxAction.Refresh) }
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(items = state.messages, key = { it.id }) { message ->
                                    InboxMessageItem(
                                        message = message,
                                        onToggleStar = { viewModel.onAction(InboxAction.ToggleStar(it)) },
                                        onClick = { viewModel.onAction(InboxAction.MessageClicked(it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                                }
                            }
                        }
                    }
                    is InboxUiState.Empty -> {
                        // Simple empty state placeholder (T022/T023 not in scope)
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (state.reason == EmptyReason.NO_MESSAGES)
                                    stringResource(R.string.inbox_empty_no_messages_title)
                                else
                                    stringResource(R.string.inbox_empty_no_results_title),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                    is InboxUiState.Error -> {
                        InboxErrorState(
                            message = state.message,
                            onRetry = { viewModel.onAction(InboxAction.RetryLoad) }
                        )
                    }
                }
            }
        }
    }
}



