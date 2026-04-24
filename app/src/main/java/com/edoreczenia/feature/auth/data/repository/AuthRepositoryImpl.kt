package com.edoreczenia.feature.auth.data.repository

import com.edoreczenia.core.error.AppError
import com.edoreczenia.feature.auth.data.api.AuthApi
import com.edoreczenia.feature.auth.data.dto.request.LoginRequestDto
import com.edoreczenia.feature.auth.data.dto.request.RefreshTokenRequestDto
import com.edoreczenia.feature.auth.data.dto.response.ErrorResponseDto
import com.edoreczenia.feature.auth.data.mapper.AuthMapper
import com.edoreczenia.feature.auth.domain.model.AuthResult
import com.edoreczenia.feature.auth.domain.model.Session
import com.edoreczenia.feature.auth.domain.model.User
import com.edoreczenia.feature.auth.domain.repository.AuthRepository
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementacja [AuthRepository] korzystająca z Retrofit + kotlinx-serialization.
 * Na tym etapie implementuje wyłącznie: [login], [refreshSession], [checkSession].
 * Pozostałe metody (register, verifyEmailCode, resendVerificationCode, logout)
 * zostaną zaimplementowane w kolejnych user stories.
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val mapper: AuthMapper = AuthMapper,
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }
) : AuthRepository {

    override suspend fun login(username: String, password: String): AuthResult<Pair<User, Session>> =
        safeApiCall {
            val response = authApi.login(LoginRequestDto(username = username, password = password))
            mapper.mapLoginResponse(response)
        }

    override suspend fun refreshSession(refreshToken: String): AuthResult<Session> =
        safeApiCall {
            val response = authApi.refreshToken(RefreshTokenRequestDto(refreshToken = refreshToken))
            mapper.mapRefreshTokenResponse(response)
        }

    override suspend fun checkSession(): AuthResult<User> =
        safeApiCall {
            val response = authApi.checkSession()
            mapper.mapSessionCheckResponse(response)
        }

    // ---- Niezaimplementowane (US2–US4) ----

    override suspend fun register(
        deviceName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthResult<String> = AuthResult.Failure(AppError.Unknown())

    override suspend fun verifyEmailCode(
        username: String,
        verificationCode: String
    ): AuthResult<Unit> = AuthResult.Failure(AppError.Unknown())

    override suspend fun resendVerificationCode(username: String): AuthResult<Unit> =
        AuthResult.Failure(AppError.Unknown())

    override suspend fun logout(refreshToken: String): AuthResult<Unit> =
        AuthResult.Failure(AppError.Unknown())

    // ---- Pomocnicze ----

    private suspend fun <T> safeApiCall(block: suspend () -> T): AuthResult<T> = try {
        AuthResult.Success(block())
    } catch (e: HttpException) {
        val error = parseHttpError(e)
        AuthResult.Failure(error)
    } catch (e: IOException) {
        AuthResult.Failure(AppError.Network(e.message ?: "Błąd połączenia z siecią"))
    } catch (e: Exception) {
        AuthResult.Failure(AppError.Unknown(e))
    }

    private fun parseHttpError(e: HttpException): AppError {
        return try {
            val errorBody = e.response()?.errorBody()?.string() ?: return AppError.Unknown(e)
            val dto = json.decodeFromString(ErrorResponseDto.serializer(), errorBody)
            mapper.mapErrorResponse(dto)
        } catch (_: Exception) {
            AppError.Unknown(e)
        }
    }
}

