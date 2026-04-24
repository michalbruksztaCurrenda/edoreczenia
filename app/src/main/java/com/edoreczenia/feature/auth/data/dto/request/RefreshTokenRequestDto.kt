package com.edoreczenia.feature.auth.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String
)

