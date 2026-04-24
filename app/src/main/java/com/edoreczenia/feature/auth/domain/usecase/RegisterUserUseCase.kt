package com.edoreczenia.feature.auth.domain.usecase

import com.edoreczenia.feature.auth.domain.model.AuthResult
import com.edoreczenia.feature.auth.domain.repository.AuthRepository

class RegisterUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        deviceName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthResult<String> =
        repository.register(deviceName, username, email, password, confirmPassword)
}

