# Implementation Plan: 003-do-wysylki — Ekran Do wysyłki (Outbox)

**Branch**: `003-do-wysylki` | **Date**: 2026-04-25 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/003-do-wysylki/spec.md`

---

## Summary

Implementacja ekranu „Do wysyłki" — listy pozycji oczekujących na wysłanie w aplikacji eDoreczenia. Ekran prezentuje mockowe dane z `FakeOutboxRepository`, obsługuje pięć stanów UI (Initial / Loading / Success / Empty / Error), baner statusu z licznikiem i przyciskiem „Synchronizuj", pasek szybkich akcji oraz nawigację do ekranu szczegółów (placeholder). Architektura warstwowa (presentation / domain / data / core), bez Hilt, bez Room. UI w Jetpack Compose z Material 3. Pull-to-refresh aktywny. Brak UI zaznaczania (bez checkboxów), brak prawdziwej wysyłki.

---

## Technical Context

**Language/Version**: Kotlin 2.3.20  
**Primary Dependencies**: Jetpack Compose BOM 2026.03.00, Material 3 (via BOM), Navigation Compose 2.9.0, Lifecycle ViewModel Compose 2.10.0, Core KTX 1.18.0  
**Storage**: Brak Room — dane mockowe in-memory (`FakeOutboxRepository`). Stan in-memory w ViewModel. Czas ostatniej synchronizacji przechowywany w ViewModel jako `MutableStateFlow<String>`.  
**Testing**: JUnit 4, MockK 1.14, kotlinx-coroutines-test 1.10, FakeOutboxRepository  
**Target Platform**: Android, minSdk 34, targetSdk/compileSdk 36, JVM 17  
**Project Type**: Native Android Mobile App — ekran listy (outbox) wewnątrz istniejącej aplikacji  
**Performance Goals**: Ekran „Do wysyłki" dostępny natychmiast po przejściu z BottomNav (dane mockowe, brak opóźnień sieciowych). Symulowany refresh: 1–2 s delay.  
**Constraints**: Bez Hilt — DI ręczne przez `OutboxViewModelFactory`. Bez Room. Bez backendowego API. 4 statyczne rekordy mockowe, brak paginacji. Brak checkboxów, brak UI zaznaczania.  
**Scale/Scope**: 1 ekran główny (OutboxScreen), 2 use case'y (GetOutboxItems, RefreshOutboxItems), 1 fake repository, 4 mockowe rekordy, baner statusu, pasek quick actions, pull-to-refresh, ekran szczegółów (placeholder).

---

## Constitution Check

| Zasada | Status | Uzasadnienie |
|---|---|---|
| III. Architektura warstwowa | ✅ PASS | Moduł podzielony na presentation / domain / data / core |
| IV. Modularność feature'ów | ✅ PASS | Osobny pakiet `feature/outbox` z wewnętrznym podziałem na warstwy |
| VI. Zakaz hardcodowania | ✅ PASS | Teksty UI w `strings.xml`, kolory w `Color.kt` / Theme, mockowe dane jako stałe w `OutboxMockData` |
| VII. Obsługa błędów — 5 stanów | ✅ PASS | `OutboxUiState` obsługuje Initial / Loading / Success / Empty / Error |
| VIII. Bezpieczeństwo danych | ✅ PASS | Brak wrażliwych danych w tym feature'ze; mock data nie zawiera PII w sensie runtime |
| IX. Dane lokalne | ✅ PASS | Brak Room — uzasadnione: dane mockowe, brak wymagania persystencji w tym etapie |
| X. Komunikacja sieciowa | ✅ PASS | Brak wywołań sieciowych (FakeRepository); gotowość na podmianę przez interfejs |
| XIII. Testowalność | ✅ PASS | `FakeOutboxRepository`, testowalne use case'y, testowalne ViewModele przez coroutines-test |
| XIV. Czytelność | ✅ PASS | Nazewnictwo domenowe: OutboxItem, OutboxStatus, OutboxUiState |
| XX. Zakazy bezwzględne | ✅ PASS | Brak wywołań sieciowych z presentation, brak surowych wyjątków do UI, brak logiki w Composable |

> **Uwaga dotycząca Hilt (zasada XIII)**: Konstytucja wymaga Hilt dla DI. W tym feature'ze Hilt jest celowo wyłączony (decyzja projektowa analogiczna do 002-inbox-odebrane). Zależności dostarczane ręcznie przez `OutboxViewModelFactory`. Jest to udokumentowany wyjątek tymczasowy, do usunięcia przy podłączaniu API.

**GATE: PASS** — brak naruszeń Konstytucji nieuzasadnionych decyzją projektową.

---

## Project Structure

### Documentation (this feature)

```text
specs/003-do-wysylki/
├── plan.md              # Ten plik
├── research.md          # Phase 0 — decyzje techniczne
├── data-model.md        # Phase 1 — modele i kontrakty
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
│   │   └── AppError.kt                              # sealed class (istniejące z 001-user-auth)
│   └── ui/
│       └── theme/                                   # Material 3 Theme (istniejące)
│
└── feature/
    └── outbox/
        ├── data/
        │   └── repository/
        │       └── FakeOutboxRepository.kt          # Implementacja — mockowe dane in-memory
        ├── domain/
        │   ├── model/
        │   │   ├── OutboxItem.kt                    # Model domenowy pozycji outbox
        │   │   └── OutboxStatus.kt                  # Enum statusów przesyłki
        │   ├── repository/
        │   │   └── OutboxRepository.kt              # Interfejs kontraktu repozytorium
        │   └── usecase/
        │       ├── GetOutboxItemsUseCase.kt          # Pobierz pozycje outbox
        │       └── RefreshOutboxItemsUseCase.kt      # Odśwież / synchronizuj listę
        └── presentation/
            ├── OutboxUiState.kt                     # Stan ekranu (sealed class)
            ├── OutboxAction.kt                      # Akcje użytkownika (sealed class)
            ├── OutboxEffect.kt                      # Efekty jednorazowe (sealed class)
            ├── OutboxViewModel.kt                   # ViewModel ekranu Do wysyłki
            ├── OutboxViewModelFactory.kt            # Factory dla ViewModelu (bez Hilt)
            ├── OutboxScreen.kt                      # Główny composable ekranu
            ├── OutboxDetailPlaceholderScreen.kt     # Placeholder ekranu szczegółów
            └── components/
                ├── OutboxTopAppBar.kt               # Top App Bar z tytułem i ikonami
                ├── OutboxStatusBanner.kt            # Baner z liczbą, czasem, przyciskiem Synchronizuj
                ├── OutboxQuickActionsBar.kt         # Pasek szybkich akcji (Zaznacz wszystko, Odśwież, Wyślij)
                ├── OutboxItemCard.kt                # Karta pojedynczej pozycji outbox
                ├── OutboxLoadingState.kt            # Wskaźnik ładowania (CircularProgressIndicator)
                ├── OutboxEmptyState.kt              # Stan pustej listy
                ├── OutboxErrorState.kt              # Stan błędu z przyciskiem ponowienia
                └── OutboxBottomNavBar.kt            # Dolna nawigacja (4 zakładki, Do wysyłki aktywna)

