# Implementation Plan: 001-user-auth — Uwierzytelnianie Użytkownika

**Branch**: `001-user-auth` | **Date**: 2026-04-23 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-user-auth/spec.md`

---

## Summary

Implementacja kompletnego modułu uwierzytelniania dla aplikacji Android eDoreczenia obejmującego: logowanie, rejestrację, weryfikację konta e-mail, zarządzanie sesją in-memory oraz wylogowanie. Moduł komunikuje się wyłącznie z backendowym API (`/api/v1/auth`). Brak lokalnej bazy danych — tokeny sesji żyją wyłącznie w pamięci procesu. Architektura warstwowa (presentation / domain / data / core), DI przez Hilt, UI w Compose z Material 3.

---

## Technical Context

**Language/Version**: Kotlin 2.3.20  
**Primary Dependencies**: Jetpack Compose BOM 2026.03.00, Material 3, Hilt 2.56, Retrofit 2.11, OkHttp 4.12, Kotlinx Serialization 1.8.1, Navigation Compose 2.9.0, Lifecycle ViewModel Compose 2.10.0  
**Storage**: Brak Room dla auth. Tokeny sesji wyłącznie in-memory (`SessionManager` singleton). Brak EncryptedSharedPreferences dla tokenów (sesja nietrwała między restartami).  
**Testing**: JUnit 4, MockK 1.14, kotlinx-coroutines-test 1.10, FakeAuthRepository  
**Target Platform**: Android, minSdk 34, targetSdk/compileSdk 36, JVM 17  
**Project Type**: Native Android Mobile App  
**Performance Goals**: Logowanie < 5s przy standardowym połączeniu (SC-001). Rejestracja + dotarcie do ekranu weryfikacji < 3 min (SC-002).  
**Constraints**: Sesja nietrwała między restartami. Brak Room dla auth. Brak lokalnego hasła i kodu weryfikacyjnego. Tokeny tylko in-memory.  
**Scale/Scope**: 3 ekrany auth, 10 use case'ów, 1 moduł Hilt dla auth, 1 fake repository.

---

## Constitution Check

| Zasada | Status | Uzasadnienie |
|---|---|---|
| III. Architektura warstwowa | ✅ PASS | Moduł podzielony na presentation/domain/data/core |
| IV. Modularność feature'ów | ✅ PASS | Osobny pakiet `feature/auth` z wewnętrznym podziałem |
| VI. Zakaz hardcodowania | ✅ PASS | BASE_URL przez `BuildConfig`, wersje bibliotek w `libs.versions.toml` |
| VII. Obsługa błędów — 5 stanów | ✅ PASS | Każdy ekran obsługuje Initial/Loading/Success/Error |
| VIII. Bezpieczeństwo danych | ✅ PASS | Tokeny wyłącznie in-memory, brak persistencji haseł |
| IX. Dane lokalne | ✅ PASS | Brak Room dla auth — zgodnie z założeniami architektonicznymi |
| X. Komunikacja sieciowa | ✅ PASS | Wywołania przez warstwę data i repozytoria, jawne DTO |
| XII. Walidacja danych | ✅ PASS | Walidatory w warstwie domain, wywoływane przed żądaniem do API |
| XIII. Testowalność | ✅ PASS | FakeAuthRepository, MockK, testowalne ViewModele i use case'y |
| XX. Zakazy bezwzględne | ✅ PASS | Brak wywołań sieciowych z presentation, brak surowych wyjątków do UI |

**GATE: PASS** — brak naruszeń Konstytucji.

> **Uwaga**: `android_requirements.md` nie istnieje. Wymagania pobrane z `spec.md` i Konstytucji. Plik `android_requirements.md` należy utworzyć jako osobne zadanie (patrz sekcja 11 — Elementy odroczone).

---

## Project Structure

### Documentation (this feature)

```text
specs/001-user-auth/
├── plan.md              # Ten plik
├── research.md          # Phase 0 — decyzje techniczne
├── data-model.md        # Phase 1 — modele i DTO
├── quickstart.md        # Phase 1 — przewodnik developera
├── contracts/
│   └── api-contract.md  # Phase 1 — pełny kontrakt API
└── tasks.md             # Phase 2 — do wygenerowania przez /speckit.tasks
```

### Source Code (repository root)

```text
app/src/main/java/com/edoreczenia/
├── core/
│   ├── error/
│   │   └── AppError.kt                     # sealed class — wspólny model błędu
│   ├── network/
│   │   ├── NetworkModule.kt                # Hilt module — Retrofit, OkHttp
│   │   └── AuthInterceptor.kt              # OkHttp interceptor — Bearer token
│   └── session/
│       └── SessionManager.kt               # Singleton — stan sesji in-memory
│
└── feature/
    └── auth/
        ├── data/
        │   ├── api/
        │   │   └── AuthApi.kt              # Retrofit interface
        │   ├── dto/
        │   │   ├── request/                # DTO requestów (Register, Login, itd.)
        │   │   └── response/               # DTO odpowiedzi
        │   ├── mapper/
        │   │   └── AuthMapper.kt           # DTO ↔ modele domenowe
        │   └── repository/
        │       ├── AuthRepositoryImpl.kt   # Implementacja repozytorium
        │       └── FakeAuthRepository.kt   # Fake do testów i dev
        ├── di/
        │   └── AuthModule.kt               # Hilt module — bindowanie repo, use case'ów
        ├── domain/
        │   ├── model/
        │   │   ├── User.kt
        │   │   ├── Session.kt
        │   │   ├── AccountStatus.kt
        │   │   ├── AuthSessionState.kt
        │   │   ├── AuthResult.kt
        │   │   └── VerificationStatus.kt
        │   ├── repository/
        │   │   └── AuthRepository.kt       # interfejs kontraktu
        │   ├── usecase/
        │   │   ├── LoginUseCase.kt
        │   │   ├── RegisterUserUseCase.kt
        │   │   ├── VerifyEmailCodeUseCase.kt
        │   │   ├── ResendVerificationCodeUseCase.kt
        │   │   ├── LogoutUseCase.kt
        │   │   ├── GetCurrentSessionUseCase.kt
        │   │   ├── RefreshSessionUseCase.kt
        │   │   ├── ValidateLoginFormUseCase.kt
        │   │   ├── ValidateRegistrationFormUseCase.kt
        │   │   └── ValidateVerificationCodeUseCase.kt
        │   └── validator/
        │       ├── LoginFormValidator.kt
        │       ├── RegistrationFormValidator.kt
        │       └── VerificationCodeValidator.kt
        └── presentation/
            ├── navigation/
            │   └── AuthNavGraph.kt         # zagnieżdżony graf nawigacyjny
            ├── login/
            │   ├── LoginScreen.kt
            │   ├── LoginViewModel.kt
            │   └── LoginUiState.kt
            ├── registration/
            │   ├── RegistrationScreen.kt
            │   ├── RegistrationViewModel.kt
            │   └── RegistrationUiState.kt
            └── verifyemail/
                ├── VerifyEmailScreen.kt
                ├── VerifyEmailViewModel.kt
                └── VerifyEmailUiState.kt

