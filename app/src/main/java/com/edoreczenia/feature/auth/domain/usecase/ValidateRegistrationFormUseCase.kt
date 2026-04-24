package com.edoreczenia.feature.auth.domain.usecase

import com.edoreczenia.feature.auth.domain.validator.RegistrationFormValidationResult
import com.edoreczenia.feature.auth.domain.validator.RegistrationFormValidator

class ValidateRegistrationFormUseCase(
    private val validator: RegistrationFormValidator = RegistrationFormValidator()
) {
    operator fun invoke(
        deviceName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): RegistrationFormValidationResult =
        validator.validate(deviceName, username, email, password, confirmPassword)
}

