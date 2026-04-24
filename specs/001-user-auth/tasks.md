# Tasks: 001-user-auth — Uwierzytelnianie Użytkownika

**Feature Branch**: `001-user-auth`
**Date**: 2026-04-23
**Input**: specs/001-user-auth/plan.md, spec.md, data-model.md, contracts/api-contract.md, research.md, quickstart.md, .specify/memory/constitution.md, android_requirements.md
**Tests**: Tak — uwzględnione zgodnie z wymaganiami planu (sekcja 9) i poleceniem użytkownika

**Organizacja**: Taski pogrupowane fazami — Setup, Foundational (core), następnie po jednej fazie na user story. Każda faza jest niezależnie testowalnym przyrostem.

---

## Format: `[ID] [P?] [Story?] Description — ścieżka`

- **[P]**: Można wykonać równolegle (różne pliki, brak blokujących zależności)
- **[Story]**: US1/US2/US3/US4 — numer user story z spec.md
- Każde zadanie zawiera dokładną ścieżkę pliku

---

## Phase 1: Setup — Zależności i konfiguracja projektu

**Cel**: Skonfigurowanie projektu Android pod feature 001-user-auth zgodnie z quickstart.md. Bez tej fazy żadna warstwa nie może zostać zaimplementowana.

> ⚠️ Żadna z bibliotek NIE może mieć wersji hardcodowanej w `build.gradle.kts` — wyłącznie przez `libs.versions.toml` (zasada VI Konstytucji).

- [ ] T001 Dodaj sekcje `[versions]` dla: hilt (2.56), navigationCompose (2.9.0), retrofitVersion (2.11.0), okhttpVersion (4.12.0), kotlinxSerializationJson (1.8.1), retrofitKotlinxConverter (1.0.0), lifecycleViewmodelCompose (2.10.0), coroutinesTest (1.10.2), mockk (1.14.2) — plik `gradle/libs.versions.toml`
- [ ] T002 Dodaj sekcje `[libraries]` dla: hilt-android, hilt-compiler, hilt-navigation-compose, androidx-navigation-compose, retrofit, okhttp, okhttp-logging-interceptor, kotlinx-serialization-json, retrofit-kotlinx-serialization-converter, androidx-lifecycle-viewmodel-compose, kotlinx-coroutines-test, mockk — plik `gradle/libs.versions.toml`
- [ ] T003 Dodaj sekcje `[plugins]` dla: hilt i kotlin-serialization — plik `gradle/libs.versions.toml`
- [ ] T004 Dodaj pluginy `alias(libs.plugins.hilt)` i `alias(libs.plugins.kotlin.serialization)` do bloku `plugins {}` — plik `app/build.gradle.kts`
- [ ] T005 Dodaj wszystkie zależności runtime (hilt-android, ksp hilt-compiler, hilt-navigation-compose, navigation-compose, retrofit, okhttp, logging-interceptor, kotlinx-serialization-json, retrofit-kotlinx-serialization-converter, lifecycle-viewmodel-compose) do bloku `dependencies {}` — plik `app/build.gradle.kts`
- [ ] T006 Dodaj zależności testowe (kotlinx-coroutines-test, mockk) do bloku `dependencies {}` — plik `app/build.gradle.kts`
- [ ] T007 Dodaj adnotację `@HiltAndroidApp` do klasy `Application` (utwórz klasę jeśli nie istnieje; zarejestruj w `AndroidManifest.xml`) — plik `app/src/main/java/com/edoreczenia/eDoreczeniApp.kt`
- [ ] T008 Dodaj BuildConfig field `BASE_URL` z wartością placeholder (np. `"https://api.edoreczenia.pl"`) w bloku `buildConfigField` — plik `app/build.gradle.kts`; upewnij się, że `buildConfig = true` jest włączone w bloku `buildFeatures`

**Checkpoint**: Projekt kompiluje się z nowymi zależnościami. Brak błędów Gradle.

---

## Phase 2: Foundational — Warstwa core (blokująca wszystkie user stories)

**Cel**: Implementacja wspólnych komponentów `core/`, na których zależą wszystkie warstwy feature'a auth. Faza musi być ukończona przed jakąkolwiek pracą nad user stories.

> ⚠️ KRYTYCZNE: Żadna user story nie może być rozpoczęta przed ukończeniem tej fazy.

### Pakiety core

- [ ] T009 [P] Utwórz pakiet `com.edoreczenia.core.error` i plik `AppError.kt` — sealed class z wariantami: `Network(message: String)`, `Unauthorized(code: String, message: String)`, `Forbidden(code: String, message: String)`, `SessionExpired`, `Validation(fieldErrors: Map<String, List<String>>, message: String)`, `Business(code: String, message: String)`, `Unknown(throwable: Throwable?)` — plik `app/src/main/java/com/edoreczenia/core/error/AppError.kt`
- [ ] T010 [P] Utwórz `SessionManager.kt` — klasa z adnotacją `@Singleton @Inject constructor`; przechowuje `var session: Session? = null` (in-memory, bez persistencji); udostępnia `val sessionState: MutableStateFlow<AuthSessionState>`; metody: `setSession(user: User, session: Session)`, `clearSession()`, `getAccessToken(): String?`, `getRefreshToken(): String?`; `clearSession()` ustawia `sessionState.value = AuthSessionState.UNAUTHENTICATED` i zeruje oba tokeny — plik `app/src/main/java/com/edoreczenia/core/session/SessionManager.kt`
- [ ] T011 Utwórz `AuthInterceptor.kt` — implementacja `okhttp3.Interceptor`; pobiera `accessToken` z `SessionManager`; jeśli token dostępny, dodaje nagłówek `Authorization: Bearer <token>` do żądania; brak tokenu nie blokuje żądania (publiczne endpointy) — plik `app/src/main/java/com/edoreczenia/core/network/AuthInterceptor.kt`; wymaga T010
- [ ] T012 Utwórz `TokenAuthenticator.kt` — implementacja `okhttp3.Authenticator`; wywoływana przy HTTP 401 dla chronionych endpointów; wykonuje synchroniczne wywołanie `POST /api/v1/auth/refresh` z `refreshToken` pobranym z `SessionManager`; przy sukcesie: aktualizuje `SessionManager` nową sesją i ponawia oryginalne żądanie; przy błędzie: wywołuje `SessionManager.clearSession()` i zwraca `null` — plik `app/src/main/java/com/edoreczenia/core/network/TokenAuthenticator.kt`; wymaga T010; **uwaga**: żeby uniknąć cyklu DI, `SessionManager` jest przekazywany bezpośrednio, bez wstrzykiwania `AuthRepository`
- [ ] T013 Utwórz `NetworkModule.kt` — moduł Hilt `@Module @InstallIn(SingletonComponent::class)`; dostarcza: `OkHttpClient` z `AuthInterceptor`, `TokenAuthenticator` i `HttpLoggingInterceptor`; dostarcza `Retrofit` z `BASE_URL` z `BuildConfig`, konwerterem `kotlinx-serialization` i skonfigurowanym `OkHttpClient`; oznacza `SessionManager` jako `@Singleton` — plik `app/src/main/java/com/edoreczenia/core/network/NetworkModule.kt`; wymaga T011, T012