app/src/test/java/com/edoreczenia/
└── feature/auth/
    ├── domain/
    │   ├── usecase/                        # testy use case'ów
    │   └── validator/                      # testy walidatorów
    ├── data/
    │   ├── mapper/                         # testy maperów DTO
    │   └── repository/                     # testy AuthRepositoryImpl z MockWebServer
    └── presentation/
        ├── login/                          # testy LoginViewModel
        ├── registration/                   # testy RegistrationViewModel
        └── verifyemail/                    # testy VerifyEmailViewModel
```

**Structure Decision**: Wariant "Mobile" (Option 3 z szablonu). Jeden moduł Android app. Kod podzielony na pakiety `core/` (wspólne) i `feature/auth/` (feature-specific). Struktura odwzorowuje architekturę warstwową wprost na hierarchię pakietów.

---

## 1. Architektura modułu

### Warstwy i ich odpowiedzialności

#### `presentation` — co widzi użytkownik

- Composable screens: `LoginScreen`, `RegistrationScreen`, `VerifyEmailScreen`
- ViewModele: zarządzają `UiState` i reagują na akcje użytkownika
- `AuthNavGraph`: definiuje graf nawigacji dla ekranów auth
- **Nie zawiera** logiki biznesowej ani wywołań sieciowych
- Dozwolone zależności: `domain` (use case'y, modele), `core` (AppError, nawigacja)

#### `domain` — logika biznesowa

- Modele domenowe: `User`, `Session`, `AccountStatus`, `AuthSessionState`, `AuthResult`, `VerificationStatus`
- Use case'y: orkiestrują wywołania repozytoriów i walidatorów
- Walidatory: `LoginFormValidator`, `RegistrationFormValidator`, `VerificationCodeValidator`
- Kontrakt: `AuthRepository` (interfejs — implementacja w `data`)
- **Nie zna** Androida, Retrofit, żadnych bibliotek zewnętrznych
- Dozwolone zależności: `core` (AppError, modele bazowe)

#### `data` — dostęp do danych

- `AuthApi`: interfejs Retrofit dla endpointów `/api/v1/auth`
- DTO: modele żądań i odpowiedzi (request/response)
- `AuthMapper`: mapowanie DTO ↔ modele domenowe
- `AuthRepositoryImpl`: implementuje `AuthRepository`, wywołuje `AuthApi`, obsługuje błędy HTTP
- `FakeAuthRepository`: implementuje `AuthRepository`, zwraca predefiniowane dane — do testów i developmentu
- `SessionManager`: zarządza stanem sesji in-memory (wstrzykiwany przez Hilt jako `@Singleton`)
- Dozwolone zależności: `domain` (tylko kontrakty/modele), `core`

#### `core` — fundament

- `AppError`: sealed class — wspólny model błędu aplikacji
- `NetworkModule`: Hilt module konfigurujący Retrofit, OkHttp, interceptory
- `AuthInterceptor`: dodaje nagłówek `Authorization: Bearer` do chronionych żądań
- `SessionManager`: in-memory store dla `Session` i `AuthSessionState`
- **Nie może zależeć** od `presentation`

### Kierunki zależności

```
LoginViewModel
    → LoginUseCase (domain)
        → AuthRepository (kontrakt domain)
            ← AuthRepositoryImpl (data) [implementacja wstrzyknięta przez Hilt]
                → AuthApi (Retrofit)
                → SessionManager (core)
    → ValidateLoginFormUseCase (domain)
        → LoginFormValidator (domain)
