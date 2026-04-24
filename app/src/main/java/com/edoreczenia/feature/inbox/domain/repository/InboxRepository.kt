package com.edoreczenia.feature.inbox.domain.repository

import com.edoreczenia.feature.inbox.domain.model.InboxMessage
import kotlinx.coroutines.flow.Flow

interface InboxRepository {
    fun getMessages(): Flow<List<InboxMessage>>
    suspend fun refresh(): Result<Unit>
    suspend fun toggleStar(id: String): Result<Unit>
}

