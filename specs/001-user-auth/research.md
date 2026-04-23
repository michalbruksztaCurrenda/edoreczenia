# Research: 001-user-auth

**Feature Branch**: `001-user-auth`
**Date**: 2026-04-23
**Status**: Complete — wszystkie NEEDS CLARIFICATION rozwiązane

---

## 1. Zarządzanie sesjami w aplikacjach Android (in-memory, bez trwałości między restartami)

**Decision**: Tokeny sesji (`accessToken`, `refreshToken`) przechowywane wyłącznie w pamięci operacyjnej aplikacji (`SessionManager` jako singleton wstrzykiwany przez Hilt z `@Singleton`). Nie używamy `EncryptedSharedPreferences` ani `DataStore` do persistencji tokenów między uruchomieniami — zgodnie z założeniem architektonicznym nr 7.

**Rationale**: Wymaganie biznesowe mówi wprost: sesja NIE przetrwa między uruchomieniami aplikacji. Przechowywanie tokenów w pamięci procesowej jest wystarczające i bezpieczne — po kill procesu dane znikają automatycznie. Nie ma potrzeby jawnego czyszczenia przy starcie.

**Alternatives considered**:
- `EncryptedSharedPreferences` — odrzucone, ponieważ daje trwałość między restartami, której spec zabrania dla tego feature'a.
- `DataStore (Preferences)` — odrzucone z tego samego powodu.
- Android Keystore — odrzucone dla tokenów sesji w tym feature'ze; może być użyte dla certyfikatów w przyszłości.

---

## 2. Biblioteka HTTP: Retrofit + OkHttp

**Decision**: Retrofit 2.x (najnowsza stabilna) jako klient HTTP; OkHttp 4.x jako silnik transportu + interceptor do dodawania nagłówka `Authorization: Bearer <token>` oraz interceptor do odświeżania tokenów (Authenticator).

**Rationale**: Retrofit jest de facto standardem na Androidzie dla komunikacji REST. Integruje się naturalnie z Kotlin Coroutines (`suspend fun`). Umożliwia czyste oddzielenie warstwy `data` od `domain`. Owijamy go w wewnętrzne abstrakcje (zasada II).

**Alternatives considered**:
- Ktor Client — dojrzały, ale mniej spread na Androidzie; odrzucone ze względu na prostszą ścieżkę migracji Retrofit.
- Volley — przestarzałe; odrzucone.

---

## 3. Dependency Injection: Hilt

**Decision**: Hilt (oparty na Dagger 2) jako framework DI zgodnie z zasadą XIII Konstytucji.

**Rationale**: Hilt jest oficjalnym rozwiązaniem DI dla Androida zalecanym przez Google. Integruje się bezpośrednio z cyklem życia Activity/Fragment/ViewModel. Wymagany do testowalności — łatwy do zastąpienia fake'ami w testach (`@TestInstallIn`).

**Alternatives considered**:
- Koin — lżejszy, ale słabsza walidacja w compile-time; odrzucone.
- Ręczne DI — nieakceptowalne przy tej skali projektu.

---

## 4. Nawigacja: Jetpack Navigation Compose

**Decision**: Jetpack Navigation Compose jako system nawigacji. Ekrany auth tworzą osobny zagnieżdżony graf nawigacyjny (`authNavGraph`), oddzielony od grafu głównej aplikacji.

**Rationale**: Natywna integracja z Compose. Bezpieczna nawigacja przez `popUpTo` z `inclusive=true` eliminuje powrót do ekranów auth po zalogowaniu. Zagnieżdżony graf auth izoluje przepływ uwierzytelniania.

**Alternatives considered**:
- Compose Destinations (biblioteka) — upraszcza type-safe navigation, ale dodaje zewnętrzną zależność; odłożone do oceny w innym feature'ze.
- Custom back stack — nadmiernie skomplikowane; odrzucone.

---

## 5. Wzorzec ViewModel + UiState (MVI-lite)

**Decision**: Każdy ekran auth ma dedykowany `ViewModel` zarządzający `UiState` (sealed class/data class) eksponowanym przez `StateFlow`. Zdarzenia użytkownika jako metody (`onLoginClicked()`) lub sealed class `UserAction`. Efekty jednorazowe (nawigacja, toast) przez `Channel<Effect>` + `receiveAsFlow()`.

**Rationale**: Jednokierunkowy przepływ danych (UDF) zgodny z zasadami Compose i Konstytucją (zasada VII — 5 stanów). `StateFlow` gwarantuje bezpieczny dostęp z UI. `Channel` dla efektów unika duplikowania przy rekompozycji.