```

---

## 2. Struktura pakietów

### Feature-specific (wewnątrz `feature/auth/`)

| Element | Pakiet | Warstwa |
|---|---|---|
| `LoginScreen`, `LoginViewModel`, `LoginUiState` | `feature.auth.presentation.login` | presentation |
| `RegistrationScreen`, `RegistrationViewModel`, `RegistrationUiState` | `feature.auth.presentation.registration` | presentation |
| `VerifyEmailScreen`, `VerifyEmailViewModel`, `VerifyEmailUiState` | `feature.auth.presentation.verifyemail` | presentation |
| `AuthNavGraph` | `feature.auth.presentation.navigation` | presentation |
| `User`, `Session`, `AccountStatus`, `AuthSessionState`, `AuthResult`, `VerificationStatus` | `feature.auth.domain.model` | domain |
| `AuthRepository` (interfejs) | `feature.auth.domain.repository` | domain |
| `LoginUseCase`, `RegisterUserUseCase`, ... | `feature.auth.domain.usecase` | domain |
| `LoginFormValidator`, `RegistrationFormValidator`, `VerificationCodeValidator` | `feature.auth.domain.validator` | domain |
| `AuthApi` | `feature.auth.data.api` | data |
| DTO request/response | `feature.auth.data.dto.request/response` | data |
| `AuthMapper` | `feature.auth.data.mapper` | data |
| `AuthRepositoryImpl`, `FakeAuthRepository` | `feature.auth.data.repository` | data |
| `AuthModule` (Hilt) | `feature.auth.di` | data/di |

### Core (współdzielone z innymi feature'ami)

| Element | Pakiet | Rola |
|---|---|---|
| `AppError` | `core.error` | Model błędów aplikacji |
| `NetworkModule` | `core.network` | Konfiguracja Retrofit/OkHttp |
| `AuthInterceptor` | `core.network` | Bearer token do żądań |
| `SessionManager` | `core.session` | In-memory stan sesji |

---

## 3. Warstwa presentation

### LoginScreen

**ViewModel**: `LoginViewModel`

**UiState**: `LoginUiState`
```
isLoading: Boolean
usernameInput: String
passwordInput: String
usernameError: String?   // "Pole wymagane"
passwordError: String?   // "Pole wymagane"
formError: String?       // Ogólny komunikat auth/sieciowy
```

**Effect** (Channel): `LoginEffect`
```
NavigateToMain
NavigateToRegistration
NavigateToVerifyEmail(username: String)  // gdy konto niezweryfikowane
ShowMessage(text: String)
```

**UserAction (metody ViewModelu)**:
- `onUsernameChanged(value: String)`
- `onPasswordChanged(value: String)`
- `onLoginClicked()`
- `onRegisterClicked()`
- `onRetryClicked()`

**Stany**:
- `Initial`: pusty formularz, brak błędów
- `Loading`: `isLoading = true`, formularz i przycisk zablokowane
- `Success`: emitowany `LoginEffect.NavigateToMain`
- `Error`: `formError` ustawiony, komunikat ogólny (bezpieczeństwo — FR-004)

**Obsługa błędów backendu**:
- `AppError.Unauthorized (INVALID_CREDENTIALS)` → `formError = res/string/error_invalid_credentials`
- `AppError.Forbidden (ACCOUNT_NOT_ACTIVE)` → efekt `NavigateToVerifyEmail(username)`
- `AppError.Business (ACCOUNT_LOCKED)` → `formError = res/string/error_account_locked`
- `AppError.Business (TOO_MANY_LOGIN_ATTEMPTS)` → `formError = res/string/error_too_many_attempts`
- `AppError.Network` → `formError = res/string/error_network`

**Nawigacja**:
- `LoginScreen → RegistrationScreen`: przycisk "Zarejestruj się" → `NavigateToRegistration`
- `LoginScreen → Main`: po sukcesie → `NavigateToMain` z `popUpTo(authGraph) { inclusive = true }`
- `LoginScreen → VerifyEmailScreen`: konto niezweryfikowane → `NavigateToVerifyEmail(username)`
- Back z LoginScreen: wyjście z aplikacji (LoginScreen jest rootem grafu auth)

---

### RegistrationScreen

**ViewModel**: `RegistrationViewModel`

**UiState**: `RegistrationUiState`
```
isLoading: Boolean
deviceNameInput: String
usernameInput: String
emailInput: String
passwordInput: String
confirmPasswordInput: String
deviceNameError: String?
usernameError: String?
emailError: String?
passwordError: String?
confirmPasswordError: String?
formError: String?
```

**Effect**: `RegistrationEffect`
```
NavigateToVerifyEmail(username: String, email: String)
NavigateToLogin
ShowMessage(text: String)
```

**UserAction**:
- `onDeviceNameChanged(value: String)`, `onUsernameChanged(value: String)`, `onEmailChanged(value: String)`, `onPasswordChanged(value: String)`, `onConfirmPasswordChanged(value: String)`
- `onRegisterClicked()`
- `onBackToLoginClicked()`

**Stany**:
- `Initial`: pusty formularz
- `Loading`: formularz i przycisk zablokowane (FR-014 — zapobieganie duplikatom żądań)
- `Success`: `NavigateToVerifyEmail(username, email)`
- `Error`: błędy pól (walidacja kliencka) lub `formError` (błędy backendu)

**Walidacja kliencka** (inline — na onChange lub onSubmit):
- Wszystkie pola wymagane (FR-009)
- Email poprawny format (FR-011)
- Hasło spełnia polityę (FR-012)
- confirmPassword == password (FR-010)

**Obsługa błędów backendu**:
- `AppError.Business(USERNAME_ALREADY_EXISTS)` → `usernameError` (FR-013)
- `AppError.Business(EMAIL_ALREADY_EXISTS)` → `emailError` (FR-013)
- `AppError.Validation(fieldErrors)` → mapowanie na odpowiednie pola
- `AppError.Network` → `formError`

**Nawigacja**:
- Back / "Powrót do logowania" → `NavigateToLogin`
- Po sukcesie → `NavigateToVerifyEmail(username, email)`

---

### VerifyEmailScreen

**ViewModel**: `VerifyEmailViewModel`

**Parametry nawigacji**: `username: String` (przekazany z RegistrationScreen)

**UiState**: `VerifyEmailUiState`
```
isLoading: Boolean
username: String            // display-only, niezmienialny
verificationCodeInput: String
verificationCodeError: String?
formError: String?
canResend: Boolean          // czy można wysłać ponownie
resendCooldownSeconds: Int  // odliczanie po resend
```

**Effect**: `VerifyEmailEffect`
```
NavigateToLogin(showSuccessMessage: Boolean)
ShowMessage(text: String)
```

**UserAction**:
- `onVerificationCodeChanged(value: String)`
- `onVerifyClicked()`
- `onResendCodeClicked()`
- `onBackClicked()`

**Stany**:
- `Initial`: pusty kod, `canResend = true`
- `Loading`: przycisk zablokowany
- `Success`: `NavigateToLogin(showSuccessMessage = true)` — FR-029, FR-021 (auto-trim kodu)
- `Error`: komunikat błędu z liczbą pozostałych prób (FR-018)

**Obsługa błędów backendu**:
- `AppError.Unauthorized(INVALID_VERIFICATION_CODE)` → `verificationCodeError` + info o pozostałych próbach
- `AppError.Business(VERIFICATION_CODE_EXPIRED)` → `formError` + sugestia resend
- `AppError.Business(VERIFICATION_LOCKED)` → `formError`, `canResend = true` (wymagany nowy kod)
- `AppError.Business(RESEND_LIMIT_EXCEEDED)` → `formError`, `canResend = false`
- `AppError.Network` → `formError`

**Nawigacja**:
- Po sukcesie → `NavigateToLogin(showSuccessMessage = true)` z `popUpTo(verifyEmail) { inclusive = true }`
- Back → `NavigateToLogin` lub `popBackStack()` (nie można cofnąć się do stanu "zarejestrowany, niezweryfikowany" w kółko)

---

## 4. Warstwa domain

### Modele domenowe

Opisane szczegółowo w `data-model.md`. Kluczowe:
- `User(id, username, email, accountStatus)`
- `Session(accessToken, refreshToken, tokenType, expiresInSeconds, refreshExpiresInSeconds)`
- `AccountStatus`: `PENDING_VERIFICATION | ACTIVE | LOCKED | DISABLED`
- `AuthSessionState`: `UNAUTHENTICATED | AUTHENTICATING | AUTHENTICATED(user, session) | SESSION_EXPIRED | LOGGING_OUT | ERROR`
- `AuthResult<T>`: `Success(data: T) | Failure(error: AppError)`

### AuthRepository (interfejs kontraktu)

```kotlin
interface AuthRepository {
    suspend fun login(username: String, password: String): AuthResult<Pair<User, Session>>
    suspend fun register(deviceName: String, username: String, email: String, password: String, confirmPassword: String): AuthResult<String> // userId
    suspend fun verifyEmailCode(username: String, verificationCode: String): AuthResult<Unit>
    suspend fun resendVerificationCode(username: String): AuthResult<Unit>
    suspend fun logout(refreshToken: String): AuthResult<Unit>
    suspend fun refreshSession(refreshToken: String): AuthResult<Session>
    suspend fun checkSession(): AuthResult<User>
}
```

### Use Case'y

#### LoginUseCase
- **Wejście**: `username: String`, `password: String`
- **Wyjście**: `AuthResult<Pair<User, Session>>`
- **Odpowiedzialność**: wywołuje `AuthRepository.login()`, sukces → aktualizuje `SessionManager` (state → `AUTHENTICATED`), błąd → przekazuje `AppError`
- **Nie wykonuje** walidacji formularza — to robi `ValidateLoginFormUseCase`

#### RegisterUserUseCase
- **Wejście**: `deviceName, username, email, password, confirmPassword: String`
- **Wyjście**: `AuthResult<String>` (userId)
- **Odpowiedzialność**: wywołuje `AuthRepository.register()`

#### VerifyEmailCodeUseCase
- **Wejście**: `username: String`, `verificationCode: String` (już po trim)
- **Wyjście**: `AuthResult<Unit>`
- **Odpowiedzialność**: wywołuje `AuthRepository.verifyEmailCode()`

#### ResendVerificationCodeUseCase
- **Wejście**: `username: String`
- **Wyjście**: `AuthResult<Unit>`

#### LogoutUseCase
- **Wejście**: brak (pobiera refreshToken z SessionManager)
- **Wyjście**: `AuthResult<Unit>`
- **Odpowiedzialność**: wywołuje `AuthRepository.logout()`, następnie `SessionManager.clearSession()` — niezależnie od wyniku API (local-first logout)

#### GetCurrentSessionUseCase
- **Wejście**: brak
- **Wyjście**: `AuthSessionState` z `SessionManager.sessionState`
- **Odpowiedzialność**: zwraca aktualny stan sesji z pamięci

#### RefreshSessionUseCase
- **Wejście**: brak (pobiera refreshToken z SessionManager)
- **Wyjście**: `AuthResult<Session>`
- **Odpowiedzialność**: wywołuje `AuthRepository.refreshSession()`, sukces → aktualizuje session w SessionManager, błąd → `SessionManager` → `SESSION_EXPIRED`

#### ValidateLoginFormUseCase
- **Wejście**: `username: String`, `password: String`
- **Wyjście**: `LoginFormValidationResult` (błędy per pole lub OK)
- **Odpowiedzialność**: deleguje do `LoginFormValidator`

#### ValidateRegistrationFormUseCase
- **Wejście**: wszystkie pola formularza rejestracji
- **Wyjście**: `RegistrationFormValidationResult`
- **Odpowiedzialność**: deleguje do `RegistrationFormValidator`

#### ValidateVerificationCodeUseCase
- **Wejście**: `code: String`
- **Wyjście**: `VerificationCodeValidationResult`
- **Odpowiedzialność**: trim + delegacja do `VerificationCodeValidator`

### Walidatory

#### LoginFormValidator
- `username`: niepuste → OK, puste → `FieldError("Pole wymagane")`
- `password`: niepuste → OK, puste → `FieldError("Pole wymagane")`

#### RegistrationFormValidator
- `deviceName`: niepuste, 3–100 znaków
- `username`: niepuste, 3–50 znaków
- `email`: niepuste, regex format e-mail
- `password`: niepuste, min 8 znaków, ≥1 wielka litera, ≥1 cyfra lub znak specjalny
- `confirmPassword`: niepuste, równe `password`

#### VerificationCodeValidator
- `code` po trim: niepuste

### Mapowanie błędów (domain → AppError)

Use case'y otrzymują `AppError` z warstwy `data` i przekazują je do ViewModeli przez `AuthResult.Failure`. ViewModel mapuje `AppError` na komunikaty UI (zasoby stringów).

---

## 5. Warstwa data

### AuthApi (interfejs Retrofit)

```
POST /api/v1/auth/register       → RegisterRequestDto → RegisterResponseDto
POST /api/v1/auth/verify-email-code → VerifyEmailCodeRequestDto → VerifyEmailCodeResponseDto
POST /api/v1/auth/resend-verification-code → ResendVerificationCodeRequestDto → ResendVerificationCodeResponseDto
POST /api/v1/auth/login          → LoginRequestDto → LoginResponseDto
POST /api/v1/auth/logout         → LogoutRequestDto → Unit (200 OK)
POST /api/v1/auth/refresh        → RefreshTokenRequestDto → RefreshTokenResponseDto
GET  /api/v1/auth/session        → SessionCheckResponseDto
```

Wszystkie DTO opisane szczegółowo w `data-model.md`.

### AuthMapper

Odpowiedzialny za konwersję:
- `LoginResponseDto` → `Pair<User, Session>`
- `RegisterResponseDto` → `String` (userId)
- `VerifyEmailCodeResponseDto` → `Unit`
- `ResendVerificationCodeResponseDto` → `Unit`
- `RefreshTokenResponseDto` → `Session`
- `SessionCheckResponseDto` → `User`
- `UserDto` → `User`
- `SessionDto` → `Session`
- `String (accountStatus)` → `AccountStatus`
- `ErrorResponseDto` → `AppError`

### AuthRepositoryImpl

- Opakowuje każde wywołanie `AuthApi` w `try/catch`
- `HttpException` → parsuje ciało błędu przez `ErrorResponseDto` → `AppError` (przez `AuthMapper`)
- `IOException` → `AppError.Network`
- Inne `Exception` → `AppError.Unknown`
- Sukces → mapuje DTO na modele domenowe przez `AuthMapper`

#### Mapowanie kodów HTTP → AppError

| HTTP | code | AppError |
|---|---|---|
| 400 | INVALID_REQUEST / INVALID_VERIFICATION_CODE_FORMAT | `Validation(fieldErrors, message)` |
| 401 | INVALID_CREDENTIALS | `Unauthorized(code, message)` |
| 401 | INVALID_VERIFICATION_CODE | `Unauthorized(code, message)` |
| 401 | INVALID_REFRESH_TOKEN | `SessionExpired` |
| 403 | ACCOUNT_NOT_ACTIVE | `Forbidden(code, message)` |
| 403 | REFRESH_TOKEN_REVOKED | `SessionExpired` |
| 404 | USER_NOT_FOUND | `Business(code, message)` |
| 409 | USERNAME_ALREADY_EXISTS / EMAIL_ALREADY_EXISTS / ACCOUNT_ALREADY_VERIFIED | `Business(code, message)` |
| 410 | VERIFICATION_CODE_EXPIRED | `Business(code, message)` |
| 422 | PASSWORD_POLICY_VIOLATION | `Validation(fieldErrors, message)` |
| 423 | ACCOUNT_LOCKED / VERIFICATION_LOCKED | `Business(code, message)` |
| 429 | TOO_MANY_LOGIN_ATTEMPTS / RESEND_LIMIT_EXCEEDED | `Business(code, message)` |
| IOException | — | `Network(message)` |
| inne | — | `Unknown(throwable)` |

### SessionManager

- Singleton (`@Singleton` Hilt)
- Przechowuje: `var session: Session?` (in-memory), `val sessionState: MutableStateFlow<AuthSessionState>`
- Metody: `setSession(user, session)`, `clearSession()`, `getAccessToken(): String?`, `getRefreshToken(): String?`
- `clearSession()` → `sessionState.value = UNAUTHENTICATED`, oba tokeny `null`
- Po kill procesu — dane znikają automatycznie (in-memory)

### AuthInterceptor (OkHttp)

- Pobiera `accessToken` z `SessionManager`
- Jeśli token dostępny: dodaje `Authorization: Bearer <token>` do żądania
- Żądania publiczne (login, register, verify, resend, refresh) — interceptor nie blokuje, token może być null

### Token Refresh — OkHttp Authenticator

- Triggowany przy HTTP 401 dla chronionych endpointów
- Wywołuje `POST /api/v1/auth/refresh` z `refreshToken`
- Sukces → aktualizuje `SessionManager`, ponawia oryginalne żądanie
- Błąd → `SessionManager.clearSession()`, emituje `SESSION_EXPIRED`

---

## 6. Dane lokalne i bezpieczeństwo

### Co przechowujemy lokalnie

| Dane | Gdzie | Dlaczego |
|---|---|---|
| `accessToken` | In-memory (`SessionManager`) | Wymagane do autoryzacji żądań przez czas życia sesji |
| `refreshToken` | In-memory (`SessionManager`) | Wymagane do odświeżania sesji przez czas życia procesu |
| `user.id`, `user.username`, `user.email`, `user.accountStatus` | In-memory (`SessionManager.AuthSessionState.AUTHENTICATED`) | Minimalne dane do wyświetlenia stanu aplikacji |

### Czego NIE przechowujemy

- Hasło użytkownika — nigdy, nigdzie (zasada VIII)
- Kod weryfikacyjny — nigdy, nigdzie (zasada VIII)
- Tokeny w `EncryptedSharedPreferences` / `DataStore` / Room — sesja ma być nietrwała między restartami
- Żadne dane auth w logach (zasada XV)

### Dlaczego nie używamy Room dla auth

1. **Sesja nietrwała** — wymaganie biznesowe explicite zabrania przetrwania sesji między restartami. Room trwale persystuje dane. In-memory jest prostsze i bezpieczniejsze dla tego wymagania.
2. **Brak lokalnej bazy użytkowników** — aplikacja nie jest źródłem prawdy. Backend decyduje o stanie konta.
3. **Prostota testów** — FakeAuthRepository jest prostszy niż baza Room in-memory do testowania use case'ów.
4. **Mniejsza powierzchnia ataku** — brak trwałego zapisu tokenów = brak ryzyka odczytu danych po reinstalacji.

### Wpływ na testy i development

- `FakeAuthRepository` implementuje `AuthRepository` i zwraca predefiniowane wyniki
- Ustawienie `FakeAuthRepository.shouldFailWith = AppError.Network` testuje ścieżkę błędu
- Hilt `@TestInstallIn` zastępuje `AuthModule` modułem testowym wstrzykującym `FakeAuthRepository`
- Brak potrzeby SQLite in-memory, brak potrzeby serwera HTTP w testach jednostkowych

---

## 7. Modele domenowe i statusy

Opisane szczegółowo w `data-model.md`. Podsumowanie:

| Model | Warstwa | Opis |
|---|---|---|
| `User` | domain | Dane użytkownika (id, username, email, status) |
| `Session` | domain | Tokeny i TTL (in-memory only) |
| `AccountStatus` | domain | `PENDING_VERIFICATION / ACTIVE / LOCKED / DISABLED` |
| `AuthSessionState` | domain | Stan sesji w aplikacji (sealed class z danymi) |
| `AuthResult<T>` | domain | `Success<T> / Failure(AppError)` — wynik operacji auth |
| `VerificationStatus` | domain | `PENDING / VERIFIED / EXPIRED / LOCKED / CODE_RESENT` |
| `AppError` | core | Wspólny model błędu (sealed class) |

Wszystkie stringowe statusy backendu mapowane są na typy domenowe w `AuthMapper` — warstwa domain nigdy nie operuje na surowych stringach z API.

---

## 8. Nawigacja i flow

### Graf nawigacyjny (`AuthNavGraph`)

```
AUTH_GRAPH (startDestination = Login)
├── Login
├── Registration
└── VerifyEmail/{username}
```

`AUTH_GRAPH` jest zagnieżdżonym grafem podłączanym do grafu głównej aplikacji.

### Flows

#### App start → Login
- `MainActivity` sprawdza `SessionManager.sessionState`
- `UNAUTHENTICATED` → nawigacja do `AUTH_GRAPH`
- `AUTHENTICATED` → nawigacja do `MAIN_GRAPH` (inbox)
- `SESSION_EXPIRED` → nawigacja do `AUTH_GRAPH` z komunikatem

#### Login → Register
- Przycisk "Zarejestruj się" → `navigate(Registration)`
- Back z Registration → `popBackStack()` → Login

#### Register → Verify Email
- Sukces rejestracji → `navigate(VerifyEmail/{username})` z `popUpTo(Registration) { inclusive = true }`
- Back z VerifyEmail → Login (nie Registration — FR-028, edge case cofania)

#### Verify Email → Login
- Sukces weryfikacji → `navigate(Login)` z `popUpTo(AUTH_GRAPH)` i `showSuccessMessage = true`
- Back ręczny → `popBackStack()` → Login

#### Login success → Main App
- Sukces logowania → `navigate(MAIN_GRAPH)` z `popUpTo(AUTH_GRAPH) { inclusive = true }`
- Cofanie z Main nie wraca do ekranów auth

#### Logout → Login
- `LogoutUseCase` wywołany z dowolnego miejsca (np. Settings)
- `SessionManager.clearSession()` → `SESSION_STATE = UNAUTHENTICATED`
- Główna nawigacja reaguje na zmianę stanu → `navigate(AUTH_GRAPH)` z `popUpTo(MAIN_GRAPH) { inclusive = true }`

#### Wygaśnięcie sesji
- `AuthInterceptor` / `Authenticator` → `SessionManager.clearSession()` → state = `SESSION_EXPIRED`
- Aplikacja obserwuje `sessionState` (np. w `MainActivity` lub globalnym ViewModel)
- Automatyczne przekierowanie na Login z komunikatem "Sesja wygasła"

### Zachowanie przy aktywnej sesji po powrocie do aplikacji

- Process nie był killowany → `SessionManager` ma tokeny in-memory → sesja aktywna
- Process był killowany → `SessionManager` pusty → `UNAUTHENTICATED` → ekran Login

---

## 9. Testowalność i strategia testów

### Testy jednostkowe walidatorów

- `LoginFormValidatorTest`: puste username/password → błąd; wypełnione → OK
- `RegistrationFormValidatorTest`: każde pole z granicznymi wartościami (2 znaki, 3 znaki, 50, 51 znaków), email format, polityka haseł (8 znaków, 7 znaków, brak wielkiej litery, brak cyfry/znaku specjalnego), confirmPassword niezgodne
- `VerificationCodeValidatorTest`: pusty string, string ze spacjami, poprawny kod po trim

### Testy jednostkowe use case'ów

- `LoginUseCaseTest`: sukces → `SessionManager` zaktualizowany; błąd HTTP 401 → `AuthResult.Failure(Unauthorized)`; błąd sieciowy → `AuthResult.Failure(Network)`
- `RegisterUserUseCaseTest`: sukces → `AuthResult.Success(userId)`; HTTP 409 → `Failure(Business(USERNAME_ALREADY_EXISTS))`
- `VerifyEmailCodeUseCaseTest`: sukces → `AuthResult.Success`; HTTP 423 → `Failure(Business(VERIFICATION_LOCKED))`
- `LogoutUseCaseTest`: `SessionManager.clearSession()` wywołany niezależnie od wyniku API; sukces API; błąd API (local-first logout)
- `RefreshSessionUseCaseTest`: sukces → session zaktualizowana; błąd → `SESSION_EXPIRED` w SessionManager

### Testy ViewModeli

- Wszystkie `ViewModel` testowane z `TestCoroutineDispatcher` i `runTest`
- `LoginViewModelTest`: puste pola → walidacja przed wywołaniem use case'a; sukces → efekt `NavigateToMain`; błąd sieci → `formError` ustawiony; `isLoading` przełącza się poprawnie; brak duplikatów żądań przy wielokrotnym kliknięciu
- `RegistrationViewModelTest`: walidacja pól inline; HTTP 409 → błąd przy odpowiednim polu; sukces → efekt `NavigateToVerifyEmail`
- `VerifyEmailViewModelTest`: trim kodu przed wysłaniem; LOCKED → `canResend = true`; sukces → efekt `NavigateToLogin`

### Testy mapowania DTO

- `AuthMapperTest`: `LoginResponseDto` → `User` + `Session`; `ErrorResponseDto` → poprawny `AppError`; `String` accountStatus → `AccountStatus` (nieznany status → fallback)

### Testy AuthRepositoryImpl

- Z `MockWebServer` (OkHttp): HTTP 200 → sukces; HTTP 401 → `AppError.Unauthorized`; IOException → `AppError.Network`; malformed JSON → `AppError.Unknown`

### Dlaczego FakeAuthRepository jest lepsze niż lokalna baza użytkowników

1. **Deterministyczność**: Fake zwraca dokładnie te dane, które ustawiamy w teście. Baza może mieć stan uboczny.
2. **Szybkość**: Fake działa w pamięci, bez I/O. Room in-memory ma overhead inicjalizacji.
3. **Izolacja**: Fake nie wymaga żadnych zależności Androida — testy mogą być pure JVM unit tests.
4. **Kontrola błędów**: `fake.shouldFailWith = AppError.Network` — jedna linijka, pełna kontrola ścieżki błędu.
5. **Spójność z architekturą**: Auth nie używa Room z założenia — użycie Room in-memory w testach byłoby inkoherentne z architekturą produkcyjną.

---

## 10. Trade-offy i decyzje architektoniczne

### Dlaczego auth nie używa lokalnej bazy jako źródła prawdy

Aplikacja Android jest tylko klientem. Backend jest jedynym źródłem prawdy dla: stanu konta, poprawności hasła, ważności kodu weryfikacyjnego i sesji. Lokalny cache użytkowników stwarzałby ryzyko niespójności stanu (np. konto zablokowane na backendzie, aktywne lokalnie) oraz zwiększał powierzchnię ataku.

### Dlaczego brak Room dla auth

- Sesja ma być nietrwała między restartami — Room daje trwałość, której nie potrzebujemy
- Brak potrzeby złożonych zapytań, relacji, migracji schematów
- In-memory SessionManager jest prostszy, bezpieczniejszy i wystarczający
- Zgodność z zasadą IX Konstytucji: każde przechowywanie danych musi być uzasadnione funkcjonalnie

### Dlaczego sesja nietrwała między restartami

Decyzja biznesowa (założenie nr 6 z wymagań). Zwiększa bezpieczeństwo: każde uruchomienie aplikacji wymaga uwierzytelnienia. Upraszcza zarządzanie sesją: nie ma konieczności obsługi "stale token" przy starcie.

### Jak ograniczyć zależność od bibliotek zewnętrznych

- `AuthApi` jest interfejsem — implementacja Retrofit może być zastąpiona bez zmian w domain/presentation
- `SessionManager` nie zależy od żadnej biblioteki
- Walidatory w domain są pure Kotlin — bez zależności
- Use case'y operują na modelach domenowych, nie DTO

### Elementy do kolejnych feature'ów

- Biometria (Zasada I Konstytucji — zaplanowane)
- Reset hasła
- Remember me / trwała sesja (wymagałaby EncryptedSharedPreferences)
- Wielosesyjność i zarządzanie urządzeniami
- Notyfikacje push po weryfikacji

---

## 11. Elementy odroczone

| Element | Status | Akcja |
|---|---|---|
| TTL access tokenu (teraz: 900s) | Do potwierdzenia z backendem | Zmiana tylko w konfiguracji — nie hardcodować |
| TTL refresh tokenu (teraz: 28800s) | Do potwierdzenia z backendem | Jw. |
| Dokładny format kodu weryfikacyjnego | Zakładamy 6 cyfr — do potwierdzenia | Validator może wymagać aktualizacji |
| Limit resendów kodu | Nieznany — backend zwraca 429 | Obsłużone przez `AppError.Business(RESEND_LIMIT_EXCEEDED)` |
| Cooldown między resendami | Nieznany | UI może pokazywać odliczanie jeśli backend przekaże `Retry-After` header |
| Polityka blokady konta (liczba prób, czas) | Nieznany | Backend zwraca 423/429 — UI obsłużone |
| Reset hasła | Poza zakresem feature'a | Osobny feature |
| Logowanie biometryczne | Poza zakresem feature'a | Osobny feature (androidx.biometric już w libs.versions.toml) |
| Remember me / trwała sesja | Poza zakresem feature'a | Wymaga EncryptedSharedPreferences — sprzeczne z założeniem nr 6 |
| `android_requirements.md` | Plik nie istnieje | Należy utworzyć jako nadrzędne źródło wymagań biznesowych (zasada XVII) |
| Konfiguracja `network_security_config.xml` | Nie uwzględniona w tym feature'ze | Wymagana przed release (zasada VIII) |

---

## Artifacts

| Plik | Opis |
|---|---|
| `specs/001-user-auth/plan.md` | Ten plik — pełny plan techniczny |
| `specs/001-user-auth/research.md` | Decyzje techniczne, wybór bibliotek |
| `specs/001-user-auth/data-model.md` | Modele domenowe, DTO, UiState |
| `specs/001-user-auth/contracts/api-contract.md` | Pełny kontrakt REST API |
| `specs/001-user-auth/quickstart.md` | Przewodnik developera (deps, struktura) |
