# Tasks: 002-inbox-odebrane — Ekran Odebrane (Inbox)

**Input**: Design documents from `/specs/002-inbox-odebrane/`
**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/navigation-contract.md ✅ | quickstart.md ✅

**Uwaga**: `AppError.kt` istnieje już w `core/error/` (feature 001-user-auth). Nie zawiera `AppError.Technical` — plan odwołuje się do `AppError.Unknown` jako odpowiednika. Wariant `AppError.Network` już istnieje. Szczegół do weryfikacji przez developera przy T005.

---

## Format: `[ID] [P?] [Story] Opis z ścieżką pliku`

- **[P]**: Można wykonać równolegle z innymi zadaniami w tej samej fazie (różne pliki, brak nierozwiązanych zależności)
- **[Story]**: Przynależność do User Story z spec.md
- Faza Setup / Foundational: brak etykiety Story

---

## Phase 1: Setup — Struktura pakietów i zasoby

**Cel**: Przygotowanie szkieletu pakietów feature'a inbox oraz zasobów stringów. Blokuje wszystkie fazy domenowe i prezentacyjne.

- [ ] T001 Utwórz strukturę pakietów feature inbox: `app/src/main/java/com/edoreczenia/feature/inbox/domain/model/`, `domain/repository/`, `domain/usecase/`, `data/repository/`, `presentation/components/` oraz `app/src/test/java/com/edoreczenia/feature/inbox/domain/usecase/`, `data/repository/`, `presentation/`
- [ ] T002 [P] Dodaj wpisy strings.xml dla feature inbox (etykiety ogólne, filtry, stany puste, stan błędu, nawigacja dolna, FAB, komunikat szczegółów) zgodnie z data-model.md sekcja "Zasoby strings.xml" w `app/src/main/res/values/strings.xml`

---

## Phase 2: Foundational — Modele domenowe i kontrakt repozytorium

**Cel**: Fundament domeny — modele i interfejs kontraktu. Blokuje use case'y, FakeRepository i ViewModel.

- [ ] T003 Utwórz `InboxMessage` data class (pola: id, caseNumber, senderName, subject, preview, displayDate, isRead, isStarred) w `app/src/main/java/com/edoreczenia/feature/inbox/domain/model/InboxMessage.kt`
- [ ] T004 [P] Utwórz `InboxFilter` enum (ALL, UNREAD, STARRED) w `app/src/main/java/com/edoreczenia/feature/inbox/domain/model/InboxFilter.kt`
- [ ] T005 Utwórz interfejs `InboxRepository` z metodami `getMessages(): Flow<List<InboxMessage>>`, `suspend fun refresh(): Result<Unit>`, `suspend fun toggleStar(id: String): Result<Unit>` w `app/src/main/java/com/edoreczenia/feature/inbox/domain/repository/InboxRepository.kt`

  > **Zależność**: T003, T004 muszą być gotowe przed T005.

---

## Phase 3: User Story 5 & 6 & 7 — FakeInboxRepository i use case'y (fundament stanów)

**Cel**: Implementacja warstwy danych i logiki domenowej niezbędnej do obsługi wszystkich stanów UI (US5 loading, US6 error, US7 empty). Blokuje ViewModel i wszystkie testy.

**Niezależny test**: `FakeInboxRepositoryTest` + `GetInboxMessagesUseCaseTest` + `ToggleStarUseCaseTest` przechodzą bez UI.

- [ ] T006 [US5] [US6] [US7] Utwórz `FakeInboxRepository` implementujący `InboxRepository`: `MutableStateFlow<List<InboxMessage>>(MOCK_MESSAGES)`, `getMessages()` jako `Flow` z `delay(300ms)`, `refresh()` z `delay(500..1000ms)` i flagą `shouldSimulateError: Boolean = false` (gdy true → `Result.failure(AppError.Network(...))`), `toggleStar(id)` mutujący stan in-memory przez `update`. Dane mockowe (4 rekordy: msg-001..msg-004) jako stałe w `companion object`. Preview zgodne z data-model.md. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/data/repository/FakeInboxRepository.kt`

  > **Zależność**: T003, T004, T005.

- [ ] T007 [P] [US5] [US6] [US7] Utwórz `GetInboxMessagesUseCase`: przyjmuje `repository: InboxRepository`, operator `invoke(filter: InboxFilter, searchQuery: String): Flow<List<InboxMessage>>` — subskrybuje `repository.getMessages()`, stosuje filtrowanie wg `InboxFilter` (ALL/UNREAD/STARRED) i wyszukiwanie (trim+lowercase+ignoreCase na senderName, subject, caseNumber), łączy oba warunki przez AND. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/domain/usecase/GetInboxMessagesUseCase.kt`

  > **Zależność**: T003, T004, T005.