**Alternatives considered**:
- `SharedFlow` dla efektów — możliwe, ale `Channel` jest bezpieczniejszy dla one-shot events (nawigacja).
- Pełna architektura MVI z Intent/Model — nadmiarowa złożoność dla tego zakresu.

---

## 6. Serializacja JSON: Kotlinx Serialization / Gson / Moshi

**Decision**: Kotlinx Serialization (`kotlinx-serialization-json`) z adapterem dla Retrofit (`retrofit2-kotlinx-serialization-converter`).

**Rationale**: Kotlin-native, null-safe, działa w compile-time, nie wymaga refleksji. Spójne z podejściem Kotlin-first projektu.

**Alternatives considered**:
- Gson — mutable fields, refleksja, brak null safety z Kotlin; odrzucone.
- Moshi — dobra alternatywa, ale generuje boilerplate z adapterami; odrzucone na rzecz Kotlinx.

---

## 7. Odświeżanie tokenów (Token Refresh Strategy)

**Decision**: OkHttp `Authenticator` (triggowany przy HTTP 401) jako mechanizm odświeżania tokenów. Jeśli refresh się nie uda (401/403), `SessionManager` czyści sesję i emituje sygnał `SESSION_EXPIRED` obsługiwany globalnie.

**Rationale**: `Authenticator` jest wbudowanym mechanizmem OkHttp przeznaczonym dokładnie do tego celu. Centralny `SessionManager` jest single source of truth dla stanu sesji. Inne warstwy (szczególnie `presentation`) subskrybują `sessionState: StateFlow<AuthSessionState>`.

**Alternatives considered**:
- OkHttp Interceptor zamiast Authenticator — Interceptor działa przed wysłaniem żądania, Authenticator po 401; Authenticator jest właściwszym miejscem.

---

## 8. Strategia testów i fake repository

**Decision**: Testy jednostkowe (`JUnit 4` + `Mockito` lub `MockK`) dla use case'ów, walidatorów, maperów. Testy ViewModeli z `TestCoroutineDispatcher` / `runTest`. Fake repository (`FakeAuthRepository`) implementujący kontrakt `AuthRepository` z domeną, zwracający predefiniowane odpowiedzi — bez zewnętrznych zależności.

**Rationale**: Fake repository jest deterministyczny, szybki, przenośny i nie wymaga running serwera ani bazy danych. Umożliwia testowanie wszystkich ścieżek (sukces, błąd, timeout) przez proste ustawienie `fake.shouldFail = true`. Brak Room dla auth eliminuje konieczność in-memory bazy SQLite w testach.

**Alternatives considered**:
- MockWebServer (OkHttp) — realistyczniejszy dla testów integracyjnych HTTP, ale nadmiarowy dla unit testów use case'ów.
- Room in-memory — nieadekwatne, ponieważ auth nie używa Room.

---

## 9. Biblioteki do dodania do libs.versions.toml (wymagane dla tego feature'a)

| Biblioteka | Powód |
|---|---|
| `hilt-android`, `hilt-compiler`, `hilt-navigation-compose` | DI (zasada XIII Konstytucji) |
| `retrofit`, `okhttp`, `logging-interceptor` | Komunikacja HTTP z backendem |
| `kotlinx-serialization-json`, `retrofit2-kotlinx-serialization-converter` | Serializacja JSON |
| `androidx-navigation-compose` | Nawigacja między ekranami |
| `lifecycle-viewmodel-compose` | ViewModel w Compose |
| `mockk` lub `mockito-kotlin` | Mocking w testach jednostkowych |
| `kotlinx-coroutines-test` | Testy coroutines |

> **UWAGA**: Żadna z tych bibliotek NIE może mieć wersji hardcodowanej w `build.gradle.kts` — wyłącznie przez `libs.versions.toml` (zasada VI Konstytucji).

---

## 10. Rozwiązane NEEDS CLARIFICATION

| Pytanie | Odpowiedź (z clarifications w spec.md) |
|---|---|
| Polityka haseł? | Min. 8 znaków + min. 1 duża litera + min. 1 cyfra lub znak specjalny |
| Nawigacja po weryfikacji? | Ekran logowania; brak auto-login |
| Trwałość sesji? | In-memory; wygasa przy zamknięciu aplikacji |
| Wielokrotny błędny kod? | Po 3 próbach kod unieważniony; wymagany resend |
| Polityka komunikatów błędów? | Logowanie: ogólne; rejestracja: szczegółowe dla konfliktu username/email |
| android_requirements.md? | Plik nie istnieje — wymagania pobrane z spec.md + Konstytucji |