### Pakiety struktury feature/auth

- [ ] T014 [P] Utwórz docelową strukturę katalogów (pustych pakietów) dla feature auth zgodnie z plan.md sekcja "Project Structure": `feature/auth/data/api`, `feature/auth/data/dto/request`, `feature/auth/data/dto/response`, `feature/auth/data/mapper`, `feature/auth/data/repository`, `feature/auth/di`, `feature/auth/domain/model`, `feature/auth/domain/repository`, `feature/auth/domain/usecase`, `feature/auth/domain/validator`, `feature/auth/presentation/login`, `feature/auth/presentation/registration`, `feature/auth/presentation/verifyemail`, `feature/auth/presentation/navigation` — katalogi w `app/src/main/java/com/edoreczenia/`

**Checkpoint**: Warstwa `core` jest gotowa. Struktura pakietów istnieje. Projekt kompiluje się.

---

## Phase 3: User Story 1 — Logowanie (Priority: P1) 🎯 MVP

**Cel**: Użytkownik z aktywnym, zweryfikowanym kontem może zalogować się i trafić do głównego widoku. Błędne dane → ogólny komunikat. Puste pola → walidacja kliencka.

**Niezależny test**: Uruchomić ekran logowania, podać poprawne dane (FakeAuthRepository w trybie sukces) → oczekiwany efekt `NavigateToMain`. Podać złe dane → oczekiwany `formError`. Puste pola → `usernameError`/`passwordError`.

### Modele domenowe (US1)

- [ ] T015 [P] Utwórz `AccountStatus.kt` — enum class z wariantami: `PENDING_VERIFICATION`, `ACTIVE`, `LOCKED`, `DISABLED` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/model/AccountStatus.kt`
- [ ] T016 [P] Utwórz `User.kt` — data class z polami: `id: String`, `username: String`, `email: String`, `accountStatus: AccountStatus` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/model/User.kt`
- [ ] T017 [P] Utwórz `Session.kt` — data class z polami: `accessToken: String`, `refreshToken: String`, `tokenType: String`, `expiresInSeconds: Int`, `refreshExpiresInSeconds: Int` — **wyłącznie in-memory, bez serializacji do pliku/bazy** — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/model/Session.kt`
- [ ] T018 [P] Utwórz `AuthSessionState.kt` — sealed class z wariantami: `Unauthenticated`, `Authenticating`, `Authenticated(user: User, session: Session)`, `SessionExpired`, `LoggingOut`, `Error(error: AppError)` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/model/AuthSessionState.kt`; wymaga T009, T016, T017
- [ ] T019 [P] Utwórz `AuthResult.kt` — sealed class generyczna `AuthResult<out T>` z wariantami: `Success(data: T)` i `Failure(error: AppError)` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/model/AuthResult.kt`; wymaga T009

### Kontrakt AuthRepository (US1)

- [ ] T020 Utwórz `AuthRepository.kt` — interfejs z metodami suspend: `login(username: String, password: String): AuthResult<Pair<User, Session>>`, `register(deviceName: String, username: String, email: String, password: String, confirmPassword: String): AuthResult<String>`, `verifyEmailCode(username: String, verificationCode: String): AuthResult<Unit>`, `resendVerificationCode(username: String): AuthResult<Unit>`, `logout(refreshToken: String): AuthResult<Unit>`, `refreshSession(refreshToken: String): AuthResult<Session>`, `checkSession(): AuthResult<User>` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/repository/AuthRepository.kt`; wymaga T016, T017, T019

### Walidatory (US1)

- [ ] T021 [P] Utwórz `LoginFormValidator.kt` — klasa z metodą `validate(username: String, password: String): LoginFormValidationResult`; klasa wynikowa zawiera opcjonalne błędy per pole (`usernameError: String?`, `passwordError: String?`) i metodę `isValid`; reguły: username niepuste → OK, puste → błąd "Pole wymagane"; analogicznie password — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/validator/LoginFormValidator.kt`

### Use case'y (US1 — logowanie i sesja)

- [ ] T022 Utwórz `ValidateLoginFormUseCase.kt` — use case delegujący do `LoginFormValidator`; wejście: `username: String`, `password: String`; wyjście: `LoginFormValidationResult` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/usecase/ValidateLoginFormUseCase.kt`; wymaga T021
- [ ] T023 Utwórz `LoginUseCase.kt` — use case suspend; wywołuje `AuthRepository.login()`; przy sukcesie wywołuje `SessionManager.setSession(user, session)` i ustawia stan `AUTHENTICATED`; przy błędzie zwraca `AuthResult.Failure`; **nie** wykonuje walidacji formularza — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/usecase/LoginUseCase.kt`; wymaga T020, T010
- [ ] T024 Utwórz `GetCurrentSessionUseCase.kt` — use case; zwraca aktualny `AuthSessionState` z `SessionManager.sessionState` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/usecase/GetCurrentSessionUseCase.kt`; wymaga T010
- [ ] T025 Utwórz `RefreshSessionUseCase.kt` — use case suspend; pobiera `refreshToken` z `SessionManager`; wywołuje `AuthRepository.refreshSession()`; przy sukcesie aktualizuje session w `SessionManager`; przy błędzie wywołuje `SessionManager.clearSession()` i ustawia `SESSION_EXPIRED` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/usecase/RefreshSessionUseCase.kt`; wymaga T020, T010

### DTO i AuthApi dla logowania (US1)

- [ ] T026 [P] Utwórz `LoginRequestDto.kt` — data class z `@Serializable`: pola `username: String`, `password: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/request/LoginRequestDto.kt`
- [ ] T027 [P] Utwórz `UserDto.kt` — data class z `@Serializable`: pola `id: String`, `username: String`, `email: String`, `accountStatus: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/UserDto.kt`
- [ ] T028 [P] Utwórz `SessionDto.kt` — data class z `@Serializable`: pola `accessToken: String`, `refreshToken: String`, `tokenType: String`, `expiresInSeconds: Int`, `refreshExpiresInSeconds: Int` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/SessionDto.kt`
- [ ] T029 [P] Utwórz `LoginResponseDto.kt` — data class z `@Serializable`: pola `status: String`, `user: UserDto`, `session: SessionDto` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/LoginResponseDto.kt`; wymaga T027, T028
- [ ] T030 [P] Utwórz `ErrorResponseDto.kt` — data class z `@Serializable`: pola `status: String`, `code: String`, `message: String`, `fieldErrors: Map<String, List<String>>? = null` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/ErrorResponseDto.kt`
- [ ] T031 [P] Utwórz `RefreshTokenRequestDto.kt` — data class z `@Serializable`: pole `refreshToken: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/request/RefreshTokenRequestDto.kt`
- [ ] T032 [P] Utwórz `RefreshTokenResponseDto.kt` — data class z `@Serializable`: pola `status: String`, `session: SessionDto` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/RefreshTokenResponseDto.kt`; wymaga T028
- [ ] T033 [P] Utwórz `SessionCheckResponseDto.kt` — data class z `@Serializable`: pola `status: String`, `user: UserDto` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/SessionCheckResponseDto.kt`; wymaga T027
- [ ] T034 Utwórz `AuthApi.kt` — interfejs Retrofit; endpointy zgodne z api-contract.md: `POST /api/v1/auth/login` → `@Body LoginRequestDto`: `LoginResponseDto`; `POST /api/v1/auth/refresh` → `@Body RefreshTokenRequestDto`: `RefreshTokenResponseDto`; `GET /api/v1/auth/session`: `SessionCheckResponseDto` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/api/AuthApi.kt`; wymaga T026–T033; **tylko te endpointy na tym etapie, pozostałe zostaną dodane w US2–US4**

### Mapper (US1 — logowanie i błędy)

- [ ] T035 Utwórz `AuthMapper.kt` — obiekt/klasa z metodami mapującymi: `UserDto → User` (z mapowaniem `accountStatus: String → AccountStatus` z fallbackiem na `ACTIVE` lub dedykowany fallback); `SessionDto → Session`; `LoginResponseDto → Pair<User, Session>`; `RefreshTokenResponseDto → Session`; `SessionCheckResponseDto → User`; `ErrorResponseDto → AppError` (zgodnie z tabelą HTTP → AppError z plan.md sekcja 5); **mapowanie stringowego accountStatus z API na enum AccountStatus** — plik `app/src/main/java/com/edoreczenia/feature/auth/data/mapper/AuthMapper.kt`; wymaga T009, T015–T017, T027–T030

### AuthRepositoryImpl — implementacja dla logowania (US1)

- [ ] T036 Utwórz `AuthRepositoryImpl.kt` — implementacja `AuthRepository`; wstrzykuje `AuthApi` i `AuthMapper`; implementuje na tym etapie metody: `login()`, `refreshSession()`, `checkSession()`; każde wywołanie API owinięte w `try/catch`: `HttpException` → parsuje ciało błędu przez `ErrorResponseDto` → `AppError` (przez `AuthMapper`); `IOException` → `AppError.Network`; inne `Exception` → `AppError.Unknown`; sukces → mapuje DTO przez `AuthMapper` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/repository/AuthRepositoryImpl.kt`; wymaga T020, T034, T035

