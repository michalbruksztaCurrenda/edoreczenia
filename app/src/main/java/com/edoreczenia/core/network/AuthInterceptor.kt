package com.edoreczenia.core.network

import com.edoreczenia.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Dodaje nagłówek Authorization: Bearer <token> do każdego żądania, jeśli token jest dostępny.
 * Brak tokenu nie blokuje żądania — publiczne endpointy przejdą bez nagłówka.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = sessionManager.getAccessToken()

        val request = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}

