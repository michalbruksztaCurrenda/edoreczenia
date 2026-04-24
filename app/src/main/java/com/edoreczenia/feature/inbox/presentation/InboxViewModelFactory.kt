package com.edoreczenia.feature.inbox.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.edoreczenia.feature.inbox.domain.usecase.GetInboxMessagesUseCase
import com.edoreczenia.feature.inbox.domain.usecase.ToggleStarUseCase

class InboxViewModelFactory(
    private val getInboxMessagesUseCase: GetInboxMessagesUseCase,
    private val toggleStarUseCase: ToggleStarUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InboxViewModel::class.java)) {
            return InboxViewModel(getInboxMessagesUseCase, toggleStarUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

