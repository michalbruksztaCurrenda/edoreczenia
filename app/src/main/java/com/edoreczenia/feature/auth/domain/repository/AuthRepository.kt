package com.edoreczenia.feature.auth.domain.repository

import com.edoreczenia.feature.auth.domain.model.AuthResult
import com.edoreczenia.feature.auth.domain.model.Session
import com.edoreczenia.feature.auth.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthResult<Pair<User, Session>>
    suspend fun register(
        deviceName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthResult<String>
    suspend fun verifyEmailCode(username: String, verificationCode: String): AuthResult<Unit>
    suspend fun resendVerificationCode(username: String): AuthResult<Unit>
    suspend fun logout(refreshToken: String): AuthResult<Unit>
    suspend fun refreshSession(refreshToken: String): AuthResult<Session>
    suspend fun checkSession(): AuthResult<User>
}