- [ ] T008 [P] [US4] Utwórz `ToggleStarUseCase`: przyjmuje `repository: InboxRepository`, `suspend operator fun invoke(messageId: String): Result<Unit>` — deleguje do `repository.toggleStar(messageId)`, opakowuje wyjątki w `Result.failure(AppError.Unknown(...))`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/domain/usecase/ToggleStarUseCase.kt`

  > **Zależność**: T005.

---

## Phase 4: User Story 5 — Stan ładowania (Loading state)

**Cel**: Pełny stack ViewModel + kontrakt prezentacyjny gotowy do renderowania stanu Loading.

**Niezależny test**: `InboxViewModelTest` — inicjalizacja przechodzi przez Initial → Loading → Success.

- [ ] T009 [US5] Utwórz `EmptyReason` enum (NO_MESSAGES, NO_RESULTS) i `InboxUiState` sealed class (Initial, Loading, Success, Empty, Error) w `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxUiState.kt`

  > **Zależność**: T003, T004.

- [ ] T010 [P] [US5] Utwórz `InboxAction` sealed class (FilterChanged, SearchQueryChanged, ToggleStar, MessageClicked, Refresh, RetryLoad) w `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxAction.kt`

  > **Zależność**: T004.

- [ ] T011 [P] [US5] [US9] Utwórz `InboxEffect` sealed class (NavigateToMessageDetail, ShowToast) w `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxEffect.kt`

- [ ] T012 [US5] Utwórz `InboxViewModel`: pola `_activeFilter`, `_searchQuery`, `_isRefreshing`, `_uiState` jako `MutableStateFlow`, `_effects` jako `Channel<InboxEffect>`. W `init`: ustawia Loading, uruchamia `combine(_activeFilter, _searchQuery).flatMapLatest { getInboxMessagesUseCase(filter, query) }.collectLatest` → mapuje pustą listę na `Empty(reason)`, niepustą na `Success(messages, unreadCount, filter, query, isRefreshing)`, błąd na `Error(message)`. Metoda `onAction(action: InboxAction)` obsługuje wszystkie warianty (FilterChanged, SearchQueryChanged, ToggleStar, MessageClicked, Refresh, RetryLoad). Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxViewModel.kt`

  > **Zależność**: T007, T008, T009, T010, T011.

- [ ] T013 [P] [US5] Utwórz `InboxViewModelFactory` implementujący `ViewModelProvider.Factory`, przyjmujący `getInboxMessagesUseCase` i `toggleStarUseCase`, tworzący `InboxViewModel`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxViewModelFactory.kt`

  > **Zależność**: T012.

- [ ] T014 [US5] Utwórz `InboxLoadingState` composable: `CircularProgressIndicator` wycentrowany pionowo i poziomo w `Box(fillMaxSize)`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxLoadingState.kt`

---

## Phase 5: User Story 1 — Lista przesyłek (Success state)

**Cel**: Widoczna lista 4 mockowych przesyłek z pełnym wizualnym różnicowaniem read/unread/starred.

**Niezależny test**: Uruchomienie aplikacji po zalogowaniu pokazuje listę 4 rekordów z poprawnym stylem.