app/src/test/java/com/edoreczenia/
└── feature/outbox/
    ├── domain/
    │   └── usecase/
    │       ├── GetOutboxItemsUseCaseTest.kt
    │       └── RefreshOutboxItemsUseCaseTest.kt
    ├── data/
    │   └── repository/
    │       └── FakeOutboxRepositoryTest.kt
    └── presentation/
        └── OutboxViewModelTest.kt
```

---

## 1. Architektura modułu

### Warstwy i odpowiedzialności

#### `presentation` — co widzi użytkownik

- `OutboxScreen`: główny composable ekranu, orkiestruje renderowanie stanów
- `OutboxViewModel`: zarządza `OutboxUiState`, reaguje na `OutboxAction`, emituje `OutboxEffect`
- `OutboxViewModelFactory`: tworzy ViewModel z wstrzykniętymi zależnościami (bez Hilt)
- Komponenty UI: TopAppBar, StatusBanner, QuickActionsBar, ItemCard, LoadingState, EmptyState, ErrorState, BottomNavBar
- `OutboxDetailPlaceholderScreen`: placeholder ekranu szczegółów (tylko TopAppBar z Back i tekst „Szczegóły wkrótce")
- **Nie zawiera** logiki biznesowej
- Dozwolone zależności: `domain` (use case'y, modele), `core` (AppError)

#### `domain` — logika biznesowa

- `OutboxItem`: model domenowy pozycji — kompletny zestaw pól
- `OutboxStatus`: enum statusów (`SEND_ERROR`, `PENDING_APPROVAL`, `WAITING`)
- `OutboxRepository`: interfejs kontraktu — `getItems()`, `refresh()`
- `GetOutboxItemsUseCase`: subskrybuje `repository.getItems()`, zwraca `Flow<List<OutboxItem>>`
- `RefreshOutboxItemsUseCase`: wywołuje `repository.refresh()`, symuluje opóźnienie, zwraca `Result<Unit>`
- **Nie zna** Androida, Compose, żadnych bibliotek zewnętrznych
- Dozwolone zależności: `core` (AppError)

#### `data` — dostęp do danych

- `FakeOutboxRepository`: implementuje `OutboxRepository`. Przechowuje stan listy jako `MutableStateFlow<List<OutboxItem>>` in-memory. Udostępnia `getItems()` jako `Flow<List<OutboxItem>>`. `refresh()` symuluje opóźnienie 1–2 s i ponownie emituje te same dane.
- Dozwolone zależności: `domain` (kontrakty/modele), `core`

#### `core` — fundament (istniejące z 001-user-auth)

- `AppError`: sealed class — model błędów aplikacji. W tym feature'ze użyte: `AppError.Technical`, `AppError.Network`.
- Motyw i kolory: `Color.kt`, `Theme.kt` — paleta eDoreczenia

### Kierunki zależności

```
OutboxViewModel
    → GetOutboxItemsUseCase (domain)
        → OutboxRepository (kontrakt domain)
            ← FakeOutboxRepository (data) [wstrzyknięty przez OutboxViewModelFactory]
    → RefreshOutboxItemsUseCase (domain)
        → OutboxRepository (kontrakt domain)
