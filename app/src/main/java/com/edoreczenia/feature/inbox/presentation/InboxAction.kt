package com.edoreczenia.feature.inbox.presentation

import com.edoreczenia.feature.inbox.domain.model.InboxFilter

sealed class InboxAction {
    data class FilterChanged(val filter: InboxFilter) : InboxAction()
    data class SearchQueryChanged(val query: String) : InboxAction()
    data class ToggleStar(val messageId: String) : InboxAction()
    data class MessageClicked(val messageId: String) : InboxAction()
    data object Refresh : InboxAction()
    data object RetryLoad : InboxAction()
}

