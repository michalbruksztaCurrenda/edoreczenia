package com.edoreczenia.feature.auth.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