```

### Wpięcie feature'a w istniejący flow i nawigację

- `OutboxScreen` dostępny z `BottomNavigationBar` — zakładka „Do wysyłki" (pozycja 2)
- `BottomNavBar` jest wspólnym komponentem pojawiającym się w `InboxScreen` (002) i `OutboxScreen` (003) — aktywna zakładka różni się w zależności od ekranu
- Kliknięcie zakładki „Poczta" przenosi z Outbox do Inbox (`"inbox"` w AppNavGraph)
- Kliknięcie zakładki „Do wysyłki" przenosi z Inbox do Outbox (`"outbox"` w AppNavGraph)
- Kliknięcie pozycji listy przenosi do `OutboxDetailPlaceholderScreen` (`"outbox_detail/{itemId}"`)
- Zmiany wymagane w istniejącym kodzie:
  1. `AppNavGraph.kt`: dodać destynację `"outbox"` oraz `"outbox_detail/{itemId}"`
  2. `InboxBottomNavBar.kt` / `OutboxBottomNavBar.kt`: zaktualizować handlery kliknięć zakładki „Do wysyłki" / „Poczta"

---

## 2. Struktura pakietów

| Element | Pakiet | Warstwa |
|---|---|---|
| `OutboxItem`, `OutboxStatus` | `feature.outbox.domain.model` | domain |
| `OutboxRepository` (interfejs) | `feature.outbox.domain.repository` | domain |
| `GetOutboxItemsUseCase`, `RefreshOutboxItemsUseCase` | `feature.outbox.domain.usecase` | domain |
| `FakeOutboxRepository` | `feature.outbox.data.repository` | data |
| `OutboxUiState`, `OutboxAction`, `OutboxEffect` | `feature.outbox.presentation` | presentation |
| `OutboxViewModel`, `OutboxViewModelFactory` | `feature.outbox.presentation` | presentation |
| `OutboxScreen`, `OutboxDetailPlaceholderScreen` | `feature.outbox.presentation` | presentation |
| Komponenty UI (`OutboxTopAppBar`, itd.) | `feature.outbox.presentation.components` | presentation |

---

## 3. Model domenowy

### `OutboxItem` — pola domenowe

| Pole | Typ | Opis | Wymóg makiety |
|---|---|---|---|
| `id` | `String` | Unikalny identyfikator pozycji | identyfikacja przy nawigacji |
| `caseNumber` | `String` | Numer sprawy (np. „KM 124/23") | widoczny jako badge/chip |
| `recipientName` | `String` | Nazwa adresata (osoba lub podmiot) | pierwsza linia karty |
| `subject` | `String` | Temat / tytuł przesyłki | druga linia karty |
| `status` | `OutboxStatus` | Enum statusu przesyłki | badge + kolor krawędzi |
| `errorMessage` | `String?` | Opcjonalny komunikat błędu (`null` gdy brak) | widoczny tylko dla SEND_ERROR |

### `OutboxStatus` — enum

| Wartość | Etykieta UI | Kolor krawędzi (token M3) | Kolor badge (token M3) |
|---|---|---|---|
| `SEND_ERROR` | „Błąd wysyłki" | `error` (czerwony) | `errorContainer` |
| `PENDING_APPROVAL` | „Do zatwierdzenia" | `tertiaryFixedDim` (pomarańczowy) | `surfaceContainer` |
| `WAITING` | „Oczekuje na wysyłkę" | `surfaceVariant` (szary) | `surfaceContainerHigh` |

> Fallback dla nieznanych statusów: traktowany jak `WAITING`.

### Elementy wizualne obliczane z pól domenowych (nie osobne pola modelu)

| Element wizualny | Źródło | Logika |
|---|---|---|
| Kolor lewej krawędzi karty | `status` | Mapeowany wg tabeli powyżej na token koloru M3 |
| Kolor i etykieta badge'a | `status` | Mapeowany wg tabeli powyżej |
| Widoczność `errorMessage` | `errorMessage` | Wyświetlany tylko gdy `errorMessage != null` |

---

## 4. Warstwa data

### `FakeOutboxRepository`

**Interfejs implementowany**: `OutboxRepository`

**Przechowywanie stanu**:
```
private val _items = MutableStateFlow<List<OutboxItem>>(MOCK_ITEMS)
```
Inicjalizowany predefiniowaną listą 4 rekordów (zdefiniowanych w `companion object` lub `object OutboxMockData`).

**Sygnatury metod**:
- `getItems(): Flow<List<OutboxItem>>` — emituje `_items.asStateFlow()`
- `refresh(): Result<Unit>` — `delay(1000..2000 ms)`, ponownie emituje te same dane (brak zmiany `_items`), zwraca `Result.success(Unit)`. Z flagą `shouldSimulateError = true` zwraca `Result.failure(AppError.Network(...))`

**Mockowe rekordy** (stałe `OutboxMockData`):

| id | caseNumber | recipientName | subject | status | errorMessage |
|----|------------|---------------|---------|--------|--------------|
| `out-001` | KM 124/23 | Janusz Kowalski | Wezwanie do zapłaty - Zaległe alimenty | `SEND_ERROR` | Brak podpisu kwalifikowanego |
| `out-002` | GKM 45/24 | Anna Nowak | Postanowienie o zajęciu wynagrodzenia | `PENDING_APPROVAL` | null |
| `out-003` | KM 902/22 | PKO Bank Polski S.A. | Zapytanie o stan konta dłużnika | `WAITING` | null |
| `out-004` | KM 11/24 | Marek Wójcik | Postanowienie o umorzeniu | `WAITING` | null |

**Późniejsza wymiana na `OutboxRepositoryImpl` z API**:
- Nowa klasa `OutboxRepositoryImpl` implementuje ten sam interfejs `OutboxRepository`
- Korzysta z `OutboxApi` (Retrofit) i `OutboxMapper` (DTO → model domenowy)
- Podmiana wyłącznie w `OutboxViewModelFactory` — warstwy `domain` i `presentation` bez zmian

---

## 5. Warstwa domain

### `OutboxRepository` — interfejs kontraktu

```
interface OutboxRepository {
    fun getItems(): Flow<List<OutboxItem>>
    suspend fun refresh(): Result<Unit>
}
```

Kontrakt minimalny dla zakresu tego feature'a. Przyszłe rozszerzenia: `getItemById(id)`, `sendItems(ids)`, `getItems(page, size)` — dodawane przy kolejnych feature'ach bez naruszania istniejących use case'ów.

### `GetOutboxItemsUseCase`

**Odpowiedzialność**: Subskrybuje `repository.getItems()`, zwraca `Flow<List<OutboxItem>>`. Nie stosuje filtrowania ani wyszukiwania w tej wersji (brak filtrów w spec).

**Sygnatura**: `operator fun invoke(): Flow<List<OutboxItem>>`

**Obsługa błędów**: błędy Flow mapowane na `AppError` w ViewModelu (catch w `collectLatest`).

### `RefreshOutboxItemsUseCase`

**Odpowiedzialność**: Wywołuje `repository.refresh()`, opakowuje błędy w `AppError`.

**Sygnatura**: `suspend operator fun invoke(): Result<Unit>`

**Obsługa błędów**: `Result.failure(AppError.Network(...))` lub `AppError.Technical(...)` — zależnie od źródła błędu.

### Obsługa stanów

| Stan | Kiedy | Reprezentacja |
|---|---|---|
| `Initial` | Przed pierwszym ładowaniem | `OutboxUiState.Initial` |
| `Loading` | Trwa pobieranie | `OutboxUiState.Loading` |
| `Success` | Lista załadowana (niepusta) | `OutboxUiState.Success(items, unsendCount, lastSyncTime, isRefreshing)` |
| `Empty` | Lista załadowana, ale pusta | `OutboxUiState.Empty` |
| `Error` | Błąd pobierania | `OutboxUiState.Error(message)` |

### Mapowanie błędów

| AppError | Źródło | Klucz strings.xml |
|---|---|---|
| `AppError.Network` | brak sieci / symulacja w FakeRepo | `error_network` |
| `AppError.Technical` | nieoczekiwany wyjątek | generyczny komunikat techniczny |

---

## 6. Warstwa presentation

### `OutboxUiState` — stany ekranu

```
sealed class OutboxUiState {
    object Initial : OutboxUiState()
    object Loading : OutboxUiState()
    data class Success(
        val items: List<OutboxItem>,
        val unsendCount: Int,          // liczba pozycji ze statusem != (gotowe do wysłania) — de facto wszystkie 4 w mocku
        val lastSyncTime: String,      // sformatowany czas ostatniej synchronizacji, np. "10:32"
        val isRefreshing: Boolean      // pull-to-refresh / Synchronizuj w toku
    ) : OutboxUiState()
    object Empty : OutboxUiState()
    data class Error(val message: String) : OutboxUiState()
}
```

> Brak pola zaznaczenia (`selectedIds`) — zgodnie z clarify: UI multi-select nie wchodzi w zakres tej iteracji.

### `OutboxAction` — akcje użytkownika

```
sealed class OutboxAction {
    object Refresh : OutboxAction()              // pull-to-refresh lub „Odśwież listę"
    object Synchronize : OutboxAction()          // przycisk „Synchronizuj" w banerze
    object SelectAll : OutboxAction()            // brak reakcji w mock (no-op)
    object Send : OutboxAction()                 // brak reakcji w mock (no-op)
    data class ItemClicked(val itemId: String) : OutboxAction()
    object RetryLoad : OutboxAction()
}
```

### `OutboxEffect` — efekty jednorazowe

```
sealed class OutboxEffect {
    data class NavigateToDetail(val itemId: String) : OutboxEffect()
}
```

> Brak efektu `ShowToast` — zgodnie z clarify: „Zaznacz wszystko" i „Wyślij" nie wywołują żadnej reakcji (brak Snackbara w mock).

### `OutboxViewModel`

**Zależności wstrzykiwane przez `OutboxViewModelFactory`**:
- `getOutboxItemsUseCase: GetOutboxItemsUseCase`
- `refreshOutboxItemsUseCase: RefreshOutboxItemsUseCase`

**Stan wewnętrzny ViewModel**:
- `_uiState: MutableStateFlow<OutboxUiState>` (domyślnie `Initial`)
- `_lastSyncTime: MutableStateFlow<String>` (domyślnie bieżący czas przy inicjalizacji)
- `_isRefreshing: MutableStateFlow<Boolean>` (domyślnie `false`)
- `_effects: Channel<OutboxEffect>` (efekty jednorazowe)

**Inicjalizacja (`init`)**:
1. `_uiState.value = Loading`
2. `getOutboxItemsUseCase()` → `collectLatest`
3. Na podstawie listy:
   - pusta → `OutboxUiState.Empty`
   - niepusta → `OutboxUiState.Success(items, unsendCount = items.size, lastSyncTime, isRefreshing = false)`
4. Obsługa błędu → `OutboxUiState.Error(message)`

**Obsługa akcji**:

| Akcja | Zachowanie ViewModel |
|---|---|
| `Refresh` | `_isRefreshing = true`, wywołuje `refreshOutboxItemsUseCase()`, po zakończeniu `_isRefreshing = false`, aktualizuje `_lastSyncTime` |
| `Synchronize` | Identyczne jak `Refresh` — loading 1–2 s → reload → aktualizacja czasu |
| `SelectAll` | no-op (brak implementacji w mock) |
| `Send` | no-op (brak implementacji w mock) |
| `ItemClicked(id)` | `effects.send(NavigateToDetail(id))` |
| `RetryLoad` | Reset stanu do Loading, re-trigger kolekcji Flow |

**Obliczanie `unsendCount`**: `items.size` — w mock wszystkie pozycje są „niewysłane".

**Obliczanie `lastSyncTime`**: formatowany bieżący czas (`HH:mm`) przy inicjalizacji i po każdym Refresh/Synchronize.

### Podział ekranu na Composable

```
OutboxScreen
└── Scaffold
    ├── topBar: OutboxTopAppBar
    │       └── title: „Do wysyłki", ikona menu (start), ikona wyszukiwania (end)
    ├── bottomBar: OutboxBottomNavBar
    │       └── 4 zakładki: Poczta | Do wysyłki (aktywna) | ADE | Ustawienia
    └── content (SwipeRefresh / PullToRefreshBox):
        ├── OutboxStatusBanner          ← zawsze widoczny gdy Success
        ├── OutboxQuickActionsBar       ← zawsze widoczny gdy Success
        └── when (uiState):
            OutboxUiState.Initial     → brak renderowania (krótkotrwały)
            OutboxUiState.Loading     → OutboxLoadingState (CircularProgressIndicator)
            OutboxUiState.Success     → LazyColumn z OutboxItemCard dla każdej pozycji
            OutboxUiState.Empty       → OutboxEmptyState
            OutboxUiState.Error       → OutboxErrorState z przyciskiem „Spróbuj ponownie"
