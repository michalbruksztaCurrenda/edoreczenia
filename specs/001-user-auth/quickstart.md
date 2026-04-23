# Quickstart: 001-user-auth

**Feature Branch**: `001-user-auth`
**Date**: 2026-04-23

## Cel

Przewodnik szybkiego startu dla programisty implementującego moduł uwierzytelniania.

---

## Wymagania wstępne

- Android Studio Meerkat (lub nowszy)
- JDK 17
- Projekt skonfigurowany pod `minSdk 34`, `compileSdk 36`
- Branch: `001-user-auth`

---

## Krok 1: Dodaj wymagane zależności

Dodaj do `gradle/libs.versions.toml`:

```toml
[versions]
# ... istniejące ...
hilt = "2.56"
navigationCompose = "2.9.0"
retrofitVersion = "2.11.0"
okhttpVersion = "4.12.0"
kotlinxSerializationJson = "1.8.1"
retrofitKotlinxConverter = "1.0.0"
lifecycleViewmodelCompose = "2.10.0"
coroutinesTest = "1.10.2"
mockk = "1.14.2"

[libraries]
# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Retrofit + OkHttp
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofitVersion" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttpVersion" }
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttpVersion" }

# Kotlinx Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
retrofit-kotlinx-serialization-converter = { group = "com.jakewharton.retrofit", name = "retrofit2-kotlinx-serialization-converter", version.ref = "retrofitKotlinxConverter" }

# ViewModel Compose
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }

# Test
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }

[plugins]
# ... istniejące ...
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

---

## Krok 2: Skonfiguruj app/build.gradle.kts

Dodaj pluginy:
```kotlin
plugins {
    // ... istniejące ...
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}
```

Dodaj zależności:
```kotlin
dependencies {
    // ... istniejące ...
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
```

---

## Krok 3: Struktura pakietów do utworzenia

```
com.edoreczenia/
├── core/
│   ├── error/
│   │   └── AppError.kt
│   ├── network/
│   │   ├── NetworkModule.kt          (Hilt module)
│   │   └── AuthInterceptor.kt
│   └── session/
│       └── SessionManager.kt
├── feature/
│   └── auth/
│       ├── data/
│       │   ├── api/
│       │   │   └── AuthApi.kt
│       │   ├── dto/
│       │   │   ├── request/          (DTO requestów)
│       │   │   └── response/         (DTO odpowiedzi)
│       │   ├── mapper/
│       │   │   └── AuthMapper.kt
│       │   └── repository/
│       │       ├── AuthRepositoryImpl.kt
│       │       └── FakeAuthRepository.kt
│       ├── domain/
│       │   ├── model/
│       │   │   ├── User.kt
│       │   │   ├── Session.kt
│       │   │   ├── AccountStatus.kt
│       │   │   ├── AuthSessionState.kt
│       │   │   └── AuthResult.kt
│       │   ├── repository/
│       │   │   └── AuthRepository.kt  (interfejs)
│       │   ├── usecase/
│       │   │   ├── LoginUseCase.kt
│       │   │   ├── RegisterUserUseCase.kt
│       │   │   ├── VerifyEmailCodeUseCase.kt
│       │   │   ├── ResendVerificationCodeUseCase.kt
│       │   │   ├── LogoutUseCase.kt
│       │   │   ├── GetCurrentSessionUseCase.kt
│       │   │   ├── RefreshSessionUseCase.kt
│       │   │   ├── ValidateLoginFormUseCase.kt
│       │   │   ├── ValidateRegistrationFormUseCase.kt
│       │   │   └── ValidateVerificationCodeUseCase.kt
│       │   └── validator/
│       │       ├── LoginFormValidator.kt
│       │       ├── RegistrationFormValidator.kt
│       │       └── VerificationCodeValidator.kt
│       └── presentation/
│           ├── login/
│           │   ├── LoginScreen.kt
│           │   ├── LoginViewModel.kt
│           │   └── LoginUiState.kt
│           ├── registration/
│           │   ├── RegistrationScreen.kt
│           │   ├── RegistrationViewModel.kt
│           │   └── RegistrationUiState.kt
│           └── verifyemail/
│               ├── VerifyEmailScreen.kt
│               ├── VerifyEmailViewModel.kt
│               └── VerifyEmailUiState.kt
```

---

## Krok 4: Uruchomienie z Fake Repository (development/test)

Skonfiguruj moduł Hilt tak, aby w buildzie `debug` lub testach wstrzykiwał `FakeAuthRepository` zamiast `AuthRepositoryImpl`. Użyj `@TestInstallIn` w testach lub osobnego modułu Hilt dla `debugImplementation`.

---

## Dokumentacja

- Specyfikacja: `specs/001-user-auth/spec.md`
- Plan: `specs/001-user-auth/plan.md`
- Model danych: `specs/001-user-auth/data-model.md`
- Kontrakt API: `specs/001-user-auth/contracts/api-contract.md`
- Research: `specs/001-user-auth/research.md`

