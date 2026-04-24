package com.edoreczenia.feature.auth.domain.usecase

import com.edoreczenia.feature.auth.domain.validator.LoginFormValidationResult
import com.edoreczenia.feature.auth.domain.validator.LoginFormValidator

class ValidateLoginFormUseCase(
    private val validator: LoginFormValidator = LoginFormValidator()
) {
    operator fun invoke(username: String, password: String): LoginFormValidationResult =
        validator.validate(username, password)
}

