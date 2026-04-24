package com.edoreczenia.feature.auth.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponseDto(
    val status: String,
    val session: SessionDto
)

