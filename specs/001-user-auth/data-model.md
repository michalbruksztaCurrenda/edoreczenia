# Data Model: 001-user-auth

**Feature Branch**: `001-user-auth`
**Date**: 2026-04-23
**Źródło**: spec.md + kontrakt API + research.md

---

## Modele domenowe (warstwa `domain`)

### User

```
User
├── id: String                    // "usr_8f4c3f8e"
├── username: String              // login = nazwa użytkownika
├── email: String
└── accountStatus: AccountStatus
```

### AccountStatus (enum/sealed)

```
AccountStatus
├── PENDING_VERIFICATION
├── ACTIVE
├── LOCKED
└── DISABLED
```

### Session

```
Session
├── accessToken: String           // JWT, in-memory only
├── refreshToken: String          // JWT, in-memory only
├── tokenType: String             // "Bearer"
├── expiresInSeconds: Int         // 900 (TTL do ustalenia z backendem)
└── refreshExpiresInSeconds: Int  // 28800 (TTL do ustalenia z backendem)
```

### AuthSessionState (sealed class)

```
AuthSessionState
├── UNAUTHENTICATED
├── AUTHENTICATING
├── AUTHENTICATED(user: User, session: Session)
├── SESSION_EXPIRED
├── LOGGING_OUT
└── ERROR(error: AppError)
```

### AuthResult<T> (sealed class)

```
AuthResult<T>
├── Success(data: T)
└── Failure(error: AppError)
```

### VerificationStatus (enum/sealed)

```
VerificationStatus
├── PENDING
├── VERIFIED
├── EXPIRED
├── LOCKED                        // po 3 błędnych próbach
└── CODE_RESENT
```

---

## Modele błędów (warstwa `core`)

### AppError (sealed class)

```
AppError
├── Network(message: String)
├── Unauthorized(code: String, message: String)
├── Forbidden(code: String, message: String)
├── SessionExpired
├── Validation(fieldErrors: Map<String, List<String>>, message: String)
├── Business(code: String, message: String)
└── Unknown(throwable: Throwable?)
```

> Mapowanie HTTP → AppError:
> - IOException / brak sieci → Network
> - HTTP 400 VALIDATION_ERROR → Validation
> - HTTP 401 AUTH_ERROR → Unauthorized
> - HTTP 403 AUTH_ERROR → Forbidden
> - HTTP 401 INVALID_REFRESH_TOKEN / 403 REFRESH_TOKEN_REVOKED → SessionExpired
> - HTTP 409 / 410 / 422 / 423 / 429 BUSINESS_ERROR → Business
> - HTTP 404 → Business
> - Inne → Unknown

---

## Modele warstwy `data` (DTO)

### RegisterRequest DTO

```
RegisterRequestDto
├── deviceName: String
├── username: String
├── email: String
├── password: String
└── confirmPassword: String
```

### RegisterResponse DTO

```
RegisterResponseDto
├── status: String                // "REGISTRATION_PENDING_VERIFICATION"
├── userId: String
├── username: String
├── email: String
├── verificationRequired: Boolean
└── message: String
```

### VerifyEmailCodeRequest DTO

```
VerifyEmailCodeRequestDto
├── username: String
└── verificationCode: String
```

### VerifyEmailCodeResponse DTO

```
VerifyEmailCodeResponseDto
├── status: String                // "ACCOUNT_VERIFIED"
├── userId: String
├── accountStatus: String         // "ACTIVE"
└── message: String
```

### ResendVerificationCodeRequest DTO

```
ResendVerificationCodeRequestDto
└── username: String
```

### ResendVerificationCodeResponse DTO

```
ResendVerificationCodeResponseDto
├── status: String                // "VERIFICATION_CODE_RESENT"
└── message: String
```

### LoginRequest DTO

```
LoginRequestDto
├── username: String
└── password: String
```

### LoginResponse DTO

```
LoginResponseDto
├── status: String                // "AUTHENTICATED"
├── user: UserDto
└── session: SessionDto
```

### UserDto

