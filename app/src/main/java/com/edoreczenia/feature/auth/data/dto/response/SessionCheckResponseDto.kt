package com.edoreczenia.feature.auth.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SessionCheckResponseDto(
    val status: String,
    val user: UserDto
)

