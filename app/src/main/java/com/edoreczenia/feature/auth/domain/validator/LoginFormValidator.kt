package com.edoreczenia.feature.auth.domain.validator

data class LoginFormValidationResult(
    val usernameError: String? = null,
    val passwordError: String? = null
) {
    val isValid: Boolean get() = usernameError == null && passwordError == null
}

class LoginFormValidator {
    fun validate(username: String, password: String): LoginFormValidationResult {
        val usernameError = if (username.isBlank()) "Pole wymagane" else null
        val passwordError = if (password.isBlank()) "Pole wymagane" else null
        return LoginFormValidationResult(usernameError, passwordError)
    }
}

