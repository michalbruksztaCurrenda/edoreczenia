# Research: 002-inbox-odebrane — Ekran Odebrane (Inbox)

**Data**: 2026-04-24 | **Feature**: `002-inbox-odebrane`

---

## Decyzje techniczne i uzasadnienia

### 1. Pull-to-refresh API w Compose Material 3

**Decision**: Używamy `PullRefreshIndicator` + `rememberPullRefreshState` z `androidx.compose.material` (Material 2 compat layer). Material 3 w Compose BOM 2026.03.00 nie eksportuje własnego `PullRefreshIndicator` jako stable API — dostępny jest `PullToRefreshBox` / `PullToRefreshState` w `androidx.compose.material3` (experimental w starszych BOM, stable od BOM 2024.x+). Należy zweryfikować dostępność w projekcie i użyć stabilnego wariantu z aktualnego BOM.

**Rationale**: Pull-to-refresh jest wymaganiem FR-009. `PullToRefreshBox` z Material 3 jest stabilne od BOM 2024.06.00+ i preferowane nad Material 2 compat. Przy aktualnym BOM 2026.03.00 należy użyć `PullToRefreshBox` z M3.

**Alternatives considered**: Zewnętrzna biblioteka (Accompanist) — odrzucona ze względu na zasadę II Konstytucji (opakowywanie zewnętrznych integracji) i dostępność natywnego rozwiązania w BOM.

---

### 2. StateFlow vs SharedFlow dla efektów jednorazowych

**Decision**: `Channel<InboxEffect>` z `receiveAsFlow()` dla efektów jednorazowych (nawigacja, Toast). `MutableStateFlow<InboxUiState>` dla stanu ekranu.

**Rationale**: Channel gwarantuje jednokrotną konsumpcję efektu — nie jest odtwarzany po rekompozycji. `SharedFlow(replay=0)` jest alternatywą, ale Channel jest prostszy i nie wymaga konfiguracji. `StateFlow` dla UiState zapewnia, że nowy subskrybent zawsze otrzyma aktualny stan (np. po obróceniu urządzenia).

**Alternatives considered**: `SharedFlow` — możliwy, ale Channel jest idiomatyczny dla efektów jednorazowych w Compose bez Hilt.

---

### 3. Logika filtrowania — use case vs ViewModel

**Decision**: Logika filtrowania i wyszukiwania umieszczona w `GetInboxMessagesUseCase`, nie w ViewModel ani Composable.

**Rationale**: Zgodnie z zasadą XVI i XX Konstytucji — logika biznesowa nie może być w UI. Use case jest jednostkowo testowalny bez zależności od Androida. ViewModel jest odpowiedzialny wyłącznie za zarządzanie stanem i przekazywanie akcji użytkownika do use case'ów.

**Alternatives considered**: Filtrowanie w ViewModel — odrzucone (narusza zasadę V Konstytucji). Filtrowanie w Composable — odrzucone (narusza zasady XVI i XX).

---

### 4. `displayDate` jako String vs LocalDateTime

**Decision**: `displayDate: String` w modelu domenowym `InboxMessage`.

**Rationale**: Dane mockowe mają preformatowane daty ("10:45", "Wczoraj", "15 Lis"). Przy podłączeniu API: mapowanie `ISO datetime → displayDate` odbędzie się w `InboxMapper` w warstwie `data` — warstwy `domain` i `presentation` bez zmian. Brak nadmiarowej zależności od `java.time` w modelu domenowym w pierwszej wersji.

**Alternatives considered**: `LocalDateTime` w modelu domenowym + formatowanie w Composable — odrzucone (formatowanie to logika prezentacyjna, nie domenowa; komplikuje model bez potrzeby w tej wersji).

---

### 5. MutableStateFlow vs Room dla stanu gwiazdki

**Decision**: Stan gwiazdki przechowywany wyłącznie w `MutableStateFlow` w `FakeInboxRepository` in-memory.

**Rationale**: NFR-007 spec — Room nie jest wymagany w tym feature'ze. Stan gwiazdki jest lokalny (nie synchronizowany z backendem). Trwałość między sesjami nie jest wymaganiem pierwszej wersji. Dodanie Room w przyszłości: nowy `InboxDao`, `InboxEntity`, mapper — bez zmian w `domain` i `presentation`.

**Alternatives considered**: Room od razu — odrzucone (over-engineering, brak wymagania persystencji).

---

### 6. EmptyReason — rozróżnienie wariantów pustej listy

**Decision**: Wprowadzamy `enum class EmptyReason { NO_MESSAGES, NO_RESULTS }` w ramach `InboxUiState.Empty`.

**Rationale**: Spec i clarify definiują dwa wizualnie różne warianty stanu pustego: (A) pusta lista bez filtrów — ikona `archive`, "Koniec wiadomości"; (B) brak wyników filtrowania/wyszukiwania — ikona `search_off`, "Brak wyników". Rozróżnienie musi być jawne — UI nie może wymagać logiki warunkowej opartej na stanie filtru.

**Alternatives considered**: Przekazywanie `activeFilter` i `searchQuery` do Composable i decydowanie tam — odrzucone (logika warunkowa w UI narusza zasadę XX).

---

### 7. Brak Hilt — InboxViewModelFactory

**Decision**: `InboxViewModelFactory` implementujące `ViewModelProvider.Factory` dostarczają zależności (`GetInboxMessagesUseCase`, `ToggleStarUseCase`) do `InboxViewModel`.

**Rationale**: NFR-006 spec — Hilt wyłączony dla bieżącej fazy. `viewModel(factory = InboxViewModelFactory(...))` w `InboxScreen` lub `NavHost` jest idiomatic Compose bez Hilt. Factory tworzone z `FakeInboxRepository` w punkcie kompozycji.

**Alternatives considered**: Hilt — odrzucone (NFR-006). Singleton fabryka — odrzucone (utrudnia testowanie).

---

### 8. Ikony Material Symbols

**Decision**: Ikony pobierane z `androidx.compose.material.icons` (Extended Icons): `Icons.Filled.Mail`, `Icons.Outlined.Drafts`, `Icons.Filled.Star`, `Icons.Outlined.StarBorder`, `Icons.Filled.Edit`, `Icons.Outlined.Archive`, `Icons.Outlined.SearchOff`, `Icons.Outlined.CloudOff`, `Icons.Outlined.Outbox`, `Icons.Outlined.ManageSearch`, `Icons.Outlined.Settings`, `Icons.Outlined.Notifications`, `Icons.Outlined.Person`, `Icons.Outlined.Menu`.

**Rationale**: NFR-010 — ikony MUSZĄ pochodzić z Material Symbols / Material Icons dostępnych w projekcie. Extended Icons jest dostępne przez `implementation(libs.androidx.compose.material.icons.extended)` — należy dodać do `build.gradle.kts` jeśli jeszcze nie ma.

**Alternatives considered**: Material Symbols via font — możliwe, ale Extended Icons jest prostszym rozwiązaniem w projekcie Compose bez dodatkowej konfiguracji.