```

> **Uwaga**: `OutboxStatusBanner` i `OutboxQuickActionsBar` renderowane wyłącznie w stanie `Success`. W stanach `Loading`, `Empty` i `Error` baner i pasek akcji nie są wyświetlane — ekran pokazuje tylko odpowiedni stan.

---

## 7. UI i zgodność z makietą

### Sekcje i komponenty ekranu (według `docs/screens/do_wysy_ki/screen.png`)

**TopAppBar**:
- Tytuł: „Do wysyłki" — `strings.xml` (`outbox_title`)
- Ikona menu (hamburger) po lewej — brak funkcji w tej iteracji (no-op)
- Ikona wyszukiwania po prawej — brak funkcji w tej iteracji (no-op, wizualnie obecna)
- Kolor tła: `primary` (#001e40), tekst i ikony: `onPrimary`

**Baner statusu** (`OutboxStatusBanner`):
- Ikona `sync_problem` (Material Symbol, FILL=0, kolor `error`)
- Tekst: „X niewysłanych elementów" (liczba z `unsendCount`)
- Tekst: „Ostatnia próba: HH:mm"
- Przycisk „Synchronizuj" (TextButton lub OutlinedButton)
- Tło: `errorContainer` lub dedykowany kolor z palety — do doprecyzowania przy implementacji na podstawie kodu HTML makiety
- Padding: `16.dp` horizontal, `12.dp` vertical

**Pasek szybkich akcji** (`OutboxQuickActionsBar`):
- Poziomy Row z 3 elementami: „Zaznacz wszystko" | „Odśwież listę" | „Wyślij"
- Każdy element: ikona + etykieta (TextButton z ikoną)
- Ikony: `check_box_outline_blank` / `refresh` / `send` (Material Symbols)
- „Zaznacz wszystko" i „Wyślij" — no-op w mock
- „Odśwież listę" — wywołuje `OutboxAction.Refresh`
- Separator / divider pod paskiem akcji

**Lista pozycji** (`LazyColumn` z `OutboxItemCard`):
- Każda karta (`OutboxItemCard`):
  - Lewa krawędź 4dp w kolorze statusu (drawn jako `Box` z `fillMaxHeight()` lub `Canvas`)
  - Numer sprawy: chip/badge (np. `AssistChip` lub `SuggestionChip`) w kolorze statusu
  - Nazwa adresata: `bodyLarge` / `titleSmall`, `FontWeight.SemiBold`
  - Temat przesyłki: `bodyMedium`, `onSurfaceVariant`, maxLines=2
  - Badge statusu: `FilterChip` lub `SuggestionChip` z etykietą tekstową i kolorem wg `OutboxStatus`
  - Komunikat błędu (warunkowy): `bodySmall`, kolor `error`, widoczny tylko gdy `errorMessage != null`
  - Padding: `16.dp` horizontal, `12.dp` vertical
  - Divider między kartami
- Kliknięcie karty → `OutboxAction.ItemClicked(itemId)`

**Stan pusty** (`OutboxEmptyState`):
- Ikona (np. `outbox` lub `check_circle`, Material Symbol)
- Tytuł: `outbox_empty_title` = „Brak pozycji do wysyłki"
- Podtytuł: `outbox_empty_subtitle` = „Wszystkie przesyłki zostały wysłane"
- Wyśrodkowany pionowo i poziomo

**Stan błędu** (`OutboxErrorState`):
- Ikona `error_outline`
- Tytuł: `outbox_error_title` = „Nie udało się pobrać kolejki"
- Przycisk: `outbox_error_retry_button` = „Spróbuj ponownie" → `OutboxAction.RetryLoad`

**Stan ładowania** (`OutboxLoadingState`):
- `CircularProgressIndicator` wyśrodkowany

**BottomNavBar** (`OutboxBottomNavBar`):
- 4 zakładki: Poczta (`inbox`) | Do wysyłki (`send`, aktywna) | ADE (`search`) | Ustawienia (`settings`)
- Aktywna zakładka „Do wysyłki" wyróżniona kolorem `secondaryContainer`
- Ikony: `mail` | `send` | `search` | `settings` (Material Symbols)
- Etykiety z `strings.xml`: `inbox_bottom_nav_inbox` | `inbox_bottom_nav_outbox` | `inbox_bottom_nav_ade` | `inbox_bottom_nav_settings` (reużycie kluczy z 002)

### Zgodność wizualna z dotychczasowymi ekranami

- Paleta kolorów identyczna jak w `feature/inbox` (primary `#001e40`, secondaryContainer `#fd8b00`)
- TopAppBar w kolorze `primary` — spójne z Inbox
- Material 3 komponenty: `Scaffold`, `LazyColumn`, `TopAppBar`, `NavigationBar`, `SuggestionChip`
- Typografia: domyślna skala M3 — bez odchyleń

