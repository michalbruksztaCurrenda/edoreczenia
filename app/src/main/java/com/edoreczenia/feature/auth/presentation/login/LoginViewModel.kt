package com.edoreczenia.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.edoreczenia.core.error.AppError
import com.edoreczenia.core.session.SessionManager
import com.edoreczenia.feature.auth.data.repository.FakeAuthRepository
import com.edoreczenia.feature.auth.domain.usecase.LoginUseCase
import com.edoreczenia.feature.auth.domain.usecase.ValidateLoginFormUseCase
import com.edoreczenia.feature.auth.domain.model.AuthResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val validateLoginFormUseCase: ValidateLoginFormUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(usernameInput = value, usernameError = null, formError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value, passwordError = null, formError = null) }
    }

    fun onLoginClicked() {
        val state = _uiState.value
        if (state.isLoading) return

        val validation = validateLoginFormUseCase(state.usernameInput, state.passwordInput)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    usernameError = validation.usernameError,
                    passwordError = validation.passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formError = null) }
            val result = loginUseCase(state.usernameInput, state.passwordInput)
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.send(LoginEffect.NavigateToMain)
                }
                is AuthResult.Failure -> {
                    val errorMessage = mapErrorToMessage(result.error, state.usernameInput)
                    _uiState.update { it.copy(isLoading = false, formError = errorMessage) }
                }
            }
        }
    }

    fun onRegisterClicked() {
        viewModelScope.launch {
            _effects.send(LoginEffect.NavigateToRegistration)
        }
    }

    fun onRetryClicked() {
        _uiState.update { it.copy(formError = null) }
    }

    private suspend fun mapErrorToMessage(error: AppError, username: String): String? {
        return when (error) {
            is AppError.Unauthorized -> {
                if (error.code == "INVALID_CREDENTIALS") {
                    "Nieprawidłowa nazwa użytkownika lub hasło"
                } else {
                    "Błąd autoryzacji. Spróbuj ponownie."
                }
            }
            is AppError.Forbidden -> {
                if (error.code == "ACCOUNT_NOT_ACTIVE") {
                    _effects.send(LoginEffect.NavigateToVerifyEmail(username))
                    null
                } else {
                    error.message
                }
            }
            is AppError.Business -> {
                when (error.code) {
                    "ACCOUNT_LOCKED" -> "Konto zostało zablokowane. Skontaktuj się z administratorem."
                    "TOO_MANY_LOGIN_ATTEMPTS" -> "Zbyt wiele prób logowania. Spróbuj ponownie później."
                    else -> "Wystąpił błąd. Spróbuj ponownie."
                }
            }
            is AppError.Network -> "Brak połączenia z siecią. Sprawdź internet i spróbuj ponownie."
            else -> "Wystąpił nieoczekiwany błąd. Spróbuj ponownie."
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val sessionManager = SessionManager.getInstance()
                val repository = FakeAuthRepository()
                val loginUseCase = LoginUseCase(repository, sessionManager)
                val validateUseCase = ValidateLoginFormUseCase()
                return LoginViewModel(loginUseCase, validateUseCase) as T
            }
        }
    }
}

