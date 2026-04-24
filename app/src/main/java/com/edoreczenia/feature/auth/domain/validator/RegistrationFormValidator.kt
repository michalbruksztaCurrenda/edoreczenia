package com.edoreczenia.feature.auth.domain.validator

data class RegistrationFormValidationResult(
    val deviceNameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
) {
    val isValid: Boolean
        get() = deviceNameError == null &&
                usernameError == null &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null
}

class RegistrationFormValidator {

    private val emailRegex = Regex(
        "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    )

    fun validate(
        deviceName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): RegistrationFormValidationResult {
        val deviceNameError = when {
            deviceName.isBlank() -> "Pole wymagane"
            deviceName.length < 3 -> "Nazwa urządzenia musi mieć co najmniej 3 znaki"
            deviceName.length > 100 -> "Nazwa urządzenia może mieć maksymalnie 100 znaków"
            else -> null
        }

        val usernameError = when {
            username.isBlank() -> "Pole wymagane"
            username.length < 3 -> "Nazwa użytkownika musi mieć co najmniej 3 znaki"
            username.length > 50 -> "Nazwa użytkownika może mieć maksymalnie 50 znaków"
            else -> null
        }

        val emailError = when {
            email.isBlank() -> "Pole wymagane"
            !emailRegex.matches(email) -> "Podaj prawidłowy adres e-mail"
            else -> null
        }

        val passwordError = when {
            password.isBlank() -> "Pole wymagane"
            password.length < 8 -> "Hasło musi mieć co najmniej 8 znaków"
            !password.any { it.isUpperCase() } -> "Hasło musi zawierać co najmniej jedną wielką literę"
            !password.any { it.isDigit() || !it.isLetterOrDigit() } ->
                "Hasło musi zawierać co najmniej jedną cyfrę lub znak specjalny"
            else -> null
        }

        val confirmPasswordError = when {
            confirmPassword.isBlank() -> "Pole wymagane"
            confirmPassword != password -> "Hasła nie są zgodne"
            else -> null
        }

        return RegistrationFormValidationResult(
            deviceNameError = deviceNameError,
            usernameError = usernameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError
        )
    }
}

