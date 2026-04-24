# Implementation Plan: 002-inbox-odebrane — Ekran Odebrane (Inbox)

**Branch**: `002-inbox-odebrane` | **Date**: 2026-04-24 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-inbox-odebrane/spec.md`

---

## Summary

Implementacja ekranu Odebrane — pierwszego ekranu wyświetlanego po poprawnym zalogowaniu w aplikacji e-Komornik. Ekran prezentuje listę odebranych przesyłek/korespondencji elektronicznej na podstawie danych mockowych (`FakeInboxRepository`). Architektura warstwowa (presentation / domain / data / core), bez Hilt, bez Room, UI w Jetpack Compose z Material 3. Obsługa pięciu stanów UI zgodnie z zasadą VII Konstytucji, filtrowanie, wyszukiwanie, oznaczanie gwiazdką, pull-to-refresh i przygotowany punkt nawigacyjny do przyszłego ekranu szczegółów.

---

## Technical Context

**Language/Version**: Kotlin 2.3.20
**Primary Dependencies**: Jetpack Compose BOM 2026.03.00, Material 3 (via BOM), Navigation Compose 2.9.0, Lifecycle ViewModel Compose 2.10.0, Core KTX 1.18.0
**Storage**: Brak Room — dane mockowe in-memory (`FakeInboxRepository`). Stan gwiazdki wyłącznie w pamięci ViewModel. Brak DataStore dla tego feature'a.
**Testing**: JUnit 4, MockK 1.14, kotlinx-coroutines-test 1.10, FakeInboxRepository
**Target Platform**: Android, minSdk 34, targetSdk/compileSdk 36, JVM 17
**Project Type**: Native Android Mobile App — ekran listy (inbox) wewnątrz istniejącej aplikacji
**Performance Goals**: Ekran Odebrane dostępny w < 1s od zalogowania (SC-001). Płynne przewijanie listy bez degradacji wydajności (NFR-008).
**Constraints**: Bez Hilt na tym etapie — DI ręczne lub factory. Bez Room. Bez backendowego API. 4 statyczne rekordy mockowe, brak paginacji. Stan gwiazdki tylko w pamięci.
**Scale/Scope**: 1 ekran główny (InboxScreen), 2 use case'y, 1 fake repository, 4 mockowe rekordy, 3 filtry, pull-to-refresh.

---

## Constitution Check

| Zasada | Status | Uzasadnienie |
|---|---|---|
| III. Architektura warstwowa | ✅ PASS | Moduł podzielony na presentation / domain / data / core |
| IV. Modularność feature'ów | ✅ PASS | Osobny pakiet `feature/inbox` z wewnętrznym podziałem na warstwy |
| VI. Zakaz hardcodowania | ✅ PASS | Teksty UI w `strings.xml`, kolory w `Color.kt` / Theme, mockowe dane jako stałe w osobnej klasie |
| VII. Obsługa błędów — 5 stanów | ✅ PASS | InboxUiState obsługuje Initial / Loading / Success / Empty / Error |
| VIII. Bezpieczeństwo danych | ✅ PASS | Brak wrażliwych danych w tym feature'ze; mock data nie zawiera PII w sensie runtime |
| IX. Dane lokalne | ✅ PASS | Brak Room — uzasadnione: dane mockowe, brak wymagania persystencji w tym etapie |
| X. Komunikacja sieciowa | ✅ PASS | Brak wywołań sieciowych w tym feature'ze (FakeRepository); gotowość na podmianę |
| XIII. Testowalność | ✅ PASS | FakeInboxRepository, testowalne use case'y, testowalne ViewModele przez coroutines-test |
| XIV. Czytelność | ✅ PASS | Nazewnictwo domenowe: InboxMessage, InboxFilter, InboxUiState |
| XX. Zakazy bezwzględne | ✅ PASS | Brak wywołań sieciowych z presentation, brak surowych wyjątków do UI, brak logiki w Composable |

> **Uwaga dotycząca Hilt (zasada XIII)**: Konstytucja wymaga Hilt dla DI. W tym feature'ze Hilt jest celowo wyłączony (NFR-006 spec — decyzja projektowa dla bieżącej fazy). Zależności dostarczane ręcznie przez `InboxViewModelFactory`. Jest to udokumentowany wyjątek tymczasowy, do usunięcia przy podłączaniu API.

**GATE: PASS** — brak naruszeń Konstytucji nieuzasadnionych decyzją projektową.

---

## Project Structure

### Documentation (this feature)

```text
specs/002-inbox-odebrane/
├── plan.md              # Ten plik
├── research.md          # Phase 0 — decyzje techniczne
├── data-model.md        # Phase 1 — modele i DTO
├── quickstart.md        # Phase 1 — przewodnik developera
├── contracts/
│   └── navigation-contract.md  # Phase 1 — kontrakt nawigacyjny ekranu
└── tasks.md             # Phase 2 — do wygenerowania przez /speckit.tasks
```

### Source Code (repository root)

```text
app/src/main/java/com/edoreczenia/
├── core/
│   ├── error/
│   │   └── AppError.kt                         # sealed class (istniejące z 001-user-auth)
│   └── ui/
│       └── theme/                              # Material 3 Theme (istniejące)
│
└── feature/
    └── inbox/
        ├── data/
        │   └── repository/
        │       └── FakeInboxRepository.kt      # Implementacja — mockowe dane in-memory
        ├── domain/
        │   ├── model/
        │   │   ├── InboxMessage.kt             # Model domenowy przesyłki
        │   │   └── InboxFilter.kt              # Enum filtrów listy
        │   ├── repository/
        │   │   └── InboxRepository.kt          # Interfejs kontraktu repozytorium
        │   └── usecase/
        │       ├── GetInboxMessagesUseCase.kt  # Pobierz + filtruj + szukaj wiadomości
        │       └── ToggleStarUseCase.kt        # Przełącz oznaczenie gwiazdką
        └── presentation/
            ├── InboxUiState.kt                 # Stan ekranu (sealed class)
            ├── InboxAction.kt                  # Akcje użytkownika (sealed class)
            ├── InboxEffect.kt                  # Efekty jednorazowe (sealed class)
            ├── InboxViewModel.kt               # ViewModel ekranu Odebrane
            ├── InboxViewModelFactory.kt        # Factory dla ViewModelu (bez Hilt)
            ├── InboxScreen.kt                  # Główny composable ekranu
            └── components/
                ├── InboxTopAppBar.kt           # Top App Bar z tytułem i ikonami
                ├── InboxSearchBar.kt           # Pasek wyszukiwania
                ├── InboxFilterChips.kt         # Chipy filtrów: Wszystkie/Nieprzeczytane/Ważne
                ├── InboxMessageItem.kt         # Karta pojedynczej przesyłki
                ├── InboxLoadingState.kt        # Wskaźnik ładowania (CircularProgressIndicator)
                ├── InboxEmptyState.kt          # Stan pustej listy / brak wyników
                ├── InboxErrorState.kt          # Stan błędu z przyciskiem ponowienia
                └── InboxBottomNavBar.kt        # Dolna nawigacja (4 zakładki)

