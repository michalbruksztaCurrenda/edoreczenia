package com.edoreczenia.feature.auth.data.mapper

import com.edoreczenia.core.error.AppError
import com.edoreczenia.feature.auth.data.dto.response.ErrorResponseDto
import com.edoreczenia.feature.auth.data.dto.response.LoginResponseDto
import com.edoreczenia.feature.auth.data.dto.response.RefreshTokenResponseDto
import com.edoreczenia.feature.auth.data.dto.response.SessionCheckResponseDto
import com.edoreczenia.feature.auth.data.dto.response.SessionDto
import com.edoreczenia.feature.auth.data.dto.response.UserDto
import com.edoreczenia.feature.auth.domain.model.AccountStatus
import com.edoreczenia.feature.auth.domain.model.Session
import com.edoreczenia.feature.auth.domain.model.User

/**
 * Mapper odpowiedzialny za konwersję DTO warstwy data na modele domenowe
 * oraz mapowanie błędów API na [AppError].
 */
object AuthMapper {

    fun mapUserDto(dto: UserDto): User = User(
        id = dto.id,
        username = dto.username,
        email = dto.email,
        accountStatus = mapAccountStatus(dto.accountStatus)
    )

    fun mapSessionDto(dto: SessionDto): Session = Session(
        accessToken = dto.accessToken,
        refreshToken = dto.refreshToken,
        tokenType = dto.tokenType,
        expiresInSeconds = dto.expiresInSeconds,
        refreshExpiresInSeconds = dto.refreshExpiresInSeconds
    )

    fun mapLoginResponse(dto: LoginResponseDto): Pair<User, Session> =
        Pair(mapUserDto(dto.user), mapSessionDto(dto.session))

    fun mapRefreshTokenResponse(dto: RefreshTokenResponseDto): Session =
        mapSessionDto(dto.session)

    fun mapSessionCheckResponse(dto: SessionCheckResponseDto): User =
        mapUserDto(dto.user)

    /**
     * Mapuje [ErrorResponseDto] na odpowiedni [AppError] zgodnie z kodem domenowym.
     *
     * Tabela mapowania HTTP code → AppError:
     * - AUTH_ERROR + INVALID_CREDENTIALS           → Unauthorized
     * - AUTH_ERROR + ACCOUNT_NOT_ACTIVE             → Forbidden
     * - AUTH_ERROR + ACCOUNT_LOCKED                 → Business
     * - AUTH_ERROR + TOO_MANY_LOGIN_ATTEMPTS        → Business
     * - AUTH_ERROR + INVALID_REFRESH_TOKEN          → SessionExpired
     * - AUTH_ERROR + REFRESH_TOKEN_REVOKED          → SessionExpired
     * - VALIDATION_ERROR                            → Validation
     * - BUSINESS_ERROR                              → Business
     */
    fun mapErrorResponse(dto: ErrorResponseDto): AppError = when {
        dto.code == "INVALID_CREDENTIALS" ->
            AppError.Unauthorized(code = dto.code, message = dto.message)

        dto.code == "ACCOUNT_NOT_ACTIVE" ->
            AppError.Forbidden(code = dto.code, message = dto.message)

        dto.code == "INVALID_REFRESH_TOKEN" || dto.code == "REFRESH_TOKEN_REVOKED" ->
            AppError.SessionExpired

        dto.status == "VALIDATION_ERROR" ->
            AppError.Validation(
                fieldErrors = dto.fieldErrors ?: emptyMap(),
                message = dto.message
            )

        dto.status == "BUSINESS_ERROR" ->
            AppError.Business(code = dto.code, message = dto.message)

        dto.status == "AUTH_ERROR" ->
            AppError.Unauthorized(code = dto.code, message = dto.message)

        else ->
            AppError.Unknown()
    }

    private fun mapAccountStatus(raw: String): AccountStatus =
        when (raw.uppercase()) {
            "PENDING_VERIFICATION" -> AccountStatus.PENDING_VERIFICATION
            "ACTIVE"               -> AccountStatus.ACTIVE
            "LOCKED"               -> AccountStatus.LOCKED
            "DISABLED"             -> AccountStatus.DISABLED
            else                   -> AccountStatus.ACTIVE
        }
}