### FakeAuthRepository — setup dla US1 (dev/test)

- [ ] T037 Utwórz `FakeAuthRepository.kt` — implementacja `AuthRepository`; wszystkie metody domyślnie zwracają predefiniowane dane lub `AuthResult.Failure(AppError.Network(...))`; pola konfiguracyjne: `var loginResult: AuthResult<Pair<User, Session>>` (domyślnie sukces z fake User + Session); `var shouldFailWith: AppError? = null` (gdy ustawione, wszystkie metody zwracają `Failure`); na tym etapie implementuje: `login()`, `refreshSession()`, `checkSession()` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/repository/FakeAuthRepository.kt`; wymaga T020

### Hilt — moduł auth (US1)

- [ ] T038 Utwórz `AuthModule.kt` — moduł Hilt `@Module @InstallIn(SingletonComponent::class)`; dostarcza `AuthApi` przez Retrofit; binduje `AuthRepositoryImpl` jako `AuthRepository` przez `@Binds`; dostarcza wszystkie use case'y dla US1: `LoginUseCase`, `ValidateLoginFormUseCase`, `GetCurrentSessionUseCase`, `RefreshSessionUseCase` — plik `app/src/main/java/com/edoreczenia/feature/auth/di/AuthModule.kt`; wymaga T013, T023–T025, T036
- [ ] T039 Utwórz `FakeAuthModule.kt` — moduł Hilt do użycia w testach i debugowym wariancie buildu; binduje `FakeAuthRepository` jako `AuthRepository`; oznaczony `@TestInstallIn` lub jako moduł `debugImplementation` — plik `app/src/main/java/com/edoreczenia/feature/auth/di/FakeAuthModule.kt`; wymaga T037, T038; **komentarz w pliku wskazuje jak przełączać między real a fake**

### UiState i ViewModel — logowanie (US1)

- [ ] T040 Utwórz `LoginUiState.kt` — data class z polami: `isLoading: Boolean = false`, `usernameInput: String = ""`, `passwordInput: String = ""`, `usernameError: String? = null`, `passwordError: String? = null`, `formError: String? = null`; sealed class `LoginEffect` z wariantami: `NavigateToMain`, `NavigateToRegistration`, `NavigateToVerifyEmail(username: String)`, `ShowMessage(text: String)` — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/login/LoginUiState.kt`; wymaga T009
- [ ] T041 Utwórz `LoginViewModel.kt` — ViewModel z `@HiltViewModel @Inject constructor`; eksponuje `uiState: StateFlow<LoginUiState>` i `effects: Flow<LoginEffect>` (przez `Channel`); metody: `onUsernameChanged(value: String)`, `onPasswordChanged(value: String)`, `onLoginClicked()`, `onRegisterClicked()`, `onRetryClicked()`; `onLoginClicked()`: najpierw `ValidateLoginFormUseCase` → przy błędach walidacji aktualizuje pola error bez wywołania API; przy OK: `isLoading = true`, wywołuje `LoginUseCase`, obsługuje wynik; mapowanie AppError na komunikaty UI: `Unauthorized(INVALID_CREDENTIALS)` → ogólny komunikat (FR-004); `Forbidden(ACCOUNT_NOT_ACTIVE)` → efekt `NavigateToVerifyEmail(username)`; `Business(ACCOUNT_LOCKED)` → `formError`; `Business(TOO_MANY_LOGIN_ATTEMPTS)` → `formError`; `Network` → `formError`; dezaktywacja przycisku przez `isLoading` zapobiega duplikatom żądań (FR-014) — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/login/LoginViewModel.kt`; wymaga T022, T023, T040

### Ekran logowania i nawigacja (US1)

- [ ] T042 Utwórz `LoginScreen.kt` — Composable z `@HiltViewModel`; obserwuje `uiState` przez `collectAsStateWithLifecycle()`; obsługuje `effects` przez `LaunchedEffect`; formularz: pole username, pole password, przycisk "Zaloguj" (zablokowany gdy `isLoading`), przycisk "Zarejestruj się", spinner gdy `isLoading`; walidacja inline: `usernameError`/`passwordError` przy polach; `formError` wyświetlany jako ogólny komunikat pod formularzem; **brak logiki biznesowej w Composable** — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/login/LoginScreen.kt`; wymaga T041; ekran referencyjny: `docs/screens/logowanie/`
- [ ] T043 Utwórz `AuthNavGraph.kt` — zagnieżdżony graf nawigacyjny `NavGraphBuilder.authNavGraph(navController, onNavigateToMain)`; `startDestination = Login`; trasy: `Login`, `Registration`, `VerifyEmail/{username}`; na tym etapie: definicja grafu, trasy, przejście `Login → Registration` i `Login → Main` (`popUpTo(authGraph) { inclusive = true }`); przejście `Login → VerifyEmail` z argumentem `username` — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/navigation/AuthNavGraph.kt`; wymaga T042
- [ ] T044 Podłącz `AuthNavGraph` do głównego grafu nawigacyjnego aplikacji (`MainActivity` lub główny `NavHost`); dodaj logikę startową: sprawdź `SessionManager.sessionState` przy starcie — `UNAUTHENTICATED` → start na `AUTH_GRAPH`, `AUTHENTICATED` → start na `MAIN_GRAPH` — plik `app/src/main/java/com/edoreczenia/MainActivity.kt`; wymaga T043

### Zasoby stringów — logowanie (US1)

- [ ] T045 [P] Dodaj zasoby stringów dla ekranu logowania do `strings.xml`: `error_invalid_credentials`, `error_account_locked`, `error_too_many_attempts`, `error_network`, `error_field_required`, `login_title`, `login_username_label`, `login_password_label`, `login_button`, `login_register_button` — plik `app/src/main/res/values/strings.xml`

### Testy — US1

- [ ] T046 [P] Utwórz `LoginFormValidatorTest.kt` — testy jednostkowe: puste username → błąd; puste password → błąd; oba puste → oba błędy; oba wypełnione → `isValid = true`; graniczne (jeden znak) — plik `app/src/test/java/com/edoreczenia/feature/auth/domain/validator/LoginFormValidatorTest.kt`; wymaga T021
- [ ] T047 [P] Utwórz `LoginUseCaseTest.kt` — testy z MockK i `runTest`: sukces → `SessionManager.setSession()` wywołany, zwraca `Success`; HTTP 401 → `Failure(Unauthorized)`; IOException → `Failure(Network)`; weryfikacja że `SessionManager` NIE jest aktualizowany przy błędzie — plik `app/src/test/java/com/edoreczenia/feature/auth/domain/usecase/LoginUseCaseTest.kt`; wymaga T023, T037
- [ ] T048 [P] Utwórz `AuthMapperTest.kt` — testy mapowania: `LoginResponseDto → Pair<User, Session>`; `ErrorResponseDto(400)` → `AppError.Validation`; `ErrorResponseDto(401, INVALID_CREDENTIALS)` → `AppError.Unauthorized`; `ErrorResponseDto(401, INVALID_REFRESH_TOKEN)` → `AppError.SessionExpired`; nieznany `accountStatus` → fallback; `UserDto` → `User` z poprawnymi polami — plik `app/src/test/java/com/edoreczenia/feature/auth/data/mapper/AuthMapperTest.kt`; wymaga T035
- [ ] T049 [P] Utwórz `LoginViewModelTest.kt` — testy z `TestCoroutineDispatcher` i `runTest`: puste pola → brak wywołania use case; sukces → efekt `NavigateToMain`; błąd sieci → `formError` ustawiony; `isLoading` włącza się przy wywołaniu i wyłącza po odpowiedzi; wielokrotne kliknięcie `onLoginClicked()` przy `isLoading=true` → brak duplikatów żądań — plik `app/src/test/java/com/edoreczenia/feature/auth/presentation/login/LoginViewModelTest.kt`; wymaga T041, T037

**Checkpoint**: Logowanie działa end-to-end. Możliwe użycie z `FakeAuthRepository` bez backendu.

---

## Phase 4: User Story 2 — Rejestracja nowego konta (Priority: P2)

**Cel**: Nowy użytkownik może zarejestrować konto. Po sukcesie trafia na ekran weryfikacji e-mail. Walidacja kliencka dla wszystkich pól. Szczegółowe komunikaty przy konflikcie username/email.

**Niezależny test**: Uruchomić ekran rejestracji, wypełnić wszystkie pola poprawnymi danymi → efekt `NavigateToVerifyEmail`. Podać zajęty username → `usernameError`. Błędny email → `emailError`.

### Modele domenowe (US2)

- [X] T050 [P] Utwórz `VerificationStatus.kt`

### Walidatory (US2)

- [X] T051 [P] Utwórz `RegistrationFormValidator.kt`

### Use case (US2)

- [X] T052 Utwórz `ValidateRegistrationFormUseCase.kt`
- [X] T053 Utwórz `RegisterUserUseCase.kt`

### DTO (US2)

- [ ] T054 [P] Utwórz `RegisterRequestDto.kt` — data class `@Serializable`: `deviceName: String`, `username: String`, `email: String`, `password: String`, `confirmPassword: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/request/RegisterRequestDto.kt`
- [ ] T055 [P] Utwórz `RegisterResponseDto.kt` — data class `@Serializable`: `status: String`, `userId: String`, `username: String`, `email: String`, `verificationRequired: Boolean`, `message: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/RegisterResponseDto.kt`

### Rozszerzenie AuthApi, AuthMapper, AuthRepositoryImpl (US2)

- [ ] T056 Dodaj do `AuthApi.kt` endpoint: `POST /api/v1/auth/register` → `@Body RegisterRequestDto`: `RegisterResponseDto` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/api/AuthApi.kt`; wymaga T054, T055
- [ ] T057 Rozszerz `AuthMapper.kt` o metodę: `RegisterResponseDto → String` (zwraca `userId`) — plik `app/src/main/java/com/edoreczenia/feature/auth/data/mapper/AuthMapper.kt`; wymaga T055
- [ ] T058 Implementuj metodę `register()` w `AuthRepositoryImpl.kt` — analogicznie do `login()`: try/catch, mapowanie błędów, mapowanie sukcesu — plik `app/src/main/java/com/edoreczenia/feature/auth/data/repository/AuthRepositoryImpl.kt`; wymaga T056, T057
- [X] T059 Implementuj metodę `register()` w `FakeAuthRepository.kt`