app/src/test/java/com/edoreczenia/
└── feature/inbox/
    ├── domain/
    │   └── usecase/
    │       ├── GetInboxMessagesUseCaseTest.kt
    │       └── ToggleStarUseCaseTest.kt
    ├── data/
    │   └── repository/
    │       └── FakeInboxRepositoryTest.kt
    └── presentation/
        └── InboxViewModelTest.kt
```

**Structure Decision**: Wariant Mobile (Android). Jeden moduł Android app. Kod podzielony na `core/` (wspólne) i `feature/inbox/` (feature-specific). Struktura odwzorowuje architekturę warstwową wprost na hierarchię pakietów — identycznie jak w `feature/auth/`.

---

## 1. Architektura modułu

### Warstwy i odpowiedzialności

#### `presentation` — co widzi użytkownik

- `InboxScreen`: główny composable ekranu, orkiestruje renderowanie stanów
- `InboxViewModel`: zarządza `InboxUiState`, reaguje na `InboxAction`, emituje `InboxEffect`
- `InboxViewModelFactory`: tworzy ViewModel z wstrzykniętymi zależnościami (bez Hilt)
- Komponenty UI: TopAppBar, SearchBar, FilterChips, MessageItem, LoadingState, EmptyState, ErrorState, BottomNavBar
- **Nie zawiera** logiki filtrowania ani biznesowej
- Dozwolone zależności: `domain` (use case'y, modele), `core` (AppError)

#### `domain` — logika biznesowa

- `InboxMessage`: model domenowy przesyłki — kompletny zestaw pól
- `InboxFilter`: enum wartości filtrów (ALL, UNREAD, STARRED)
- `InboxRepository`: interfejs kontraktu — `getMessages()`, `refresh()`, `toggleStar(id)`
- `GetInboxMessagesUseCase`: orkiestruje pobranie, filtrowanie (wg `InboxFilter`) i wyszukiwanie (query string)
- `ToggleStarUseCase`: przełącza `isStarred` dla wskazanej wiadomości
- **Nie zna** Androida, Compose, żadnych bibliotek zewnętrznych
- Dozwolone zależności: `core` (AppError)

#### `data` — dostęp do danych

- `FakeInboxRepository`: implementuje `InboxRepository`. Przechowuje stan listy jako `MutableStateFlow<List<InboxMessage>>` in-memory. Udostępnia `getMessages()` jako `Flow<List<InboxMessage>>` z symulowanym opóźnieniem 300 ms. Udostępnia `toggleStar(id)` — mutacja stanu in-memory. Dostarcza 4 predefiniowane rekordy mockowe.
- Dozwolone zależności: `domain` (kontrakty/modele), `core`

#### `core` — fundament (istniejące z 001-user-auth)

- `AppError`: sealed class — model błędów aplikacji. W tym feature'ze użyty `AppError.Technical` i `AppError.Network`.
- Motyw i kolory: `Color.kt`, `Theme.kt` — paleta e-Komornik (primary: `#001e40`, primaryContainer: `#003366`, secondaryContainer: `#fd8b00`)

