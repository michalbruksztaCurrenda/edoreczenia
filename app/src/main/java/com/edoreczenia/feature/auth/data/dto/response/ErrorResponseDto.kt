package com.edoreczenia.feature.auth.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val status: String,
    val code: String,
    val message: String,
    val fieldErrors: Map<String, List<String>>? = null
)