### Hilt — rozszerzenie modułu (US2)

- [ ] T060 Dodaj do `AuthModule.kt` providery dla: `RegisterUserUseCase`, `ValidateRegistrationFormUseCase` — plik `app/src/main/java/com/edoreczenia/feature/auth/di/AuthModule.kt`; wymaga T052, T053

### UiState i ViewModel — rejestracja (US2)

- [X] T061 Utwórz `RegistrationUiState.kt`
- [X] T062 Utwórz `RegistrationViewModel.kt`

### Ekran rejestracji i nawigacja (US2)

- [X] T063 Utwórz `RegistrationScreen.kt`
- [X] T064 Rozszerz `AuthNavGraph.kt` o trasę Registration z RegistrationScreen

### Zasoby stringów — rejestracja (US2)

- [X] T065 [P] Dodaj zasoby stringów dla ekranu rejestracji do `strings.xml`

### Testy — US2

- [ ] T066 [P] Utwórz `RegistrationFormValidatorTest.kt` — testy graniczne dla każdego pola: `deviceName` 2 znaki → błąd, 3 znaki → OK, 100 znaków → OK, 101 znaków → błąd; `username` 2/3/50/51 znaków; email format (poprawny, bez @, z @, bez domeny); password (7 znaków, 8 znaków, brak wielkiej litery, brak cyfry/znaku specjalnego, spełniający wszystkie warunki); `confirmPassword` niezgodne — plik `app/src/test/java/com/edoreczenia/feature/auth/domain/validator/RegistrationFormValidatorTest.kt`; wymaga T051
- [ ] T067 [P] Utwórz `RegisterUserUseCaseTest.kt` — testy z MockK: sukces → `AuthResult.Success(userId)`; HTTP 409 USERNAME_ALREADY_EXISTS → `Failure(Business(USERNAME_ALREADY_EXISTS))`; HTTP 409 EMAIL_ALREADY_EXISTS → `Failure(Business(EMAIL_ALREADY_EXISTS))`; IOException → `Failure(Network)` — plik `app/src/test/java/com/edoreczenia/feature/auth/domain/usecase/RegisterUserUseCaseTest.kt`; wymaga T053, T037
- [ ] T068 [P] Utwórz `RegistrationViewModelTest.kt` — testy: walidacja pól inline; HTTP 409 username → `usernameError` ustawiony; sukces → efekt `NavigateToVerifyEmail` z poprawnym username i email; dezaktywacja przycisku podczas loading; wielokrotne kliknięcie przy `isLoading=true` → brak duplikatów — plik `app/src/test/java/com/edoreczenia/feature/auth/presentation/registration/RegistrationViewModelTest.kt`; wymaga T062, T037

