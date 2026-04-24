package com.edoreczenia.feature.auth.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.edoreczenia.core.error.AppError
import com.edoreczenia.feature.auth.data.repository.FakeAuthRepository
import com.edoreczenia.feature.auth.domain.model.AuthResult
import com.edoreczenia.feature.auth.domain.usecase.RegisterUserUseCase
import com.edoreczenia.feature.auth.domain.usecase.ValidateRegistrationFormUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
    private val validateRegistrationFormUseCase: ValidateRegistrationFormUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RegistrationEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onDeviceNameChanged(value: String) {
        _uiState.update { it.copy(deviceNameInput = value, deviceNameError = null, formError = null) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(usernameInput = value, usernameError = null, formError = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(emailInput = value, emailError = null, formError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value, passwordError = null, formError = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPasswordInput = value, confirmPasswordError = null, formError = null) }
    }

    fun onRegisterClicked() {
        val state = _uiState.value
        if (state.isLoading) return

        val validation = validateRegistrationFormUseCase(
            deviceName = state.deviceNameInput,
            username = state.usernameInput,
            email = state.emailInput,
            password = state.passwordInput,
            confirmPassword = state.confirmPasswordInput
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    deviceNameError = validation.deviceNameError,
                    usernameError = validation.usernameError,
                    emailError = validation.emailError,
                    passwordError = validation.passwordError,
                    confirmPasswordError = validation.confirmPasswordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formError = null) }
            val result = registerUserUseCase(
                deviceName = state.deviceNameInput,
                username = state.usernameInput,
                email = state.emailInput,
                password = state.passwordInput,
                confirmPassword = state.confirmPasswordInput
            )
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.send(
                        RegistrationEffect.NavigateToVerifyEmail(
                            username = state.usernameInput,
                            email = state.emailInput
                        )
                    )
                }
                is AuthResult.Failure -> {
                    val (updatedState) = mapErrorToState(result.error, state)
                    _uiState.value = updatedState.copy(isLoading = false)
                }
            }
        }
    }

    fun onBackToLoginClicked() {
        viewModelScope.launch {
            _effects.send(RegistrationEffect.NavigateToLogin)
        }
    }

    private fun mapErrorToState(
        error: AppError,
        state: RegistrationUiState
    ): Pair<RegistrationUiState, Unit> {
        val newState = when (error) {
            is AppError.Business -> when (error.code) {
                "USERNAME_ALREADY_EXISTS" ->
                    state.copy(usernameError = "Nazwa użytkownika jest już zajęta")
                "EMAIL_ALREADY_EXISTS" ->
                    state.copy(emailError = "Podany adres e-mail jest już zarejestrowany")
                else -> state.copy(formError = error.message)
            }
            is AppError.Validation -> {
                var s = state
                error.fieldErrors.forEach { (field, messages) ->
                    val msg = messages.firstOrNull()
                    s = when (field) {
                        "deviceName" -> s.copy(deviceNameError = msg)
                        "username" -> s.copy(usernameError = msg)
                        "email" -> s.copy(emailError = msg)
                        "password" -> s.copy(passwordError = msg)
                        "confirmPassword" -> s.copy(confirmPasswordError = msg)
                        else -> s
                    }
                }
                s.copy(formError = if (error.fieldErrors.isEmpty()) error.message else null)
            }
            is AppError.Network -> state.copy(formError = "Brak połączenia z siecią. Sprawdź internet i spróbuj ponownie.")
            else -> state.copy(formError = "Wystąpił nieoczekiwany błąd. Spróbuj ponownie.")
        }
        return Pair(newState, Unit)
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = FakeAuthRepository()
                val registerUseCase = RegisterUserUseCase(repository)
                val validateUseCase = ValidateRegistrationFormUseCase()
                return RegistrationViewModel(registerUseCase, validateUseCase) as T
            }
        }
    }
}

