package com.edoreczenia.core.network

import com.edoreczenia.core.session.SessionManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Wywoływany przez OkHttp przy HTTP 401 dla chronionych endpointów.
 *
 * W tej iteracji (bez Hilt i bez AuthApi w core) TokenAuthenticator bezpiecznie
 * wygasza sesję przy 401, zamiast próbować odświeżyć token.
 * Pełna implementacja odświeżania tokenu zostanie dodana po wdrożeniu AuthRepositoryImpl.
 */
class TokenAuthenticator(private val sessionManager: SessionManager) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Jeśli dostaliśmy 401, sesja jest nieważna — czyścimy lokalny stan.
        // Zwracamy null, co nakazuje OkHttp przerwać żądanie (brak ponowienia).
        sessionManager.clearSession()
        return null
    }
}