**Checkpoint**: Rejestracja działa end-to-end. Przejście Login→Registration→VerifyEmail poprawne.

---

## Phase 5: User Story 3 — Weryfikacja konta kodem e-mail (Priority: P3)

**Cel**: Użytkownik po rejestracji wprowadza kod weryfikacyjny. Poprawny kod aktywuje konto i przekierowuje do logowania. Po 3 błędnych próbach kod jest unieważniany. Możliwość resend.

**Niezależny test**: Uruchomić ekran VerifyEmail z fake `username`, podać poprawny kod → efekt `NavigateToLogin(showSuccessMessage=true)`. Błędny kod → `verificationCodeError`. Resend → brak błędu, stan zresetowany.

### Walidatory (US3)

- [ ] T069 [P] Utwórz `VerificationCodeValidator.kt` — klasa z metodą `validate(code: String): VerificationCodeValidationResult`; klasa wynikowa zawiera `codeError: String?` i `isValid: Boolean`; logika: trim kodu (FR-021), kod po trim niepusty → OK; pusty po trim → błąd "Pole wymagane" — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/validator/VerificationCodeValidator.kt`

### Use case'y (US3)

- [ ] T070 Utwórz `ValidateVerificationCodeUseCase.kt` — deleguje do `VerificationCodeValidator`; trim kodu przed delegacją — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/usecase/ValidateVerificationCodeUseCase.kt`; wymaga T069
- [ ] T071 Utwórz `VerifyEmailCodeUseCase.kt` — suspend; wejście: `username: String`, `verificationCode: String` (już po trim); wywołuje `AuthRepository.verifyEmailCode()` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/usecase/VerifyEmailCodeUseCase.kt`; wymaga T020
- [ ] T072 Utwórz `ResendVerificationCodeUseCase.kt` — suspend; wejście: `username: String`; wywołuje `AuthRepository.resendVerificationCode()` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/usecase/ResendVerificationCodeUseCase.kt`; wymaga T020

### DTO (US3)

- [ ] T073 [P] Utwórz `VerifyEmailCodeRequestDto.kt` — data class `@Serializable`: `username: String`, `verificationCode: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/request/VerifyEmailCodeRequestDto.kt`
- [ ] T074 [P] Utwórz `VerifyEmailCodeResponseDto.kt` — data class `@Serializable`: `status: String`, `userId: String`, `accountStatus: String`, `message: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/VerifyEmailCodeResponseDto.kt`
- [ ] T075 [P] Utwórz `ResendVerificationCodeRequestDto.kt` — data class `@Serializable`: `username: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/request/ResendVerificationCodeRequestDto.kt`
- [ ] T076 [P] Utwórz `ResendVerificationCodeResponseDto.kt` — data class `@Serializable`: `status: String`, `message: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/response/ResendVerificationCodeResponseDto.kt`

### Rozszerzenie AuthApi, AuthMapper, AuthRepositoryImpl (US3)

- [ ] T077 Dodaj do `AuthApi.kt` endpointy: `POST /api/v1/auth/verify-email-code` → `@Body VerifyEmailCodeRequestDto`: `VerifyEmailCodeResponseDto`; `POST /api/v1/auth/resend-verification-code` → `@Body ResendVerificationCodeRequestDto`: `ResendVerificationCodeResponseDto` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/api/AuthApi.kt`; wymaga T073–T076
- [ ] T078 Rozszerz `AuthMapper.kt` o metody: `VerifyEmailCodeResponseDto → Unit`; `ResendVerificationCodeResponseDto → Unit` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/mapper/AuthMapper.kt`; wymaga T074, T076
- [ ] T079 Implementuj metody `verifyEmailCode()` i `resendVerificationCode()` w `AuthRepositoryImpl.kt` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/repository/AuthRepositoryImpl.kt`; wymaga T077, T078
- [ ] T080 Implementuj metody `verifyEmailCode()` i `resendVerificationCode()` w `FakeAuthRepository.kt` — domyślnie sukces; pola konfiguracyjne: `var verifyEmailResult`, `var resendResult` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/repository/FakeAuthRepository.kt`; wymaga T037

### Hilt — rozszerzenie modułu (US3)

- [ ] T081 Dodaj do `AuthModule.kt` providery dla: `VerifyEmailCodeUseCase`, `ResendVerificationCodeUseCase`, `ValidateVerificationCodeUseCase` — plik `app/src/main/java/com/edoreczenia/feature/auth/di/AuthModule.kt`; wymaga T070–T072

