package com.edoreczenia.feature.auth.domain.model

import com.edoreczenia.core.error.AppError

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Failure(val error: AppError) : AuthResult<Nothing>()
}

