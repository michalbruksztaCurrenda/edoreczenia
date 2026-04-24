package com.edoreczenia.feature.auth.data.repository

import com.edoreczenia.core.error.AppError
import com.edoreczenia.feature.auth.domain.model.AccountStatus
import com.edoreczenia.feature.auth.domain.model.AuthResult
import com.edoreczenia.feature.auth.domain.model.Session
import com.edoreczenia.feature.auth.domain.model.User
import com.edoreczenia.feature.auth.domain.repository.AuthRepository

/**
 * Implementacja [AuthRepository] do użycia w testach i trybie dev (bez backendu).
 *
 * Konfiguracja zachowania:
 * - [loginResult] — wynik zwracany przez [login]; domyślnie sukces z fake danymi.
 * - [refreshSessionResult] — wynik zwracany przez [refreshSession].
 * - [checkSessionResult] — wynik zwracany przez [checkSession].
 * - [shouldFailWith] — gdy ustawione, WSZYSTKIE metody zwracają [AuthResult.Failure] z tym błędem.
 *
 * Aby przełączyć na prawdziwe repozytorium, podmień instancję [AuthRepository]
 * na [AuthRepositoryImpl] w miejscu kompozycji zależności (np. ViewModel lub moduł DI).
 */
class FakeAuthRepository : AuthRepository {

    var shouldFailWith: AppError? = null

    var loginResult: AuthResult<Pair<User, Session>> = AuthResult.Success(
        Pair(fakeUser(), fakeSession())
    )

    var refreshSessionResult: AuthResult<Session> = AuthResult.Success(fakeSession())

    var checkSessionResult: AuthResult<User> = AuthResult.Success(fakeUser())

    var registerResult: AuthResult<String> = AuthResult.Success("usr_fake_002")

    override suspend fun login(username: String, password: String): AuthResult<Pair<User, Session>> =
        shouldFailWith?.let { AuthResult.Failure(it) } ?: loginResult

    override suspend fun refreshSession(refreshToken: String): AuthResult<Session> =
        shouldFailWith?.let { AuthResult.Failure(it) } ?: refreshSessionResult

    override suspend fun checkSession(): AuthResult<User> =
        shouldFailWith?.let { AuthResult.Failure(it) } ?: checkSessionResult

    override suspend fun register(
        deviceName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthResult<String> =
        shouldFailWith?.let { AuthResult.Failure(it) } ?: registerResult

    override suspend fun verifyEmailCode(
        username: String,
        verificationCode: String
    ): AuthResult<Unit> = AuthResult.Failure(AppError.Unknown())

    override suspend fun resendVerificationCode(username: String): AuthResult<Unit> =
        AuthResult.Failure(AppError.Unknown())

    override suspend fun logout(refreshToken: String): AuthResult<Unit> =
        AuthResult.Failure(AppError.Unknown())

    // ---- Dane testowe ----

    companion object {
        fun fakeUser(): User = User(
            id = "usr_fake_001",
            username = "testuser",
            email = "testuser@example.com",
            accountStatus = AccountStatus.ACTIVE
        )

        fun fakeSession(): Session = Session(
            accessToken = "fake-access-token",
            refreshToken = "fake-refresh-token",
            tokenType = "Bearer",
            expiresInSeconds = 900,
            refreshExpiresInSeconds = 28800
        )
    }
}

