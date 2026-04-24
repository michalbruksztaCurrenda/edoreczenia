package com.edoreczenia.feature.inbox.domain.usecase

import com.edoreczenia.feature.inbox.domain.repository.InboxRepository

class ToggleStarUseCase(
    private val repository: InboxRepository
) {
    suspend operator fun invoke(messageId: String): Result<Unit> {
        return try {
            repository.toggleStar(messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