### UiState i ViewModel — weryfikacja e-mail (US3)

- [ ] T082 Utwórz `VerifyEmailUiState.kt` — data class: `isLoading: Boolean = false`, `username: String = ""` (display-only), `verificationCodeInput: String = ""`, `verificationCodeError: String? = null`, `formError: String? = null`, `canResend: Boolean = true`, `resendCooldownSeconds: Int = 0`; sealed class `VerifyEmailEffect`: `NavigateToLogin(showSuccessMessage: Boolean)`, `ShowMessage(text: String)` — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/verifyemail/VerifyEmailUiState.kt`
- [ ] T083 Utwórz `VerifyEmailViewModel.kt` — `@HiltViewModel @Inject constructor`; eksponuje `uiState: StateFlow<VerifyEmailUiState>` i `effects: Flow<VerifyEmailEffect>`; przyjmuje `username` z `SavedStateHandle`; metody: `onVerificationCodeChanged(value)`, `onVerifyClicked()`, `onResendCodeClicked()`, `onBackClicked()`; `onVerifyClicked()`: trim kodu (FR-021) → `ValidateVerificationCodeUseCase` → przy błędzie: `verificationCodeError`; przy OK: `isLoading = true`, `VerifyEmailCodeUseCase`; mapowanie błędów: `Unauthorized(INVALID_VERIFICATION_CODE)` → `verificationCodeError` z info o pozostałych próbach (FR-018); `Business(VERIFICATION_CODE_EXPIRED)` → `formError` + `canResend = true`; `Business(VERIFICATION_LOCKED)` → `formError`, `canResend = true` (FR-018a); `Business(RESEND_LIMIT_EXCEEDED)` → `formError`, `canResend = false`; `Network` → `formError`; sukces → efekt `NavigateToLogin(showSuccessMessage = true)` (FR-029); `onResendCodeClicked()`: `ResendVerificationCodeUseCase`, przy sukcesie reset stanu kodu — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/verifyemail/VerifyEmailViewModel.kt`; wymaga T070–T072, T082

### Ekran weryfikacji e-mail i nawigacja (US3)

- [ ] T084 Utwórz `VerifyEmailScreen.kt` — Composable; wyświetla `username` (nieedytowalny); pole kodu weryfikacyjnego; `verificationCodeError` przy polu; `formError` jako ogólny komunikat; przycisk "Zweryfikuj" (zablokowany gdy `isLoading`); przycisk "Wyślij kod ponownie" (zablokowany gdy `!canResend`); spinner loading; **brak logiki biznesowej w Composable** — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/verifyemail/VerifyEmailScreen.kt`; wymaga T083; ekran referencyjny: `docs/screens/potwierdzenie_e_mail/`
- [ ] T085 Rozszerz `AuthNavGraph.kt` o: trasę `VerifyEmail/{username}` z argumentem nawigacyjnym `username: String`; kompozable `VerifyEmailScreen`; przejście `Registration → VerifyEmail` (już zdefiniowane w T064); przejście `VerifyEmail → Login` z `popUpTo(VerifyEmail) { inclusive = true }` przy sukcesie; back z `VerifyEmail` → `popBackStack()` do `Login` — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/navigation/AuthNavGraph.kt`; wymaga T084
- [ ] T086 Rozszerz `LoginScreen.kt` o obsługę parametru nawigacyjnego `showSuccessMessage: Boolean` (przekazanego z VerifyEmail); jeśli `true`, wyświetl krótki komunikat (Snackbar lub `ShowMessage` effect) z potwierdzeniem aktywacji konta — plik `app/src/main/java/com/edoreczenia/feature/auth/presentation/login/LoginScreen.kt`; wymaga T085

### Zasoby stringów — weryfikacja (US3)

- [ ] T087 [P] Dodaj zasoby stringów dla ekranu weryfikacji do `strings.xml`: `error_invalid_verification_code`, `error_verification_code_expired`, `error_verification_locked`, `error_resend_limit_exceeded`, `verify_email_title`, `verify_email_code_label`, `verify_email_button`, `verify_email_resend_button`, `verify_email_success_message`, `error_verification_remaining_attempts` — plik `app/src/main/res/values/strings.xml`

### Testy — US3

- [ ] T088 [P] Utwórz `VerificationCodeValidatorTest.kt` — testy: pusty string → błąd; string ze spacjami → po trim pusty → błąd; " " (same spacje) → błąd; "123456" → OK; " 123456 " → OK (trim); "abc" → OK (brak ograniczeń formatu na tym etapie) — plik `app/src/test/java/com/edoreczenia/feature/auth/domain/validator/VerificationCodeValidatorTest.kt`; wymaga T069
- [ ] T089 [P] Utwórz `VerifyEmailCodeUseCaseTest.kt` — testy: sukces → `AuthResult.Success`; HTTP 423 VERIFICATION_LOCKED → `Failure(Business(VERIFICATION_LOCKED))`; HTTP 401 INVALID_VERIFICATION_CODE → `Failure(Unauthorized)`; HTTP 410 VERIFICATION_CODE_EXPIRED → `Failure(Business(VERIFICATION_CODE_EXPIRED))`; HTTP 429 RESEND_LIMIT_EXCEEDED → `Failure(Business(RESEND_LIMIT_EXCEEDED))` — plik `app/src/test/java/com/edoreczenia/feature/auth/domain/usecase/VerifyEmailCodeUseCaseTest.kt`; wymaga T071, T037
- [ ] T090 [P] Utwórz `VerifyEmailViewModelTest.kt` — testy: trim kodu przed wysłaniem (FR-021); VERIFICATION_LOCKED → `canResend = true`, `formError` ustawiony; sukces → efekt `NavigateToLogin(showSuccessMessage = true)`; RESEND_LIMIT_EXCEEDED → `canResend = false`; dezaktywacja przycisku podczas `isLoading` — plik `app/src/test/java/com/edoreczenia/feature/auth/presentation/verifyemail/VerifyEmailViewModelTest.kt`; wymaga T083, T037

**Checkpoint**: Pełny flow rejestracji: Login → Registration → VerifyEmail → Login działa end-to-end.

---

## Phase 6: User Story 4 — Wylogowanie (Priority: P4)

**Cel**: Zalogowany użytkownik może wylogować się. Sesja jest czyszczona. Użytkownik trafia na Login. Wygaśnięcie sesji → automatyczne wylogowanie z komunikatem.

**Niezależny test**: Wywołać `LogoutUseCase` → `SessionManager.clearSession()` wywołany, stan `UNAUTHENTICATED`, nawigacja na Login.

### Use case (US4)

