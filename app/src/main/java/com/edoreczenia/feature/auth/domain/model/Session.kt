package com.edoreczenia.feature.auth.domain.model

/**
 * Dane sesji przechowywane wyłącznie in-memory.
 * Nie są serializowane ani persystowane.
 */
data class Session(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Int,
    val refreshExpiresInSeconds: Int
)

