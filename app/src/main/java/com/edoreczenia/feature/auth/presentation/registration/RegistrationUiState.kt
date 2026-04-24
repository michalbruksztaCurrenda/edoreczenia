package com.edoreczenia.feature.auth.presentation.registration

data class RegistrationUiState(
    val isLoading: Boolean = false,
    val deviceNameInput: String = "",
    val usernameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val deviceNameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val formError: String? = null
)

sealed class RegistrationEffect {
    data class NavigateToVerifyEmail(val username: String, val email: String) : RegistrationEffect()
    data object NavigateToLogin : RegistrationEffect()
    data class ShowMessage(val text: String) : RegistrationEffect()
}