- [ ] T091 Utwórz `LogoutUseCase.kt` — suspend; pobiera `refreshToken` z `SessionManager`; wywołuje `AuthRepository.logout(refreshToken)`; wywołuje `SessionManager.clearSession()` **niezależnie od wyniku API** (local-first logout — FR-025); zwraca `AuthResult<Unit>` — plik `app/src/main/java/com/edoreczenia/feature/auth/domain/usecase/LogoutUseCase.kt`; wymaga T020, T010

### DTO (US4)

- [ ] T092 [P] Utwórz `LogoutRequestDto.kt` — data class `@Serializable`: `refreshToken: String` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/dto/request/LogoutRequestDto.kt`

### Rozszerzenie AuthApi, AuthMapper, AuthRepositoryImpl (US4)

- [ ] T093 Dodaj do `AuthApi.kt` endpoint: `POST /api/v1/auth/logout` → `@Body LogoutRequestDto`: `Unit` (HTTP 200) — plik `app/src/main/java/com/edoreczenia/feature/auth/data/api/AuthApi.kt`; wymaga T092
- [ ] T094 Implementuj metodę `logout()` w `AuthRepositoryImpl.kt` — plik `app/src/main/java/com/edoreczenia/feature/auth/data/repository/AuthRepositoryImpl.kt`; wymaga T093
- [ ] T095 Implementuj metodę `logout()` w `FakeAuthRepository.kt` — domyślnie sukces — plik `app/src/main/java/com/edoreczenia/feature/auth/data/repository/FakeAuthRepository.kt`; wymaga T037

### Hilt — rozszerzenie modułu (US4)

- [ ] T096 Dodaj do `AuthModule.kt` provider dla `LogoutUseCase` — plik `app/src/main/java/com/edoreczenia/feature/auth/di/AuthModule.kt`; wymaga T091

### Nawigacja i obsługa sesji (US4)

- [ ] T097 Dodaj do `MainActivity.kt` (lub dedykowanego `AppViewModel.kt`) obserwację `SessionManager.sessionState` przez `collectAsStateWithLifecycle()`; gdy stan przejdzie w `UNAUTHENTICATED` lub `SESSION_EXPIRED` → nawiguj do `AUTH_GRAPH` z `popUpTo(MAIN_GRAPH) { inclusive = true }`; przy `SESSION_EXPIRED` wyświetl komunikat "Sesja wygasła" — plik `app/src/main/java/com/edoreczenia/MainActivity.kt`; wymaga T044, T091
- [ ] T098 Dodaj przycisk wylogowania (lub stub menu) dostępny z głównego widoku aplikacji (np. `MainScreen` lub pasek narzędzi); wylogowanie wywołuje `LogoutUseCase` przez dedykowany ViewModel lub przez metodę dostępną w bieżącym grafie nawigacyjnym — **stub/placeholder dla przyszłego ekranu Settings**; wymaga T091, T097

### Zasoby stringów — wylogowanie (US4)

- [ ] T099 [P] Dodaj zasoby stringów: `session_expired_message`, `logout_button`, `logout_success_message` — plik `app/src/main/res/values/strings.xml`

### Testy — US4

- [ ] T100 [P] Utwórz `LogoutUseCaseTest.kt` — testy: sukces API → `SessionManager.clearSession()` wywołany; błąd API (IOException) → `SessionManager.clearSession()` **nadal** wywołany (local-first); weryfikacja że stan sesji = `UNAUTHENTICATED` po logout; `refreshToken` null → graceful handling — plik `app/src/test/java/com/edoreczenia/feature/auth/domain/usecase/LogoutUseCaseTest.kt`; wymaga T091, T037
- [ ] T101 [P] Utwórz `RefreshSessionUseCaseTest.kt` — testy: sukces → session zaktualizowana w `SessionManager`; błąd (INVALID_REFRESH_TOKEN) → `SessionManager.clearSession()`, stan = `SESSION_EXPIRED` — plik `app/src/test/java/com/edoreczenia/feature/auth/domain/usecase/RefreshSessionUseCaseTest.kt`; wymaga T025, T037

**Checkpoint**: Pełny cykl auth (Login, Register, VerifyEmail, Logout) działa end-to-end. Sesja in-memory, brak persistencji między restartami.

---

## Phase 7: Testy integracyjne warstwy data

**Cel**: Weryfikacja `AuthRepositoryImpl` z rzeczywistym HTTP (MockWebServer), weryfikacja `SessionManager` i `FakeAuthRepository`.

- [ ] T102 [P] Utwórz `AuthRepositoryImplTest.kt` — testy integracyjne z `MockWebServer` (OkHttp); przypadki: HTTP 200 login → sukces z poprawnym `User` i `Session`; HTTP 401 INVALID_CREDENTIALS → `AppError.Unauthorized`; HTTP 409 USERNAME_ALREADY_EXISTS → `AppError.Business`; HTTP 401 INVALID_REFRESH_TOKEN → `AppError.SessionExpired`; IOException (brak sieci) → `AppError.Network`; malformed JSON → `AppError.Unknown` — plik `app/src/test/java/com/edoreczenia/feature/auth/data/repository/AuthRepositoryImplTest.kt`; wymaga T036, T035
- [ ] T103 [P] Utwórz `SessionManagerTest.kt` — testy jednostkowe: `setSession()` → `sessionState = AUTHENTICATED`, `getAccessToken()` zwraca token; `clearSession()` → `sessionState = UNAUTHENTICATED`, oba tokeny null; wielokrotne `clearSession()` → idempotentne — plik `app/src/test/java/com/edoreczenia/core/session/SessionManagerTest.kt`; wymaga T010
- [ ] T104 [P] Utwórz `FakeAuthRepositoryTest.kt` — testy weryfikujące konfigurowalność fake: `shouldFailWith = AppError.Network` → wszystkie metody zwracają `Failure`; reset `shouldFailWith = null` → metody wracają do domyślnych wyników; domyślne wyniki są spójne z kontraktem — plik `app/src/test/java/com/edoreczenia/feature/auth/data/repository/FakeAuthRepositoryTest.kt`; wymaga T037

---

## Phase 8: Polish & Finalizacja feature'a 001-user-auth

**Cel**: Weryfikacja zgodności z Konstytucją, plan.md i api-contract.md. Drobne poprawki. Feature gotowy do code review.

- [ ] T105 [P] Sprawdź, czy `SessionManager` nie persystuje danych — weryfikacja że brak importów `SharedPreferences`, `DataStore`, `Room` w `SessionManager.kt` i `AuthRepositoryImpl.kt`; weryfikacja że hasło nigdy nie jest przechowywane (grep po `password` w klasach `core/session` i `data/repository`) — bez zmiany kodu jeśli zgodne
- [ ] T106 [P] Sprawdź, czy żaden Composable w `feature/auth/presentation/` nie zawiera bezpośrednich wywołań use case'ów ani repozytoriów (weryfikacja zasady VII Konstytucji i zasady "brak logiki biznesowej w Composable") — bez zmiany kodu jeśli zgodne
- [ ] T107 [P] Weryfikacja zgodności endpointów i kształtu DTO z `contracts/api-contract.md`: porównaj pola każdego DTO request/response z kontraktem; porównaj ścieżki URL i metody HTTP — popraw ewentualne rozbieżności
- [ ] T108 [P] Upewnij się, że wszystkie wersje bibliotek w `app/build.gradle.kts` są referencjami do `libs.versions.toml` (brak hardcodowanych wersji — zasada VI Konstytucji); weryfikacja `BASE_URL` przez `BuildConfig`
- [ ] T109 [P] Przejrzyj `AuthModule.kt` i `FakeAuthModule.kt` — dodaj komentarze wyjaśniające kiedy i jak używać `FakeAuthModule`; upewnij się, że developer może łatwo przełączyć się na fake bez modyfikacji kodu produkcyjnego
- [ ] T110 Utwórz lub zaktualizuj `README.md` w katalogu `specs/001-user-auth/` lub dodaj sekcję do głównego `README.md` z krótką instrukcją jak uruchomić feature auth z `FakeAuthRepository` (krok po kroku dla nowego developera) — plik `specs/001-user-auth/README.md`
- [ ] T111 Uruchom wszystkie testy jednostkowe feature'a auth (`./gradlew :app:test`) — zweryfikuj że wszystkie przechodzą; napraw ewentualne problemy z konfiguracją testową (TestCoroutineDispatcher, reguły Hilt w testach)
- [ ] T112 Wykonaj build debug (`./gradlew :app:assembleDebug`) — zweryfikuj brak błędów kompilacji, ostrzeżeń Hilt, błędów Kapt/KSP

---

## Zależności między fazami

### Kolejność faz

```
Phase 1 (Setup)
    ↓