### Elementy aktywne vs. tylko wizualne

| Element | Stan | Działanie |
|---|---|---|
| „Odśwież listę" | Aktywny | Wywołuje Refresh |
| „Synchronizuj" (baner) | Aktywny | Wywołuje Synchronize (loading → reload) |
| Pull-to-refresh | Aktywny | Wywołuje Refresh |
| Kliknięcie karty pozycji | Aktywny | Nawigacja do Detail |
| „Zaznacz wszystko" | Wizualny (no-op) | Brak reakcji |
| „Wyślij" | Wizualny (no-op) | Brak reakcji |
| Ikona wyszukiwania (TopAppBar) | Wizualna | Brak reakcji |
| Ikona menu (TopAppBar) | Wizualna | Brak reakcji |

---

## 8. Mock data strategy

### 4 przykładowe rekordy `OutboxMockData`

#### Rekord 1 — SEND_ERROR

| Pole | Wartość |
|---|---|
| `id` | `"out-001"` |
| `caseNumber` | `"KM 124/23"` |
| `recipientName` | `"Janusz Kowalski"` |
| `subject` | `"Wezwanie do zapłaty - Zaległe alimenty"` |
| `status` | `SEND_ERROR` |
| `errorMessage` | `"Brak podpisu kwalifikowanego"` |