### Kierunki zależności

```
InboxViewModel
    → GetInboxMessagesUseCase (domain)
        → InboxRepository (kontrakt domain)
            ← FakeInboxRepository (data) [wstrzyknięty przez InboxViewModelFactory]
    → ToggleStarUseCase (domain)
        → InboxRepository (kontrakt domain)
```

### Wpięcie w istniejący flow po zalogowaniu (001-user-auth)

Po sukcesie logowania (`LoginViewModel`) emitowany jest efekt `LoginEffect.NavigateToMain`. Efekt ten powinien nawigować do destynacji `inbox` w grafie nawigacji aplikacji (poza grafem `auth`). Zmiany wymagane w istniejącym kodzie:

1. `AppNavGraph.kt` (lub główny composable nawigacji): dodać `InboxNavGraph` / destynację `"inbox"` jako startową po zalogowaniu.
2. `LoginViewModel` / `AuthNavGraph`: zmienić docelową destynację efektu `NavigateToMain` z placeholdera na `"inbox"`.

---

## 2. Struktura pakietów

| Element | Pakiet | Warstwa |
|---|---|---|
| `InboxMessage`, `InboxFilter` | `feature.inbox.domain.model` | domain |
| `InboxRepository` (interfejs) | `feature.inbox.domain.repository` | domain |
| `GetInboxMessagesUseCase`, `ToggleStarUseCase` | `feature.inbox.domain.usecase` | domain |
| `FakeInboxRepository` | `feature.inbox.data.repository` | data |
| `InboxUiState`, `InboxAction`, `InboxEffect` | `feature.inbox.presentation` | presentation |
| `InboxViewModel`, `InboxViewModelFactory` | `feature.inbox.presentation` | presentation |
| `InboxScreen` | `feature.inbox.presentation` | presentation |
| Komponenty UI (`InboxTopAppBar`, itd.) | `feature.inbox.presentation.components` | presentation |

---

## 3. Model domenowy

### `InboxMessage` — pola domenowe

| Pole | Typ | Opis | Domenowe / Prezentacyjne |
|---|---|---|---|
| `id` | `String` | Unikalny identyfikator przesyłki | domenowe |
| `caseNumber` | `String` | Numer / sygnatura sprawy (np. "KM 1/23") | domenowe |
| `senderName` | `String` | Nazwa nadawcy / jednostki | domenowe |
| `subject` | `String` | Temat / tytuł przesyłki | domenowe |
| `preview` | `String` | Pełny fragment treści (skracanie przez `line-clamp` po stronie UI) | domenowe |
| `displayDate` | `String` | Data / czas preformatowany (np. "10:45", "Wczoraj", "15 Lis") | domenowe* |
| `isRead` | `Boolean` | Czy wiadomość jest przeczytana | domenowe |
| `isStarred` | `Boolean` | Czy wiadomość jest oznaczona gwiazdką | domenowe |

> *`displayDate` jako `String` preformatowany: w wersji mockowej wartości zdefiniowane w danych. Przy podłączeniu API: mapowanie `ISO datetime → displayDate` nastąpi w `InboxMapper` w warstwie `data`. Warstwy wyższe bez zmian.

### `InboxFilter` — enum

| Wartość | Etykieta UI | Logika filtrowania |
|---|---|---|
| `ALL` | "Wszystkie" | brak filtrowania (domyślny) |
| `UNREAD` | "Nieprzeczytane" | `message.isRead == false` |
| `STARRED` | "Ważne" | `message.isStarred == true` |

### `InboxUiState` — stany ekranu

```kotlin
sealed class InboxUiState {
    object Initial : InboxUiState()
    object Loading : InboxUiState()
    data class Success(
        val messages: List<InboxMessage>,
        val unreadCount: Int,
        val activeFilter: InboxFilter,
        val searchQuery: String,
        val isRefreshing: Boolean
    ) : InboxUiState()
    data class Empty(
        val reason: EmptyReason,
        val activeFilter: InboxFilter,
        val searchQuery: String
    ) : InboxUiState()
    data class Error(val message: String) : InboxUiState()
}

enum class EmptyReason { NO_MESSAGES, NO_RESULTS }
```