Phase 2 (Foundational — core)
    ↓
Phase 3 (US1 — Login)      ← MVP — można dostarczyć niezależnie
    ↓
Phase 4 (US2 — Registration)
    ↓
Phase 5 (US3 — VerifyEmail)
    ↓
Phase 6 (US4 — Logout)
    ↓
Phase 7 (Integration Tests)
    ↓
Phase 8 (Polish & Finalizacja)
```

### Zależności user stories

| User Story | Zależność | Uzasadnienie |
|---|---|---|
| US1 (Login) | Phase 2 gotowa | Wymaga `AppError`, `SessionManager`, `NetworkModule` |
| US2 (Registration) | US1 gotowa | Korzysta z `AuthRepository`, `AuthMapper`, `AuthApi` z US1 |
| US3 (VerifyEmail) | US2 gotowa | Flow: Register → VerifyEmail w nawigacji |
| US4 (Logout) | US1 gotowa | Korzysta z `SessionManager` i `AuthRepository` z US1 |
| Phase 7 (Integration Tests) | US1–US4 gotowe | Testuje pełną warstwę data |
| Phase 8 (Polish) | Phase 7 gotowa | Weryfikacja i finalizacja |

### Kluczowe zależności wewnątrz US1

```
T009 (AppError) ──────────────────────────────────► T018, T019, T035
T010 (SessionManager) ────────────────────────────► T011, T012, T023, T024, T025
T011 (AuthInterceptor) + T012 (TokenAuthenticator) ► T013 (NetworkModule)
T015–T019 (modele domenowe) ──────────────────────► T020 (AuthRepository kontrakt)
T020 (AuthRepository) ───────────────────────────► T022–T025 (use case'y)
T026–T033 (DTO) ─────────────────────────────────► T034 (AuthApi)
T034 (AuthApi) + T035 (Mapper) ──────────────────► T036 (AuthRepositoryImpl)
T036 (AuthRepositoryImpl) ───────────────────────► T038 (AuthModule)
T022–T025 (use case'y) ──────────────────────────► T041 (LoginViewModel)
T041 (LoginViewModel) ───────────────────────────► T042 (LoginScreen)
T042 (LoginScreen) ──────────────────────────────► T043 (AuthNavGraph)
```

### Możliwości równoległe

Wszystkie taski oznaczone `[P]` mogą być wykonywane jednocześnie przez różnych developerów, o ile ich zależności są spełnione:

- **Phase 2**: T009 i T010 można zacząć równolegle (różne pliki, brak zależności między nimi)
- **US1 — modele domenowe**: T015, T016, T017 można tworzyć równolegle (różne pliki)
- **US1 — DTO**: T026–T033 można tworzyć równolegle (różne pliki DTO)
- **US1 — testy**: T046, T047, T048, T049 można uruchomić równolegle po zaimplementowaniu zależności
- **US2 — DTO**: T054, T055 równolegle
- **US3 — DTO**: T073–T076 równolegle
- **Zasoby stringów**: T045, T065, T087, T099 mogą być dodawane równolegle w trakcie implementacji ekranów

---

## Przykład MVP: tylko US1 (Phase 1 + 2 + 3)

Aby dostarczyć działające logowanie bez rejestracji i weryfikacji:

1. Ukończ Phase 1 (T001–T008)
2. Ukończ Phase 2 (T009–T014)
3. Ukończ Phase 3 (T015–T049)

Rezultat: działający ekran logowania z `FakeAuthRepository`, walidacja kliencka, obsługa błędów, nawigacja Login→Main.

---

## Podsumowanie

| Metryka | Wartość |
|---|---|
| Łączna liczba tasków | 112 |
| Faza 1 (Setup) | 8 tasków |
| Faza 2 (Foundational) | 6 tasków |
| US1 (Login) | 35 tasków |
| US2 (Registration) | 19 tasków |
| US3 (VerifyEmail) | 22 tasków |
| US4 (Logout) | 12 tasków |
| Phase 7 (Integration Tests) | 3 taski |
| Phase 8 (Polish) | 8 tasków |
| Taski oznaczone [P] | ~45 tasków |

### Weryfikacja zgodności z Konstytucją

| Zasada | Status |
|---|---|
| Architektura warstwowa (presentation/domain/data/core) | ✅ — każda warstwa ma dedykowane taski |
| Brak Room dla auth | ✅ — żaden task nie wprowadza Room |
| Brak lokalnego przechowywania hasła | ✅ — T105 weryfikuje |
| Brak lokalnego przechowywania kodu weryfikacyjnego | ✅ — żaden task nie wprowadza persistencji kodu |
| SessionManager in-memory | ✅ — T010, T103 |
| Backend jako źródło prawdy | ✅ — wszystkie operacje przez `AuthRepository` → API |
| FakeAuthRepository od początku | ✅ — T037, T039, implementowany razem z real repo |
| Zgodność z api-contract.md | ✅ — DTO oparte dokładnie na kontrakcie; T107 weryfikuje |
| Wersje bibliotek przez libs.versions.toml | ✅ — T001–T006, T108 weryfikuje |