Karta wyświetla: czerwoną krawędź, badge „Błąd wysyłki" w `errorContainer`, komunikat błędu pod adresatem.

#### Rekord 2 — PENDING_APPROVAL

| Pole | Wartość |
|---|---|
| `id` | `"out-002"` |
| `caseNumber` | `"GKM 45/24"` |
| `recipientName` | `"Anna Nowak"` |
| `subject` | `"Postanowienie o zajęciu wynagrodzenia"` |
| `status` | `PENDING_APPROVAL` |
| `errorMessage` | `null` |

Karta wyświetla: pomarańczową krawędź, badge „Do zatwierdzenia" w `surfaceContainer`. Brak komunikatu błędu.

#### Rekord 3 — WAITING (podmiot)

| Pole | Wartość |
|---|---|
| `id` | `"out-003"` |
| `caseNumber` | `"KM 902/22"` |
| `recipientName` | `"PKO Bank Polski S.A."` |
| `subject` | `"Zapytanie o stan konta dłużnika"` |
| `status` | `WAITING` |
| `errorMessage` | `null` |

Karta wyświetla: szarą krawędź, badge „Oczekuje na wysyłkę". Brak komunikatu błędu.

#### Rekord 4 — WAITING (osoba fizyczna)

| Pole | Wartość |
|---|---|
| `id` | `"out-004"` |
| `caseNumber` | `"KM 11/24"` |
| `recipientName` | `"Marek Wójcik"` |
| `subject` | `"Postanowienie o umorzeniu"` |
| `status` | `WAITING` |
| `errorMessage` | `null` |

