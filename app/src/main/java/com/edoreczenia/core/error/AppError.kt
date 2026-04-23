package com.edoreczenia.core.error

sealed class AppError {
    data class Network(val message: String) : AppError()
    data class Unauthorized(val code: String, val message: String) : AppError()
    data class Forbidden(val code: String, val message: String) : AppError()
    data object SessionExpired : AppError()
    data class Validation(
        val fieldErrors: Map<String, List<String>>,
        val message: String
    ) : AppError()
    data class Business(val code: String, val message: String) : AppError()
    data class Unknown(val throwable: Throwable? = null) : AppError()
}