```
UserDto
├── id: String
├── username: String
├── email: String
└── accountStatus: String
```

### SessionDto

```
SessionDto
├── accessToken: String
├── refreshToken: String
├── tokenType: String
├── expiresInSeconds: Int
└── refreshExpiresInSeconds: Int
```

### LogoutRequest DTO

```
LogoutRequestDto
└── refreshToken: String
```

### RefreshTokenRequest DTO

```
RefreshTokenRequestDto
└── refreshToken: String
```

### RefreshTokenResponse DTO

```
RefreshTokenResponseDto
├── status: String                // "TOKEN_REFRESHED"
└── session: SessionDto
```

### ErrorResponse DTO (wspólny model błędu backendu)

```
ErrorResponseDto
├── status: String                // "VALIDATION_ERROR" | "AUTH_ERROR" | "BUSINESS_ERROR"
├── code: String
├── message: String
└── fieldErrors: Map<String, List<String>>? (nullable)
```

### SessionCheckResponse DTO

```
SessionCheckResponseDto
├── status: String                // "SESSION_ACTIVE"
└── user: UserDto
```

---

## UiState modele (warstwa `presentation`)

### LoginUiState

```
LoginUiState
├── isLoading: Boolean = false
├── usernameInput: String = ""
├── passwordInput: String = ""
├── usernameError: String? = null
├── passwordError: String? = null
└── formError: String? = null     // błąd ogólny (sieciowy, auth)
```

### RegistrationUiState

```
RegistrationUiState
├── isLoading: Boolean = false
├── deviceNameInput: String = ""
├── usernameInput: String = ""
├── emailInput: String = ""
├── passwordInput: String = ""
├── confirmPasswordInput: String = ""
├── deviceNameError: String? = null
├── usernameError: String? = null
├── emailError: String? = null
├── passwordError: String? = null
├── confirmPasswordError: String? = null
└── formError: String? = null
```

### VerifyEmailUiState

```
VerifyEmailUiState
├── isLoading: Boolean = false
├── username: String              // przekazany z rejestracji (nie edytowalny)
├── verificationCodeInput: String = ""
├── verificationCodeError: String? = null
├── formError: String? = null
├── canResend: Boolean = true
└── resendCooldownSeconds: Int = 0
```

---

## Relacje między modelami

```
AuthSessionState.AUTHENTICATED ──► User + Session
Session ──────────────────────────► przechowywana wyłącznie in-memory (SessionManager)
User ──────────────────────────────► tylko dane publiczne (id, username, email, status)
AppError ──────────────────────────► używany w AuthResult.Failure i UiState.formError
```

---

## Reguły walidacji (warstwa `domain`)

| Pole | Reguła | Validator |
|---|---|---|
| username (login) | Niepuste | `LoginFormValidator` |
| password (login) | Niepuste | `LoginFormValidator` |
| deviceName | Niepuste, 3–100 znaków | `RegistrationFormValidator` |
| username (rejestracja) | Niepuste, 3–50 znaków | `RegistrationFormValidator` |
| email | Niepuste, poprawny format RFC-like | `RegistrationFormValidator` |
| password (rejestracja) | Niepuste, min 8 znaków, ≥1 wielka litera, ≥1 cyfra lub znak specjalny | `RegistrationFormValidator` |
| confirmPassword | Niepuste, równe password | `RegistrationFormValidator` |
| verificationCode | Niepuste, trim białych znaków, niepusty po trim | `VerificationCodeValidator` |

---

## Stany domenowe API (zachowane jako stałe domenowe)

### AuthResponseStatus

```
REGISTRATION_PENDING_VERIFICATION
VERIFICATION_CODE_RESENT
ACCOUNT_VERIFIED
AUTHENTICATED
TOKEN_REFRESHED
LOGGED_OUT
SESSION_ACTIVE
```

### AuthSessionState (wymuszone przez kontrakt)

```
UNAUTHENTICATED
AUTHENTICATING
AUTHENTICATED
SESSION_EXPIRED
LOGGING_OUT
ERROR
```