Karta wyświetla: szarą krawędź, badge „Oczekuje na wysyłkę". Brak komunikatu błędu.

---

## 9. Nawigacja

### Przejście do ekranu „Do wysyłki"

Użytkownik przechodzi do ekranu „Do wysyłki" przez kliknięcie zakładki „Do wysyłki" w `BottomNavigationBar` dostępnej na ekranie `InboxScreen` (i każdym innym ekranie z BottomNav). Nawigacja realizowana przez `NavController.navigate("outbox")`.

### Wpięcie w `AppNavGraph`

Destynacje do dodania:
- `"outbox"` → `OutboxScreen`
- `"outbox_detail/{itemId}"` → `OutboxDetailPlaceholderScreen`

Destynacja `"outbox"` jako sibling destynacji `"inbox"` w grafie głównym (nie w grafie auth).

### Nawigacja do szczegółów

- Kliknięcie karty pozycji → `OutboxEffect.NavigateToDetail(itemId)` → `NavController.navigate("outbox_detail/$itemId")`
- `OutboxDetailPlaceholderScreen`: TopAppBar z tytułem „Szczegóły przesyłki" i ikoną `arrow_back`, kliknięcie Back → `navController.popBackStack()`
- Systemowy Back → `popBackStack()` (domyślne zachowanie Compose Navigation)

### Zachowanie powrotu

- Z `OutboxScreen` → zakładka „Poczta" → `navController.navigate("inbox")` (ewentualnie z `popUpTo` dla czystego back stacku)
- Z `OutboxDetailPlaceholderScreen` → Back → powrót do `OutboxScreen`

---

## 10. Testowalność

### Testy `FakeOutboxRepositoryTest`

- Weryfikacja, że `getItems()` emituje 4 rekordy mockowe
- Weryfikacja, że `refresh()` zwraca `Result.success(Unit)`
- Weryfikacja, że `refresh()` z flagą `shouldSimulateError = true` zwraca `Result.failure`
- Weryfikacja, że lista po `refresh()` pozostaje niezmieniona (te same dane)

### Testy `GetOutboxItemsUseCaseTest`

- Weryfikacja, że use case zwraca `Flow` emitujący listę z repozytorium
- Weryfikacja zachowania przy pustej liście z repozytorium
- Weryfikacja, że use case nie modyfikuje kolejności ani zawartości listy

### Testy `RefreshOutboxItemsUseCaseTest`

- Weryfikacja, że use case zwraca `Result.success` przy poprawnym refresh
- Weryfikacja, że use case zwraca `Result.failure(AppError.Network)` przy błędzie sieciowym
- Weryfikacja mapowania błędów na `AppError`

### Testy `OutboxViewModelTest`

- Test stanu `Loading` po inicjalizacji (przed emisją z Flow)
- Test stanu `Success` po emisji listy z `GetOutboxItemsUseCase`
- Test stanu `Empty` gdy use case emituje pustą listę
- Test stanu `Error` gdy kolekcja Flow rzuca wyjątek
- Test akcji `Refresh`: ViewModel ustawia `isRefreshing = true`, wywołuje use case, ustawia `isRefreshing = false`
- Test akcji `Synchronize`: identyczne jak Refresh + aktualizacja `lastSyncTime`
- Test akcji `SelectAll`: brak zmiany stanu (no-op)
- Test akcji `Send`: brak zmiany stanu (no-op)
- Test akcji `ItemClicked(id)`: emitowany efekt `NavigateToDetail(id)`
- Test akcji `RetryLoad`: stan wraca do `Loading`
- Test obliczenia `unsendCount` — równy liczbie elementów na liście

