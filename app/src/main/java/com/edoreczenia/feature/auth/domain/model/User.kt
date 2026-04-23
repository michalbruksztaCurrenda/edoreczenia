package com.edoreczenia.feature.auth.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val accountStatus: AccountStatus
)

