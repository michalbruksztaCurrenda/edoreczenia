package com.edoreczenia.feature.inbox.presentation

sealed class InboxEffect {
    data class NavigateToMessageDetail(val messageId: String) : InboxEffect()
    data class ShowToast(val message: String) : InboxEffect()
}

