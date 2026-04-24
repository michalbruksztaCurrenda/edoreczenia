package com.edoreczenia.feature.inbox.domain.usecase

import com.edoreczenia.feature.inbox.domain.model.InboxFilter
import com.edoreczenia.feature.inbox.domain.model.InboxMessage
import com.edoreczenia.feature.inbox.domain.repository.InboxRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetInboxMessagesUseCase(
    private val repository: InboxRepository
) {
    operator fun invoke(
        filter: InboxFilter = InboxFilter.ALL,
        searchQuery: String = ""
    ): Flow<List<InboxMessage>> {
        return repository.getMessages().map { messages ->
            val query = searchQuery.trim().lowercase()
            messages.filter { msg ->
                val matchesFilter = when (filter) {
                    InboxFilter.ALL -> true
                    InboxFilter.UNREAD -> !msg.isRead
                    InboxFilter.STARRED -> msg.isStarred
                }
                val matchesQuery = query.isEmpty() ||
                    msg.senderName.lowercase().contains(query) ||
                    msg.subject.lowercase().contains(query) ||
                    msg.caseNumber.lowercase().contains(query)
                matchesFilter && matchesQuery
            }
        }
    }
}

