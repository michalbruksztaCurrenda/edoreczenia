package com.edoreczenia.feature.auth.domain.model

import com.edoreczenia.core.error.AppError

sealed class AuthSessionState {
    data object Unauthenticated : AuthSessionState()
    data object Authenticating : AuthSessionState()
    data class Authenticated(val user: User, val session: Session) : AuthSessionState()
    data object SessionExpired : AuthSessionState()
    data object LoggingOut : AuthSessionState()
    data class Error(val error: AppError) : AuthSessionState()
}

