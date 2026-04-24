package com.edoreczenia.feature.auth.domain.usecase

import com.edoreczenia.core.session.SessionManager
import com.edoreczenia.feature.auth.domain.model.AuthResult
import com.edoreczenia.feature.auth.domain.model.Session
import com.edoreczenia.feature.auth.domain.model.User
import com.edoreczenia.feature.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(username: String, password: String): AuthResult<Pair<User, Session>> {
        val result = authRepository.login(username, password)
        if (result is AuthResult.Success) {
            sessionManager.setSession(result.data.first, result.data.second)
        }
        return result
    }
}

