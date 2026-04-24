package com.edoreczenia.feature.auth.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val status: String,
    val user: UserDto,
    val session: SessionDto
)

