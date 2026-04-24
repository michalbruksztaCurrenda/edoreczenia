package com.edoreczenia.feature.auth.domain.model

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    EXPIRED,
    LOCKED,
    CODE_RESENT
}

