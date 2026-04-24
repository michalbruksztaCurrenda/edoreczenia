package com.edoreczenia.feature.inbox.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edoreczenia.feature.inbox.domain.model.InboxFilter
import com.edoreczenia.feature.inbox.domain.usecase.GetInboxMessagesUseCase
import com.edoreczenia.feature.inbox.domain.usecase.ToggleStarUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModel(
    private val getInboxMessagesUseCase: GetInboxMessagesUseCase,
    private val toggleStarUseCase: ToggleStarUseCase
) : ViewModel() {

    private val _activeFilter = MutableStateFlow(InboxFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Initial)
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    private val _effects = Channel<InboxEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        _uiState.value = InboxUiState.Loading
        viewModelScope.launch {
            combine(_activeFilter, _searchQuery) { filter, query -> filter to query }
                .flatMapLatest { (filter, query) ->
                    getInboxMessagesUseCase(filter, query)
                }
                .catch { e ->
                    _uiState.value = InboxUiState.Error(e.message ?: "Nieznany błąd")
                }
                .collectLatest { messages ->
                    val filter = _activeFilter.value
                    val query = _searchQuery.value
                    val isRefreshing = _isRefreshing.value
                    if (messages.isEmpty()) {
                        val reason = if (filter == InboxFilter.ALL && query.isBlank()) {
                            EmptyReason.NO_MESSAGES
                        } else {
                            EmptyReason.NO_RESULTS
                        }
                        _uiState.value = InboxUiState.Empty(reason)
                    } else {
                        val unreadCount = messages.count { !it.isRead }
                        _uiState.value = InboxUiState.Success(
                            messages = messages,
                            unreadCount = unreadCount,
                            activeFilter = filter,
                            searchQuery = query,
                            isRefreshing = isRefreshing
                        )
                    }
                }
        }
    }

    fun onAction(action: InboxAction) {
        when (action) {
            is InboxAction.FilterChanged -> _activeFilter.value = action.filter
            is InboxAction.SearchQueryChanged -> _searchQuery.value = action.query
            is InboxAction.ToggleStar -> viewModelScope.launch {
                toggleStarUseCase(action.messageId)
            }
            is InboxAction.MessageClicked -> viewModelScope.launch {
                _effects.send(InboxEffect.NavigateToMessageDetail(action.messageId))
            }
            is InboxAction.Refresh -> viewModelScope.launch {
                _isRefreshing.value = true
                val current = _uiState.value
                if (current is InboxUiState.Success) {
                    _uiState.value = current.copy(isRefreshing = true)
                }
                _isRefreshing.value = false
                val updated = _uiState.value
                if (updated is InboxUiState.Success) {
                    _uiState.value = updated.copy(isRefreshing = false)
                }
            }
            is InboxAction.RetryLoad -> {
                _uiState.value = InboxUiState.Loading
                viewModelScope.launch {
                    combine(_activeFilter, _searchQuery) { filter, query -> filter to query }
                        .flatMapLatest { (filter, query) -> getInboxMessagesUseCase(filter, query) }
                        .catch { e -> _uiState.value = InboxUiState.Error(e.message ?: "Błąd") }
                        .collectLatest { messages ->
                            val filter = _activeFilter.value
                            val query = _searchQuery.value
                            if (messages.isEmpty()) {
                                _uiState.value = InboxUiState.Empty(EmptyReason.NO_MESSAGES)
                            } else {
                                _uiState.value = InboxUiState.Success(
                                    messages = messages,
                                    unreadCount = messages.count { !it.isRead },
                                    activeFilter = filter,
                                    searchQuery = query
                                )
                            }
                        }
                }
            }
        }
    }
}



