package com.edoreczenia.feature.inbox.presentation

import com.edoreczenia.feature.inbox.domain.model.InboxFilter
import com.edoreczenia.feature.inbox.domain.model.InboxMessage

enum class EmptyReason { NO_MESSAGES, NO_RESULTS }

sealed class InboxUiState {
    data object Initial : InboxUiState()
    data object Loading : InboxUiState()
    data class Success(
        val messages: List<InboxMessage>,
        val unreadCount: Int,
        val activeFilter: InboxFilter,
        val searchQuery: String,
        val isRefreshing: Boolean = false
    ) : InboxUiState()
    data class Empty(val reason: EmptyReason) : InboxUiState()
    data class Error(val message: String) : InboxUiState()
}