---

## 11. Trade-offy i decyzje architektoniczne

### Dlaczego `FakeOutboxRepository` w pierwszej wersji?

Pozwala na pełne wdrożenie i przetestowanie warstw domain i presentation bez zależności od backendu. Interfejs `OutboxRepository` gwarantuje, że podmiana `FakeOutboxRepository` na `OutboxRepositoryImpl` w przyszłości wymaga zmiany wyłącznie w `OutboxViewModelFactory`. Warstwy domain i presentation pozostają bez zmian.

### Dlaczego bez Room?

Dane outbox w pierwszej wersji są danymi mockowanymi — trwałe przechowywanie nie przynosi wartości. Room zostanie dodany przy implementacji trybu offline lub synchronizacji z backendem. Decyzja zmniejsza złożoność i przyspiesza pierwszą iterację.

### Dlaczego bez backendu?

Feature 003 jest iteracją UI/UX — celem jest wdrożenie warstw presentation i domain, weryfikacja zgodności z makietą i przygotowanie architektury. Backend zostanie podłączony w kolejnej iteracji. Separacja przez interfejs `OutboxRepository` zapewnia gotowość na podmianę bez refaktoryzacji architektury.

### Dlaczego brak UI zaznaczania (checkboxów)?

Decyzja z clarify: multi-select jest poza zakresem tej iteracji. „Zaznacz wszystko" i „Wyślij" są obecne wizualnie (spójność z makietą), ale bez logiki — jawne no-op w ViewModelu. Brak pola `selectedIds` w `OutboxUiState` — dodane przy implementacji multi-select.

### Gotowość na późniejsze podłączenie wysyłki

- Interfejs `OutboxRepository` jest miejscem na dodanie `suspend fun sendItems(ids: List<String>): Result<Unit>`
- `OutboxAction.Send` jest już zdefiniowany jako no-op — zamiana na rzeczywistą logikę nie narusza istniejących akcji
- `OutboxUiState.Success` może zostać rozszerzony o `selectedIds: Set<String>` bez zmiany pozostałych stanów

---

## 12. Elementy odroczone

Poniższe elementy są **poza zakresem tej iteracji** i nie mogą być implementowane:

| Element | Uzasadnienie |
|---|---|
| Prawdziwa wysyłka dokumentów do backendu | Brak API, poza zakresem spec |
| Integracja z REST API / Retrofit dla outbox | Poza zakresem spec — pierwsza iteracja mockowa |
| Trwałe przechowywanie danych (Room) | Brak wymagania persystencji w tej iteracji |
| Pełny ekran szczegółów pozycji outbox | Osobny feature — placeholder wystarczy |
| UI multi-select (checkboxy, zaznaczanie) | Poza zakresem zgodnie z clarify |
| Masowa wysyłka zaznaczonych pozycji | Poza zakresem zgodnie z clarify |
| Zaawansowane filtrowanie i sortowanie listy | Brak w spec i makiecie dla tej iteracji |
| Paginacja / infinite scroll | Poza zakresem spec |
| Powiadomienia push o zmianie statusu | Osobny feature |
| Obsługa załączników na ekranie listy | Osobny feature |
| Edycja i usuwanie pozycji z listy | Poza zakresem spec |

---

## Nowe klucze `strings.xml` (do dodania)

| Klucz | Wartość PL |
|---|---|
| `outbox_title` | „Do wysyłki" |
| `outbox_empty_title` | „Brak pozycji do wysyłki" |
| `outbox_empty_subtitle` | „Wszystkie przesyłki zostały wysłane" |
| `outbox_error_title` | „Nie udało się pobrać kolejki" |
| `outbox_error_retry_button` | „Spróbuj ponownie" |
| `outbox_status_send_error` | „Błąd wysyłki" |
| `outbox_status_pending_approval` | „Do zatwierdzenia" |
| `outbox_status_waiting` | „Oczekuje na wysyłkę" |
| `outbox_banner_unsent_count` | „%d niewysłanych elementów" |
| `outbox_banner_last_sync` | „Ostatnia próba: %s" |
| `outbox_banner_sync_button` | „Synchronizuj" |
| `outbox_quick_action_select_all` | „Zaznacz wszystko" |
| `outbox_quick_action_refresh` | „Odśwież listę" |
| `outbox_quick_action_send` | „Wyślij" |
| `outbox_detail_placeholder` | „Szczegóły przesyłki wkrótce" |
| `outbox_back` | „Wróć do Do wysyłki" |

