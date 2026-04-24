package com.edoreczenia.feature.auth.presentation.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val usernameInput: String = "",
    val passwordInput: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null
)

sealed class LoginEffect {
    data object NavigateToMain : LoginEffect()
    data object NavigateToRegistration : LoginEffect()
    data class NavigateToVerifyEmail(val username: String) : LoginEffect()
    data class ShowMessage(val text: String) : LoginEffect()
}