- [ ] T015 [US1] Utwórz `InboxMessageItem` composable: układ zgodny z planem sekcja 7 (`InboxMessageItem` — układ karty) — lewy pasek 4dp (secondaryContainer gdy !isRead, transparent gdy isRead), ikona koperty (Filled.Mail gdy !isRead, Outlined.Drafts gdy isRead, 24dp), wskaźnik statusu (Canvas drawCircle #fd8b00 8dp gdy !isRead, Spacer 8dp gdy isRead), kolumna treści (badge numeru sprawy LabelSmall, data BodySmall end-aligned, nadawca TitleSmall pogrubiony/muted, temat BodyMedium SemiBold/Normal, preview BodySmall maxLines=1 Ellipsis), ikona gwiazdki (IconButton: Filled.Star secondaryContainer gdy isStarred, Outlined.StarBorder outline gdy !isStarred). Parametry: `message: InboxMessage`, `onToggleStar: (String) -> Unit`, `onClick: (String) -> Unit`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxMessageItem.kt`

  > **Zależność**: T003, T004.

- [ ] T016 [P] [US1] Utwórz `InboxTopAppBar` composable: `TopAppBar` Material 3 z ikoną menu (hamburger, no-op), tytułem "e-Komornik", ikoną powiadomień (no-op), avatarem (ikona person w okrągłym kontenerze primaryContainer, no-op). Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxTopAppBar.kt`

- [ ] T017 [P] [US1] Utwórz `InboxBottomNavBar` composable: `NavigationBar` Material 3 z 4 pozycjami (Poczta/mail selected=true indicatorColor=primaryContainer, Do wysyłki/outbox no-op, ADE/manage_search no-op, Ustawienia/settings no-op). Nieaktywne ikony w onSurfaceVariant. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxBottomNavBar.kt`

- [ ] T018 [US1] Utwórz `InboxScreen` composable (wersja bazowa — stan Success): `Scaffold` z `topBar=InboxTopAppBar`, `bottomBar=InboxBottomNavBar`, FAB (ikona edit, containerColor=secondaryContainer, onClick=ShowToast). Content: nagłówek sekcji ("Odebrane" HeadlineSmall + badge "X nowe" LabelSmall tło secondaryContainer), `InboxSearchBar` (OutlinedTextField z ikoną search, placeholder ze strings.xml, onValueChange → SearchQueryChanged), `InboxFilterChips` (FilterChip ×3 z logiką selected/unselected), `when(uiState)` — dla Success: `PullToRefreshBox` wrapping `LazyColumn` z `InboxMessageItem`. Parametry: `navController: NavController`, `viewModel: InboxViewModel`. Zbiera `uiState` przez `collectAsState()`, efekty przez `LaunchedEffect`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxScreen.kt`

  > **Zależność**: T009, T010, T011, T012, T013, T014, T015, T016, T017.

  > **Uwaga pull-to-refresh**: Użyć `PullToRefreshBox` z `androidx.compose.material3` (stabilne w BOM 2026.03.00) zgodnie z research.md decision #1.

---

## Phase 6: User Story 2 & 3 — Filtrowanie i wyszukiwanie

**Cel**: Aktywne działanie chipów filtrów i pola wyszukiwania (logika już w GetInboxMessagesUseCase).

**Niezależny test**: Kliknięcie filtru "Nieprzeczytane" → 2 rekordy; wpisanie "Sąd" → 1 rekord.

- [ ] T019 [US2] [US3] Wydziel `InboxSearchBar` do osobnego composable: `OutlinedTextField` z ikoną `search` leading, placeholder `stringResource(R.string.inbox_search_placeholder)`, `onValueChange` przekazujące query do callbacku. Parametry: `query: String`, `onQueryChange: (String) -> Unit`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxSearchBar.kt`

  > **Zależność**: T018.

- [ ] T020 [P] [US2] Wydziel `InboxFilterChips` do osobnego composable: `Row` z `FilterChip` ×3 (Wszystkie/Nieprzeczytane/Ważne), `selected` i `onClick` sterowane przez `activeFilter: InboxFilter`. Styl aktywny: `selectedContainerColor=primaryContainer`, tekst biały. Styl nieaktywny: outline. Parametry: `activeFilter: InboxFilter`, `onFilterChange: (InboxFilter) -> Unit`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxFilterChips.kt`

  > **Zależność**: T018.

- [ ] T021 [US2] [US3] Zaktualizuj `InboxScreen` — zastąp inline search/filter wbudowanymi composable'ami `InboxSearchBar` i `InboxFilterChips` wydzielonymi w T019/T020. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxScreen.kt`

  > **Zależność**: T019, T020.

---

## Phase 7: User Story 7 — Stan pusty (Empty state)

**Cel**: Dwa wizualnie różne warianty pustego stanu (NO_MESSAGES i NO_RESULTS).

**Niezależny test**: Filtr "Ważne" bez zaznaczonych gwiazdek → stan Empty wariant B (search_off, "Brak wyników").

- [ ] T022 [US7] Utwórz `InboxEmptyState` composable: dwa warianty na podstawie `reason: EmptyReason` — (A) NO_MESSAGES: ikona `archive` 48dp w tle surfaceContainer, nagłówek "Koniec wiadomości" (HeadlineSmall), podpis "Wszystkie dokumenty zostały wyświetlone" (BodyMedium), dashed-border kontener; (B) NO_RESULTS: ikona `search_off` 48dp, nagłówek "Brak wyników", podpis "Nie znaleziono wiadomości spełniających kryteria". Oba warianty wyśrodkowane, odstęp 32dp od góry obszaru listy. Parametry: `reason: EmptyReason`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxEmptyState.kt`

  > **Zależność**: T009.

- [ ] T023 [US7] Dodaj obsługę stanu `InboxUiState.Empty` w `when(uiState)` w `InboxScreen` — wywołaj `InboxEmptyState(reason = uiState.reason)`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxScreen.kt`

  > **Zależność**: T022.

---

## Phase 8: User Story 6 — Stan błędu (Error state)

**Cel**: Czytelny komunikat błędu z przyciskiem ponowienia.

**Niezależny test**: `FakeInboxRepository(shouldSimulateError=true)` → ekran pokazuje InboxErrorState z przyciskiem "Spróbuj ponownie".

- [ ] T024 [US6] Utwórz `InboxErrorState` composable: ikona `cloud_off` 48dp w kolorze `error` (#ba1a1a) na tle `errorContainer`, nagłówek "Nie udało się pobrać wiadomości" (HeadlineSmall), podpis kontekstowy (BodyMedium), przycisk "Spróbuj ponownie" (Button z containerColor=secondaryContainer, onClick=onRetry). Układ wyśrodkowany pionowo i poziomo. Parametry: `message: String`, `onRetry: () -> Unit`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxErrorState.kt`

- [ ] T025 [US6] Dodaj obsługę stanu `InboxUiState.Error` w `when(uiState)` w `InboxScreen` — wywołaj `InboxErrorState(message = uiState.message, onRetry = { onAction(InboxAction.RetryLoad) })`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxScreen.kt`

  > **Zależność**: T024.

---

## Phase 9: User Story 4 — Oznaczanie gwiazdką

**Cel**: Tapnięcie gwiazdki zmienia stan in-memory i odświeża UI.

**Niezależny test**: Tapnięcie gwiazdki msg-001 → gwiazdka wypełniona; ponowne tapnięcie → gwiazdka outline. Filtr "Ważne" po oznaczeniu → msg-001 pojawia się na liście.

- [ ] T026 [US4] Zweryfikuj i dopasuj podłączenie `onToggleStar` w `InboxMessageItem` do `InboxAction.ToggleStar(messageId)` przekazywanego przez `InboxScreen`. Upewnij się, że zmiana stanu gwiazdki w `FakeInboxRepository` powoduje reemisję Flow i aktualizację UI. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxScreen.kt`

  > **Zależność**: T015, T018.

---

## Phase 10: User Story 8 — Pull-to-refresh

**Cel**: Gest pull-to-refresh wyzwala `InboxAction.Refresh`, pokazuje wskaźnik odświeżania przez 500–1000ms.

**Niezależny test**: Przeciągnięcie listy w dół → wskaźnik odświeżania pojawia się i znika po ~1s.

- [ ] T027 [US8] Upewnij się, że `PullToRefreshBox` w `InboxScreen` (stan Success) jest poprawnie skonfigurowany: `isRefreshing = (uiState as? Success)?.isRefreshing ?: false`, `onRefresh = { onAction(InboxAction.Refresh) }`. Zweryfikuj, że `InboxViewModel.onAction(Refresh)` wywołuje `repository.refresh()` i aktualizuje `isRefreshing` w UiState. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxScreen.kt`

  > **Zależność**: T018.

---

## Phase 11: User Story 9 — Nawigacja (integracja z AppNavGraph)

**Cel**: Ekran Inbox dostępny po zalogowaniu. Kliknięcie przesyłki nawiguje do placeholdera szczegółów.

**Niezależny test**: Logowanie → ekran Odebrane (nie placeholder). Tapnięcie przesyłki → ekran-placeholder z komunikatem "Szczegóły wiadomości — wkrótce".

- [ ] T028 [US9] Utwórz `InboxDetailPlaceholderScreen` composable: wyświetla `Text("Szczegóły wiadomości — wkrótce: $messageId")` wyśrodkowany. Parametry: `messageId: String`. Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxDetailPlaceholderScreen.kt`

- [ ] T029 [US1] [US9] Zaktualizuj `MainActivity.kt` — w `AppNavHost`: zastąp placeholder `MAIN_ROUTE` trasą `"inbox"` z `InboxScreen` (fabryka jednej instancji `FakeInboxRepository` → `GetInboxMessagesUseCase` + `ToggleStarUseCase` → `InboxViewModelFactory`). Zarejestruj destynację `"inbox_detail/{messageId}"` (argument `NavType.StringType`) z `InboxDetailPlaceholderScreen`. Zaktualizuj `onNavigateToMain` w `authNavGraph` tak, by navegować do `"inbox"` zamiast `"main"`. Plik: `app/src/main/java/com/edoreczenia/MainActivity.kt`

  > **Zależność**: T013, T018, T028.

- [ ] T030 [US9] Dodaj obsługę efektu `InboxEffect.NavigateToMessageDetail` w `LaunchedEffect` w `InboxScreen`: wywołaj `navController.navigate("inbox_detail/$messageId")`. Dodaj obsługę `InboxEffect.ShowToast` wyświetlającego `Toast` (np. dla FAB). Plik: `app/src/main/java/com/edoreczenia/feature/inbox/presentation/InboxScreen.kt`

  > **Zależność**: T029.

---

## Phase 12: Testy jednostkowe

**Cel**: Pokrycie testami domenowych use case'ów, repozytorium i ViewModelu.

- [ ] T031 [P] [US5] [US6] [US7] Utwórz `FakeInboxRepositoryTest`: scenariusze — `getMessages()` emituje 4 rekordy, `toggleStar("msg-001")` ustawia isStarred=true, `toggleStar("msg-002")` (już true) → false, `toggleStar("nieistniejące-id")` → success bez błędu, `refresh()` → success, `refresh(shouldSimulateError=true)` → `Result.failure(AppError.Network(...))`. Plik: `app/src/test/java/com/edoreczenia/feature/inbox/data/repository/FakeInboxRepositoryTest.kt`

  > **Zależność**: T006.

- [ ] T032 [P] [US2] [US3] Utwórz `GetInboxMessagesUseCaseTest`: scenariusze — filtr ALL → 4 wiadomości, UNREAD → 2 (msg-001, msg-003), STARRED → 1 (msg-002), query "Sąd" → msg-001, query "zawiad" → msg-003, query nieistniejąca → pusta lista, UNREAD + query "KM" → msg-001 + msg-003, toggleStar → Flow emituje zaktualizowaną listę. Używa `FakeInboxRepository` bezpośrednio (bez MockK). Plik: `app/src/test/java/com/edoreczenia/feature/inbox/domain/usecase/GetInboxMessagesUseCaseTest.kt`

  > **Zależność**: T007, T006.

- [ ] T033 [P] [US4] Utwórz `ToggleStarUseCaseTest`: scenariusze — `invoke("msg-001")` deleguje do repozytorium, błąd z repozytorium → `Result.failure(AppError.Unknown(...))`. Używa MockK dla `InboxRepository`. Plik: `app/src/test/java/com/edoreczenia/feature/inbox/domain/usecase/ToggleStarUseCaseTest.kt`

  > **Zależność**: T008.

- [ ] T034 [US5] [US2] [US3] [US4] [US6] [US7] [US8] Utwórz `InboxViewModelTest`: scenariusze — inicjalizacja Initial → Loading → Success (`runTest`, `UnconfinedTestDispatcher`), `FilterChanged(UNREAD)` → lista 2 rekordy, `SearchQueryChanged("Sąd")` → lista 1 rekord, `ToggleStar("msg-001")` → wywołuje `toggleStarUseCase`, `Refresh` → isRefreshing true → false, `RetryLoad` po błędzie → Loading → Success, błąd FakeRepository → stan Error, filtr STARRED bez gwiazdek → stan Empty(NO_RESULTS). Używa MockK dla `InboxRepository` lub `FakeInboxRepository` z flagą błędu. Plik: `app/src/test/java/com/edoreczenia/feature/inbox/presentation/InboxViewModelTest.kt`

  > **Zależność**: T012, T031.

---

## Phase 13: Polish — Weryfikacja końcowa

**Cel**: Ostateczna weryfikacja zgodności wizualnej, spójności kodu i poprawności buildowania.

- [ ] T035 [P] Zweryfikuj kolory w motywie (`ui/theme/Color.kt` lub `Theme.kt`): upewnij się, że `primary=#001e40`, `primaryContainer=#003366`, `secondaryContainer=#fd8b00` są ustawione zgodnie z planem. Jeśli brakuje — dodaj/zaktualizuj. Plik: `app/src/main/java/com/edoreczenia/ui/theme/Color.kt`

- [ ] T036 [P] Zweryfikuj, że `androidx.compose.material.icons.extended` jest w `dependencies` w `app/build.gradle.kts` (wymagane dla ikon Extended: Drafts, StarBorder, CloudOff, SearchOff, Archive, ManageSearch, Outbox itp.). Jeśli brakuje — dodaj `implementation(libs.androidx.compose.material.icons.extended)`.

- [ ] T037 Przeprowadź `./gradlew test` — wszystkie testy jednostkowe muszą przejść (T031–T034). Napraw ewentualne błędy kompilacji lub testów.

- [ ] T038 Przeprowadź pełne ręczne testy ekranu zgodnie z `specs/002-inbox-odebrane/quickstart.md` sekcje "Weryfikacja filtrów", "Weryfikacja wizualna wiadomości", "Weryfikacja pull-to-refresh", "Weryfikacja badge'u", "Weryfikacja oznaczania gwiazdką".

---

## Zależności między fazami (podsumowanie)

```
Phase 1 (T001, T002)
    ↓
Phase 2 (T003, T004, T005)
    ↓
Phase 3 (T006, T007, T008)
    ↓
Phase 4 (T009, T010, T011 → T012 → T013, T014)
    ↓
Phase 5 (T015, T016, T017 → T018)
    ↓
Phase 6 (T019, T020 → T021)   Phase 7 (T022 → T023)   Phase 8 (T024 → T025)
    ↓                               ↓                           ↓
Phase 9 (T026) ←─────────────────────────────────────────────────
    ↓
Phase 10 (T027)
    ↓
Phase 11 (T028 → T029 → T030)
    ↓
Phase 12 (T031, T032, T033 → T034)   ← można wykonywać równolegle z Phase 5–11
    ↓
Phase 13 (T035, T036 → T037 → T038)
```

**Uwaga**: Testy (Phase 12) mogą być pisane równolegle z implementacją komponentów UI (Phase 5–11), gdy tylko ich zależności domenowe (Phase 3–4) są gotowe.

---

## Równoległa realizacja — przykłady

| Można wykonać równolegle | Warunek |
|---|---|
| T003, T004 | Niezależne pliki |
| T007, T008 | Niezależne pliki, oba zależą od T005 |
| T010, T011 | Niezależne pliki, T010 zależy od T004 |
| T014, T016, T017 | Niezależne komponenty UI bez zależności między sobą |
| T019, T020 | Niezależne composable, oba wymagają T018 |
| T024, T022 | Niezależne komponenty, T022 wymaga T009 |
| T031, T032, T033 | Niezależne pliki testowe |

---

## Scope check

Wszystkie zadania mieszczą się w zakresie `002-inbox-odebrane`. Żadne zadanie nie wychodzi poza:
- `app/src/main/java/com/edoreczenia/feature/inbox/`
- `app/src/main/java/com/edoreczenia/MainActivity.kt` (tylko integracja nawigacji)
- `app/src/main/res/values/strings.xml` (tylko nowe wpisy inbox_*)
- `app/src/test/java/com/edoreczenia/feature/inbox/`

Elementy out-of-scope (Room, Hilt, Backend API, InboxDetailScreen, ekrany innych zakładek) nie są uwzględnione.