### Elementy wizualne obliczane z pól domenowych (nie osobne pola modelu)

| Element wizualny | Źródło | Logika |
|---|---|---|
| Ikona koperty | `isRead` | `mail` (FILL=1) gdy false; `drafts` (FILL=0) gdy true |
| Ikona gwiazdki | `isStarred` | `star` (FILL=1, secondaryContainer) gdy true; `star` (FILL=0, outline) gdy false |
| Kolor lewego paska | `isRead` | `secondaryContainer` (#fd8b00) gdy false; nieobecny gdy true |
| Kolor tekstu nadawcy | `isRead` | `primary` gdy false; `onSurfaceVariant` gdy true |
| Pogrubienie tematu | `isRead` | `FontWeight.SemiBold` gdy false; `FontWeight.Normal` gdy true |
| Kolor badge'a sprawy | `isRead` | `secondary`/`primary` gdy false; `outline` gdy true |
| Wskaźnik statusu (kropka) | `isRead` | Canvas `drawCircle` (#fd8b00, 8dp) gdy false; `Spacer(8dp)` gdy true |

---

## 4. Warstwa data

### `FakeInboxRepository`

**Interfejs implementowany**: `InboxRepository`

**Przechowywanie stanu**:
```
private val _messages = MutableStateFlow<List<InboxMessage>>(MOCK_MESSAGES)
```
Inicjalizowany predefiniowaną listą 4 rekordów. `toggleStar(id)` mutuje `_messages` in-memory: `_messages.update { list -> list.map { if (it.id == id) it.copy(isStarred = !it.isStarred) else it } }`.

**Sygnatury metod**:
- `getMessages(): Flow<List<InboxMessage>>` — emituje `_messages.asStateFlow()`
- `refresh(): Result<Unit>` — `delay(500..1000 ms)`, zwraca `Result.success(Unit)`. Z flagą `shouldSimulateError = true` zwraca `Result.failure(AppError.Network(...))`
- `toggleStar(id: String): Result<Unit>` — mutuje stan, zwraca `Result.success(Unit)`

**Mockowe rekordy** (stałe w `companion object` / `object InboxMockData`):

| id | caseNumber | senderName | subject | displayDate | isRead | isStarred |
|---|---|---|---|---|---|---|
| msg-001 | KM 1/23 | Sąd Rejonowy w Krakowie | wezwanie o zaliczkę | 10:45 | false | false |
| msg-002 | GKM 45/22 | Ministerstwo Finansów | wezwanie do usunięcia braków formalnych | Wczoraj | true | true |
| msg-003 | KM 124/23 | Jan Kowalski - Pełnomocnik | zawiadomienie | 15 Lis | false | false |
| msg-004 | KMS 7/23 | Urząd Skarbowy Warszawa | odpowiedź na pismo | 14 Lis | true | false |

Preview dla każdego rekordu zgodnie z clarify (pełne treści w `data-model.md`).

**Późniejsza wymiana na `InboxRepositoryImpl` z API**:
- Nowa klasa `InboxRepositoryImpl` implementuje ten sam interfejs `InboxRepository`
- Korzysta z `InboxApi` (Retrofit) i `InboxMapper` (DTO → model domenowy)
- Podmiana wyłącznie w `InboxViewModelFactory` — warstwy `domain` i `presentation` bez zmian

---

## 5. Warstwa domain

### `InboxRepository` — interfejs kontraktu

```kotlin
interface InboxRepository {
    fun getMessages(): Flow<List<InboxMessage>>
    suspend fun refresh(): Result<Unit>
    suspend fun toggleStar(id: String): Result<Unit>
}
```

Kontrakt minimalny dla zakresu tego feature'a. Przyszłe rozszerzenia: `markAsRead(id)`, `getMessageById(id)`, `getMessages(page, size)` — dodawane przy kolejnych feature'ach bez naruszania istniejących use case'ów.

### `GetInboxMessagesUseCase`

**Odpowiedzialność**: Subskrybuje `repository.getMessages()`, stosuje aktywny `InboxFilter` i query wyszukiwania. Zwraca `Flow<List<InboxMessage>>`.

**Logika filtrowania** (w use case'ie, nie w Composable):
- `InboxFilter.ALL`: brak filtrowania po statusie
- `InboxFilter.UNREAD`: `message.isRead == false`
- `InboxFilter.STARRED`: `message.isStarred == true`
- `searchQuery` (trim, lowercase, ignoreCase): dopasowanie do `senderName`, `subject`, `caseNumber`
- Łączenie filtrów: AND (filtr statusu AND query wyszukiwania)

Wywołanie z parametrami dynamicznymi: use case przyjmuje `filter: InboxFilter` i `searchQuery: String` jako parametry `invoke`. ViewModel wywołuje go z `combine(_activeFilter, _searchQuery)`.

### `ToggleStarUseCase`

**Odpowiedzialność**: Wywołuje `repository.toggleStar(id)`, opakowuje błędy w `AppError`.

**Sygnatura**: `suspend operator fun invoke(messageId: String): Result<Unit>`

**Obsługa błędów**: `Result.failure(AppError.Technical(...))` gdy repozytorium zwróci wyjątek.

### Mapowanie błędów

| AppError | Źródło | Komunikat dla użytkownika (strings.xml) |
|---|---|---|
| `AppError.Network` | brak sieci / symulacja w FakeRepo | `error_network_message` |
| `AppError.Technical` | nieoczekiwany wyjątek | `error_technical_message` |

---

## 6. Warstwa presentation

### `InboxAction` — akcje użytkownika

```kotlin
sealed class InboxAction {
    data class FilterChanged(val filter: InboxFilter) : InboxAction()
    data class SearchQueryChanged(val query: String) : InboxAction()
    data class ToggleStar(val messageId: String) : InboxAction()
    data class MessageClicked(val messageId: String) : InboxAction()
    object Refresh : InboxAction()
    object RetryLoad : InboxAction()
}
```

### `InboxEffect` — efekty jednorazowe

```kotlin
sealed class InboxEffect {
    data class NavigateToMessageDetail(val messageId: String) : InboxEffect()
    data class ShowToast(val message: String) : InboxEffect()
}
```

### `InboxViewModel`

**Zależności wstrzykiwane przez `InboxViewModelFactory`**:
- `getInboxMessagesUseCase: GetInboxMessagesUseCase`
- `toggleStarUseCase: ToggleStarUseCase`

**Stan wewnętrzny ViewModel**:
- `_activeFilter: MutableStateFlow<InboxFilter>` (domyślnie `ALL`)
- `_searchQuery: MutableStateFlow<String>` (domyślnie `""`)
- `_isRefreshing: MutableStateFlow<Boolean>` (domyślnie `false`)
- `_uiState: MutableStateFlow<InboxUiState>` (domyślnie `Initial`)
- `_effects: Channel<InboxEffect>` (efekty jednorazowe)

**Inicjalizacja (`init`)**:
1. `_uiState.value = Loading`
2. `combine(_activeFilter, _searchQuery)` → `flatMapLatest { getInboxMessagesUseCase(filter, query) }` → `collectLatest`
3. Na podstawie listy: jeśli pusta → `Empty(reason = NO_RESULTS lub NO_MESSAGES)`, w przeciwnym razie → `Success(messages, unreadCount, filter, query, isRefreshing = false)`
4. Obsługa błędu → `Error(message)`

**Obsługa akcji**:

| Akcja | Zachowanie ViewModel |
|---|---|
| `FilterChanged(filter)` | `_activeFilter.value = filter` |
| `SearchQueryChanged(query)` | `_searchQuery.value = query` |
| `ToggleStar(id)` | `viewModelScope.launch { toggleStarUseCase(id) }` |
| `MessageClicked(id)` | `effects.send(NavigateToMessageDetail(id))` |
| `Refresh` | `_isRefreshing = true`, wywołuje `repository.refresh()`, po zakończeniu `_isRefreshing = false`. W Success: `uiState.copy(isRefreshing = true/false)` |
| `RetryLoad` | Reset stanu do Loading, re-trigger kombinacji Flow |

**Obliczanie `unreadCount`**: `messages.count { !it.isRead }` — obliczane w ViewModelu przed ustawieniem stanu Success.

### Podział ekranu na Composable

```
InboxScreen
└── Scaffold
    ├── topBar: InboxTopAppBar
    ├── bottomBar: InboxBottomNavBar
    ├── floatingActionButton: FAB (ikona edit, containerColor = secondaryContainer, no-op/Toast)
    └── content:
        ├── InboxSearchBar           ← zawsze widoczny
        ├── InboxFilterChips         ← zawsze widoczny
        └── when (uiState):
            InboxUiState.Initial  → brak renderowania (krótkotrwały)
            InboxUiState.Loading  → InboxLoadingState (CircularProgressIndicator)
            InboxUiState.Success  → PullRefreshBox
                                        └── LazyColumn
                                                └── InboxMessageItem × N
            InboxUiState.Empty    → InboxEmptyState (wariant A lub B)
            InboxUiState.Error    → InboxErrorState
```

### Obsługa stanów UI

| Stan | Renderowanie | Trigger |
|---|---|---|
| `Initial` | Puste body | Start ViewModel (krótkotrwały) |
| `Loading` | `CircularProgressIndicator` wycentrowany | Po inicjalizacji / RetryLoad |
| `Success` | `LazyColumn` + `PullRefreshIndicator` | Flow emituje niepustą listę |
| `Empty (NO_RESULTS)` | Ikona `search_off`, "Brak wyników", podpis | Flow emituje pustą listę przy aktywnym filtrze/query |
| `Empty (NO_MESSAGES)` | Ikona `archive`, "Koniec wiadomości", podpis, dashed-border | Flow emituje pustą listę bez filtrów |
| `Error` | Ikona `cloud_off` (48dp, error #ba1a1a), komunikat, przycisk "Spróbuj ponownie" | Błąd z repozytorium |

### Pull-to-refresh

Użyty `PullRefreshIndicator` + `rememberPullRefreshState` z `androidx.compose.material`. Gest wywołuje `onAction(InboxAction.Refresh)`. ViewModel:
- Ustawia `isRefreshing = true` (jeśli stan to Success: `uiState.copy(isRefreshing = true)`)
- Wywołuje `repository.refresh()` z symulowanym opóźnieniem 500–1000 ms
- Po zakończeniu: `isRefreshing = false`

---

## 7. UI i zgodność z makietą

### Składowe ekranu

#### `InboxTopAppBar`
- Ikona menu (hamburger `menu`) — no-op
- Tytuł "e-Komornik" — `TopAppBarDefaults` Material 3
- Ikona powiadomień (`notifications`) — no-op
- Avatar (`person` w okrągłym kontenerze, kolor `primaryContainer`) — no-op

#### Nagłówek sekcji inbox (wewnątrz content, nad SearchBar)
- Tekst "Odebrane" — `HeadlineSmall`
- Badge "X nowe": `Text` z `LabelSmall`, tło `secondaryContainer` (#fd8b00), zaokrąglone rogi — obliczany z `unreadCount`

#### `InboxSearchBar`
- `OutlinedTextField` lub Material 3 `SearchBar` z placeholder "Szukaj w skrzynce"
- Ikona `search` leading
- `onValueChange → InboxAction.SearchQueryChanged(query)` — filtrowanie w czasie rzeczywistym

#### `InboxFilterChips`
- `FilterChip` × 3: "Wszystkie" / "Nieprzeczytane" / "Ważne"
- `selected = true` (aktywny): `selectedContainerColor = primaryContainer` (#003366), tekst biały
- `selected = false` (nieaktywny): outline style, `onSurfaceVariant`
- `onClick → InboxAction.FilterChanged(filter)`

#### `InboxMessageItem` — układ karty

```
Row {
    // Lewy pasek
    Box(width=4dp, fillMaxHeight, color=secondaryContainer gdy !isRead, transparent gdy isRead)

    // Ikona koperty
    Icon(mail FILL=1 gdy !isRead, drafts FILL=0 gdy isRead, 24dp)

    // Wskaźnik statusu (kropka)
    Canvas { drawCircle(color=#fd8b00, r=4dp) } gdy !isRead
    Spacer(8dp) gdy isRead

    // Treść wiadomości
    Column {
        Row {
            // Badge numeru sprawy (LabelSmall)
            // Data (BodySmall, end-aligned)
        }
        Text(senderName, TitleSmall, bold gdy !isRead, onSurfaceVariant gdy isRead)
        Text(subject, BodyMedium, SemiBold gdy !isRead, Normal gdy isRead)
        Text(preview, BodySmall, maxLines=1, overflow=Ellipsis)
    }

    // Ikona gwiazdki
    IconButton(onClick = { ToggleStar(id) }) {
        Icon(star FILL=1 secondaryContainer gdy isStarred, star FILL=0 outline gdy !isStarred, 24dp)
    }
}
```

#### `InboxBottomNavBar`
- `NavigationBar` (Material 3) z 4 pozycjami:
  - **Poczta** (`mail`): `selected=true`, `indicatorColor=primaryContainer` (#003366)
  - **Do wysyłki** (`outbox`): `selected=false`, no-op
  - **ADE** (`manage_search`): `selected=false`, no-op
  - **Ustawienia** (`settings`): `selected=false`, no-op
- Nieaktywne ikony: `onSurfaceVariant`

#### FAB
- `FloatingActionButton`, ikona `edit`
- `containerColor = MaterialTheme.colorScheme.secondaryContainer`
- `onClick`: Toast "Tworzenie wiadomości — wkrótce" lub no-op

### Paleta kolorów

| Token Material 3 | Wartość hex | Użycie w ekranie |
|---|---|---|
| `primary` | `#001e40` | Teksty primarne, badge aktywny |
| `primaryContainer` | `#003366` | Aktywna zakładka nav, aktywny filtr chip |
| `secondaryContainer` | `#fd8b00` | Lewy pasek, FAB, gwiazdka ważna, kropka statusu, badge nowe |
| `error` | `#ba1a1a` | Ikona `cloud_off` w stanie błędu |
| `onSurfaceVariant` | M3 system | Tekst przeczytanych wiadomości, ikony nieaktywne |
| `surfaceContainer` | M3 system | Tło ikony archiwum w stanie pustym |

### Spacing i wymiary

| Element | Wartość |
|---|---|
| Lewy pasek wiadomości | 4 dp szerokości |
| Wskaźnik statusu (kropka) | 8×8 dp |
| Ikony koperty / gwiazdki | 24 dp |
| Ikony w stanach Error/Empty | 48 dp |
| Padding karty wiadomości | 12–16 dp |
| Odstęp stanu Empty od góry listy | 32 dp |

---

## 8. Mock data strategy

### Zestaw 4 rekordów mockowych

Pokrycie stanów i przypadków testowych:

| Rekord | Status | Gwiazdka | Cel demonstracyjny |
|---|---|---|---|
| msg-001 (KM 1/23, Sąd Rejonowy w Krakowie) | **Nieprzeczytana** | Nie | Pełny styl unread: pasek, kropka, bold, `mail`, badge primary |
| msg-002 (GKM 45/22, Ministerstwo Finansów) | Przeczytana | **Tak** | Styl read + aktywna gwiazdka: `drafts`, muted, filled star |
| msg-003 (KM 124/23, Jan Kowalski - Pełnomocnik) | **Nieprzeczytana** | Nie | Drugi przykład unread — osoby fizyczne jako nadawca |
| msg-004 (KMS 7/23, Urząd Skarbowy Warszawa) | Przeczytana | Nie | Drugi przykład read — brak gwiazdki, brak paska |

**Pokrycie filtrów przez mock data**:
- Filtr "Wszystkie": 4 rekordy
- Filtr "Nieprzeczytane": msg-001, msg-003 (2 rekordy)
- Filtr "Ważne": msg-002 (1 rekord)
- Badge nowych: `unreadCount = 2`
- Wyszukiwanie "Sąd" → msg-001; "Kowalski" → msg-003; fraza nieistniejąca → Empty wariant B
- Stan błędu: flaga `shouldSimulateError = true` w FakeRepository

---

## 9. Nawigacja

### Flow po zalogowaniu

```
LoginScreen (feature/auth)
    ↓ LoginEffect.NavigateToMain
        ↓ navController.navigate("inbox") { popUpTo("login") { inclusive = true } }
InboxScreen (feature/inbox)
```

**Zmiany wymagane w istniejącym kodzie 001-user-auth**:
1. `AppNavGraph.kt` / `MainActivity.kt`: zarejestrować trasę `"inbox"` jako composable i ustawić ją jako cel po zalogowaniu.
2. `AuthNavGraph.kt` / obsługa efektu `NavigateToMain`: nawigacja do `"inbox"` z usunięciem LoginScreen ze stosu.

### Punkt nawigacyjny do szczegółów (out-of-scope implementacja, in-scope rejestracja)

Trasa: `"inbox_detail/{messageId}"` z argumentem nawigacyjnym `messageId: String`.

W bieżącej wersji:
- `InboxAction.MessageClicked(id)` → `InboxEffect.NavigateToMessageDetail(id)`
- `InboxScreen` odbiera efekt i wywołuje `navController.navigate("inbox_detail/$messageId")`
- Destynacja `"inbox_detail/{messageId}"` zarejestrowana w grafie z placeholder composable wyświetlającym Toast "Szczegóły wiadomości — wkrótce"

### Dolna nawigacja — zakładki bez ekranów

Zakładki "Do wysyłki", "ADE", "Ustawienia": `onClick = {}`. Destynacje niezarejestrowane w grafie. Implementacja w przyszłych feature'ach.

---

## 10. Testowalność

### `FakeInboxRepositoryTest`
- `getMessages()` emituje 4 rekordy mockowe po inicjalizacji
- `toggleStar("msg-001")` ustawia `isStarred = true` dla msg-001, pozostałe bez zmian
- `toggleStar("msg-002")` (już `isStarred=true`) ustawia `isStarred = false`
- `toggleStar("nieistniejące-id")` zwraca `Result.success(Unit)` bez błędu
- `refresh()` zwraca `Result.success(Unit)` po opóźnieniu
- `refresh()` z `shouldSimulateError=true` zwraca `Result.failure(AppError.Network(...))`

### `GetInboxMessagesUseCaseTest`
- Filtr `ALL` → 4 wiadomości
- Filtr `UNREAD` → 2 wiadomości (msg-001, msg-003)
- Filtr `STARRED` → 1 wiadomość (msg-002)
- Query "Sąd" → msg-001
- Query "zawiad" → msg-003
- Query nieistniejąca → pusta lista
- Łączenie: `UNREAD` + query "KM" → msg-001, msg-003 (oba spełniają oba warunki)
- Zmiana stanu przez `toggleStar` → Flow emituje zaktualizowaną listę

### `ToggleStarUseCaseTest`
- `invoke("msg-001")` deleguje do `repository.toggleStar("msg-001")`
- Błąd z repozytorium → `Result.failure(AppError.Technical(...))`

### `InboxViewModelTest`
- Inicjalizacja: stany Initial → Loading → Success (z `runTest` + `turbine`)
- `FilterChanged(UNREAD)` → lista zawiera tylko nieprzeczytane
- `SearchQueryChanged("Sąd")` → lista zawiera tylko msg-001
- `ToggleStar("msg-001")` → wywołuje `toggleStarUseCase` z "msg-001"
- `Refresh` → `isRefreshing` przechodzi `true → false`
- `RetryLoad` po błędzie → Loading → Success
- Błąd z FakeRepository → stan Error z komunikatem
- Pusta lista po filtrowaniu STARRED (gdy none starred) → stan Empty (NO_RESULTS)

**Narzędzia**: `kotlinx-coroutines-test` (`runTest`, `UnconfinedTestDispatcher`), `MockK` dla `InboxRepository`, `turbine` (opcjonalnie) dla assertions na Flow.

---

## 11. Trade-offy i decyzje architektoniczne

### Dlaczego `FakeInboxRepository` w pierwszej wersji?
Backend API dla skrzynki nie jest dostępny. FakeRepository pozwala na pełne demo UI, testowanie wszystkich 5 stanów i logiki filtrowania bez zależności sieciowych. Kontrakt `InboxRepository` zapewnia, że zamiana na `InboxRepositoryImpl` (Retrofit) wymaga zmiany tylko w factory/DI — warstwy `domain` i `presentation` bez zmian.

### Dlaczego `LazyColumn` bez paginacji?
`LazyColumn` jest stosowany ze względów architektonicznych (wydajność, gotowość na paginację). 4 statyczne rekordy nie wymagają paginacji. Dodanie Jetpack Paging 3 w przyszłości: zmiana wyłącznie w warstwie `data` i source danych, UI (`LazyColumn`) bez przepisywania.

### Dlaczego bez Room?
Stan gwiazdki żyje in-memory w ViewModelu. Brak wymagania trwałości cache w tej wersji. Dodanie Room: nowy `InboxDao` + `InboxEntity` + mapper w `data`, kontrakt `InboxRepository` bez zmian.

### Dlaczego bez Hilt?
NFR-006 — decyzja projektowa dla bieżącej fazy. `InboxViewModelFactory` dostarcza zależności ręcznie. Migracja do Hilt: `@HiltViewModel` na ViewModel, `@Module @InstallIn` dla bindowania — zmiany tylko w `di/` i ViewModelu.

### Gotowość na podłączenie API
- `InboxRepository` interfejs nie zna HTTP ✓
- `GetInboxMessagesUseCase` operuje na `Flow<List<InboxMessage>>` ✓
- `displayDate` jako `String`: mapowanie ISO → display w `InboxMapper` ✓
- Obsługa błędów 401 (auto-wylogowanie): nowy typ `AppError.Unauthorized` + obsługa w `InboxRepositoryImpl` ✓

---

## 12. Elementy odroczone (Out-of-Scope dla tej iteracji)

| Element | Uzasadnienie |
|---|---|
| Ekran szczegółów przesyłki (`InboxDetailScreen`) | Oddzielny feature (003+) |
| Zmiana `isRead=true` po kliknięciu | Należy do feature'u szczegółów |
| Backend API (`InboxRepositoryImpl`, `InboxApi`, DTO) | Brak dostępnego API |
| Persystencja danych (Room, DataStore) | Brak wymagania cache w tej wersji |
| Paginacja (Jetpack Paging 3) | Explicitnie out-of-scope (FR-018) |
| Powiadomienia push | Oddzielny feature |
| Ekrany: Do wysyłki, ADE, Ustawienia | Oddzielne feature'y |
| Ekran tworzenia nowej wiadomości | FAB no-op |
| Biometria i zarządzanie sesją | Feature security |
| Auto-wylogowanie przy błędzie 401 | Brak API, brak sesji w tym feature'ze |
| Infinite scroll / load-more | Explicitnie out-of-scope |
| Trwały cache offline | Zaawansowana funkcja przyszła |
