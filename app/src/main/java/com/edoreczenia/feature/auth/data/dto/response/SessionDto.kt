package com.edoreczenia.feature.auth.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Int,
    val refreshExpiresInSeconds: Int
)


