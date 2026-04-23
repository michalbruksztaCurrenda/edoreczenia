package com.edoreczenia.core.session

import com.edoreczenia.feature.auth.domain.model.AuthSessionState
import com.edoreczenia.feature.auth.domain.model.Session
import com.edoreczenia.feature.auth.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Zarządzanie sesją wyłącznie in-memory.
 * Nie persystuje żadnych tokenów. Sesja ginie przy restarcie procesu.
 */
class SessionManager {

    private val _sessionState = MutableStateFlow<AuthSessionState>(AuthSessionState.Unauthenticated)
    val sessionState: StateFlow<AuthSessionState> = _sessionState.asStateFlow()

    @Volatile
    private var currentSession: Session? = null

    fun setSession(user: User, session: Session) {
        currentSession = session
        _sessionState.value = AuthSessionState.Authenticated(user, session)
    }

    fun clearSession() {
        currentSession = null
        _sessionState.value = AuthSessionState.Unauthenticated
    }

    fun getAccessToken(): String? = currentSession?.accessToken

    fun getRefreshToken(): String? = currentSession?.refreshToken

    companion object {
        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(): SessionManager =
            instance ?: synchronized(this) {
                instance ?: SessionManager().also { instance = it }
            }
    }
}

