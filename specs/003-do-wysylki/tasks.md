# Tasks: 003-do-wysylki — Ekran Do wysyłki (Outbox)

**Input**: Design documents from `/specs/003-do-wysylki/`
**Prerequisites**: plan.md ✅  spec.md ✅  research.md ✅  data-model.md ✅  contracts/navigation-contract.md ✅  quickstart.md ✅

**Uwaga dotycząca AppError**: `AppError.kt` istnieje w `core/error/` (z feature 001-user-auth). Plan odwołuje się do `AppError.Technical` i `AppError.Network`. Developer powinien zweryfikować dostępność `AppError.Technical` — jeśli istnieje wyłącznie `AppError.Unknown`, należy użyć tego wariantu (nie zmieniać architektury).

---

## Format: `[ID] [P?] [Story?] Opis z ścieżką pliku`

- **[P]**: Można wykonać równolegle z innymi zadaniami tej fazy (różne pliki, brak nierozwiązanych zależności)
- **[Story]**: Przynależność do User Story z spec.md
- Faza Setup / Foundational: brak etykiety Story

---

## Phase 1: Setup — Struktura pakietów i zasoby

**Cel**: Przygotowanie szkieletu katalogów i zasobów stringów. Blokuje wszystkie fazy domenowe i prezentacyjne.

- [ ] T001 Utwórz strukturę pakietów feature outbox: `app/src/main/java/com/edoreczenia/feature/outbox/domain/model/`, `domain/repository/`, `domain/usecase/`, `data/repository/`, `presentation/components/` oraz `app/src/test/java/com/edoreczenia/feature/outbox/domain/usecase/`, `data/repository/`, `presentation/`

- [ ] T002 [P] Dodaj nowe klucze strings.xml dla feature outbox (`outbox_title`, `outbox_empty_title`, `outbox_empty_subtitle`, `outbox_error_title`, `outbox_error_retry_button`, `outbox_status_send_error`, `outbox_status_pending_approval`, `outbox_status_waiting`, `outbox_banner_unsent_count`, `outbox_banner_last_sync`, `outbox_banner_sync_button`, `outbox_quick_action_select_all`, `outbox_quick_action_refresh`, `outbox_quick_action_send`, `outbox_detail_placeholder`, `outbox_back`) zgodnie z sekcją "Nowe klucze strings.xml" w `plan.md` — plik: `app/src/main/res/values/strings.xml`

---

## Phase 2: Foundational — Modele domenowe i kontrakt repozytorium

**Cel**: Fundament domeny — modele i interfejs kontraktu. Blokuje use case'y, FakeRepository i ViewModel.

> ⚠️ CRITICAL: Fazy 3+ mogą rozpocząć się dopiero po ukończeniu tej fazy.

- [ ] T003 Utwórz `OutboxStatus` enum z wartościami `SEND_ERROR`, `PENDING_APPROVAL`, `WAITING` w `app/src/main/java/com/edoreczenia/feature/outbox/domain/model/OutboxStatus.kt`

- [ ] T004 [P] Utwórz `OutboxItem` data class (pola: `id: String`, `caseNumber: String`, `recipientName: String`, `subject: String`, `status: OutboxStatus`, `errorMessage: String?`) w `app/src/main/java/com/edoreczenia/feature/outbox/domain/model/OutboxItem.kt`

  > **Zależność**: T003 musi być gotowe przed T004.

- [ ] T005 Utwórz interfejs `OutboxRepository` z metodami: `fun getItems(): Flow<List<OutboxItem>>`, `suspend fun refresh(): Result<Unit>` w `app/src/main/java/com/edoreczenia/feature/outbox/domain/repository/OutboxRepository.kt`

  > **Zależność**: T004 musi być gotowe przed T005.

---

## Phase 3: Foundational — Use case'y domeny

**Cel**: Logika domenowa gotowa do użycia przez ViewModel. Blokuje ViewModel i testy.

- [ ] T006 Utwórz `GetOutboxItemsUseCase`: przyjmuje `repository: OutboxRepository`, `operator fun invoke(): Flow<List<OutboxItem>>` — deleguje do `repository.getItems()` bez transformacji w `app/src/main/java/com/edoreczenia/feature/outbox/domain/usecase/GetOutboxItemsUseCase.kt`

  > **Zależność**: T005.

- [ ] T007 [P] Utwórz `RefreshOutboxItemsUseCase`: przyjmuje `repository: OutboxRepository`, `suspend operator fun invoke(): Result<Unit>` — deleguje do `repository.refresh()`, opakowuje nieoczekiwane wyjątki w `Result.failure(AppError.Technical(...))` (lub `AppError.Unknown` — patrz uwaga wstępna) w `app/src/main/java/com/edoreczenia/feature/outbox/domain/usecase/RefreshOutboxItemsUseCase.kt`

  > **Zależność**: T005.

---

## Phase 4: Foundational — Warstwa data (FakeRepository)

**Cel**: Implementacja mockowego repozytorium z 4 przykładowymi rekordami. Blokuje ViewModel i testy integracyjne.

- [ ] T008 Utwórz `FakeOutboxRepository` implementujący `OutboxRepository`:
  - `companion object OutboxMockData` z 4 rekordami (out-001 SEND_ERROR / out-002 PENDING_APPROVAL / out-003 WAITING / out-004 WAITING — wartości zgodne z sekcją "Mock data strategy" w `plan.md`)
  - `private val _items = MutableStateFlow<List<OutboxItem>>(OutboxMockData.ITEMS)`
  - `getItems()` → `_items.asStateFlow()`
  - `refresh()` → `delay(1500L)` → `Result.success(Unit)`; gdy `shouldSimulateError = true` → `Result.failure(AppError.Network(...))`
  - Pole `var shouldSimulateError: Boolean = false` (do testów)
  - Plik: `app/src/main/java/com/edoreczenia/feature/outbox/data/repository/FakeOutboxRepository.kt`

  > **Zależność**: T003, T004, T005.

---

## Phase 5: User Story 1 — Przeglądanie listy pozycji do wysyłki (P1) 🎯 MVP

**Cel**: Pełny stack presentation gotowy do wyświetlenia listy 4 mockowych pozycji ze wszystkimi stanami UI (Initial / Loading / Success / Empty / Error).

**Niezależny test**: Uruchomić aplikację, przejść do sekcji „Do wysyłki". Na ekranie pojawia się lista z 4 przykładowymi pozycjami zawierającymi adresata, temat, numer sprawy i badge statusu.

- [ ] T009 [US1] Utwórz `OutboxUiState` sealed class (Initial, Loading, Success z polami `items: List<OutboxItem>` / `unsendCount: Int` / `lastSyncTime: String` / `isRefreshing: Boolean`, Empty, Error z polem `message: String`) w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/OutboxUiState.kt`

  > **Zależność**: T004.

- [ ] T010 [P] [US1] Utwórz `OutboxAction` sealed class (Refresh, Synchronize, SelectAll, Send, `ItemClicked(val itemId: String)`, RetryLoad) w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/OutboxAction.kt`

- [ ] T011 [P] [US1] Utwórz `OutboxEffect` sealed class (`NavigateToDetail(val itemId: String)`) w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/OutboxEffect.kt`

- [ ] T012 [US1] Utwórz `OutboxViewModel`:
  - Zależności wstrzykiwane: `getOutboxItemsUseCase: GetOutboxItemsUseCase`, `refreshOutboxItemsUseCase: RefreshOutboxItemsUseCase`
  - `_uiState: MutableStateFlow<OutboxUiState>` (domyślnie `Initial`)
  - `_lastSyncTime: MutableStateFlow<String>` (bieżący czas `HH:mm` przy inicjalizacji)
  - `_isRefreshing: MutableStateFlow<Boolean>` (domyślnie `false`)
  - `_effects: Channel<OutboxEffect>` (Channel.BUFFERED)
  - `val effects: Flow<OutboxEffect>` = `_effects.receiveAsFlow()`
  - `init`: ustawia `Loading`, uruchamia `getOutboxItemsUseCase().collectLatest` → pusta lista → `Empty`, niepusta → `Success(items, unsendCount = items.size, lastSyncTime, isRefreshing = false)`, błąd → `Error(message)`
  - `fun onAction(action: OutboxAction)`: Refresh/Synchronize → `_isRefreshing = true` → `refreshOutboxItemsUseCase()` → `_isRefreshing = false` → aktualizacja `_lastSyncTime`; SelectAll → no-op; Send → no-op; `ItemClicked(id)` → `effects.send(NavigateToDetail(id))`; RetryLoad → reset do `Loading` + re-kolekcja
  - Plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/OutboxViewModel.kt`

  > **Zależność**: T006, T007, T009, T010, T011.

- [ ] T013 [P] [US1] Utwórz `OutboxViewModelFactory` implementujący `ViewModelProvider.Factory`, przyjmujący `getOutboxItemsUseCase` i `refreshOutboxItemsUseCase`, tworzący `OutboxViewModel` w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/OutboxViewModelFactory.kt`

  > **Zależność**: T012.

- [ ] T014 [P] [US1] Utwórz `OutboxLoadingState` composable: `CircularProgressIndicator` wycentrowany w `Box(Modifier.fillMaxSize())` w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxLoadingState.kt`

- [ ] T015 [P] [US1] Utwórz `OutboxEmptyState` composable: ikona (Material Symbol `outbox` lub `check_circle`), tytuł z `stringResource(R.string.outbox_empty_title)`, podtytuł z `stringResource(R.string.outbox_empty_subtitle)`, wyśrodkowany pionowo i poziomo w `Box(Modifier.fillMaxSize())` w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxEmptyState.kt`

  > **Zależność**: T002.

- [ ] T016 [P] [US1] Utwórz `OutboxErrorState` composable: ikona `error_outline`, tytuł z `stringResource(R.string.outbox_error_title)`, przycisk „Spróbuj ponownie" wywołujący `onRetry: () -> Unit`, wyśrodkowany w `Column(Modifier.fillMaxSize())` w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxErrorState.kt`

  > **Zależność**: T002.

- [ ] T017 [P] [US1] Utwórz `OutboxTopAppBar` composable: `TopAppBar` Material 3 z tytułem `stringResource(R.string.outbox_title)`, ikoną menu (hamburger, no-op) po lewej, ikoną wyszukiwania (no-op) po prawej, tło `MaterialTheme.colorScheme.primary`, tekst i ikony `onPrimary` w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxTopAppBar.kt`

  > **Zależność**: T002.

- [ ] T018 [US1] Utwórz `OutboxScreen` composable: przyjmuje `navController: NavController`, tworzy `OutboxViewModel` przez `OutboxViewModelFactory` (wired z `FakeOutboxRepository`), zbiera `uiState` i `effects` przez `collectAsStateWithLifecycle` / `LaunchedEffect`, obsługuje `NavigateToDetail` → `navController.navigate("outbox_detail/$itemId")`. Scaffold z `topBar: OutboxTopAppBar`, `bottomBar: OutboxBottomNavBar`. Content: `when(uiState)` → Initial: brak renderowania; Loading: `OutboxLoadingState`; Success: `PullToRefreshBox` + `OutboxStatusBanner` + `OutboxQuickActionsBar` + `LazyColumn` z `OutboxItemCard`; Empty: `OutboxEmptyState`; Error: `OutboxErrorState`. Plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/OutboxScreen.kt`

  > **Zależność**: T012, T013, T014, T015, T016, T017 + T019 (OutboxBottomNavBar) + T020 (OutboxItemCard).

---

## Phase 6: User Story 2 — Rozróżnianie statusów przesyłek (P2)

**Cel**: Wizualne rozróżnienie statusów na karcie pozycji — kolorowa lewa krawędź + badge statusu + warunkowy komunikat błędu.

**Niezależny test**: Na liście widoczne są elementy z co najmniej 3 różnymi statusami. Każdy ma odmienną lewą krawędź i badge.

- [ ] T019 [P] [US2] Utwórz `OutboxBottomNavBar` composable: `NavigationBar` Material 3 z 4 pozycjami: Poczta (`mail`, `navController.navigate("inbox")`), Do wysyłki (`send`, aktywna, `indicatorColor = secondaryContainer`), ADE (`search`, no-op), Ustawienia (`settings`, no-op). Etykiety z `stringResource` (klucze inbox_bottom_nav_* z feature 002). Plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxBottomNavBar.kt`

  > **Zależność**: T002.

- [ ] T020 [US2] Utwórz `OutboxItemCard` composable: parametry `item: OutboxItem`, `onClick: (String) -> Unit`. Układ:
  - `Card` klikalny `onClick(item.id)` z `Modifier.fillMaxWidth()`
  - Lewa krawędź 4dp rysowana przez `Box` lub `Canvas`, kolor zależny od `item.status` (SEND_ERROR → `MaterialTheme.colorScheme.error`; PENDING_APPROVAL → `tertiaryFixedDim`; WAITING → `surfaceVariant`)
  - Chip/badge numeru sprawy: `SuggestionChip` z `item.caseNumber`, kolor wg statusu
  - Nazwa adresata: `Text(item.recipientName)` styl `bodyLarge` / `FontWeight.SemiBold`
  - Temat: `Text(item.subject)` styl `bodyMedium`, kolor `onSurfaceVariant`, `maxLines = 2`, `overflow = Ellipsis`
  - Badge statusu: `SuggestionChip` / `FilterChip` z etykietą tekstową z `stringResource` (`outbox_status_*`), kolor wg statusu
  - Komunikat błędu (warunkowy): `if (item.errorMessage != null) Text(item.errorMessage)` styl `bodySmall`, kolor `MaterialTheme.colorScheme.error`
  - Padding: `16.dp` horizontal, `12.dp` vertical; `Divider` pod kartą
  - Plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxItemCard.kt`

  > **Zależność**: T003, T004, T002.

---

## Phase 7: User Story 3 — Status baner z informacją o niewysłanych elementach (P2)

**Cel**: Baner statusu widoczny w stanie Success — liczba niewysłanych, czas ostatniej próby, przycisk Synchronizuj.

**Niezależny test**: Na ekranie widoczny baner z ikoną `sync_problem`, liczbą niewysłanych elementów, czasem ostatniej próby i przyciskiem synchronizacji.

- [ ] T021 [US3] Utwórz `OutboxStatusBanner` composable: parametry `unsendCount: Int`, `lastSyncTime: String`, `onSynchronize: () -> Unit`. Układ: poziomy `Row` z ikoną `sync_problem` (kolor `error`), kolumna tekstów (`%d niewysłanych elementów` z `outbox_banner_unsent_count`, `Ostatnia próba: HH:mm` z `outbox_banner_last_sync`), `TextButton` / `OutlinedButton` „Synchronizuj" (`outbox_banner_sync_button`) wywołujący `onSynchronize()`. Tło `errorContainer`, padding `16.dp` horizontal / `12.dp` vertical. Plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxStatusBanner.kt`

  > **Zależność**: T002.

---

## Phase 8: User Story 4 — Szybkie akcje (Quick Actions) (P3)

**Cel**: Poziomy pasek szybkich akcji widoczny w stanie Success — Zaznacz wszystko (no-op), Odśwież listę (Refresh), Wyślij (no-op).

**Niezależny test**: Pasek szybkich akcji jest widoczny pod banerem statusu. Kliknięcie „Odśwież listę" inicjuje odświeżenie.

- [ ] T022 [US4] Utwórz `OutboxQuickActionsBar` composable: parametry `onSelectAll: () -> Unit`, `onRefresh: () -> Unit`, `onSend: () -> Unit`. Układ: poziomy `Row` z `Spacer(Modifier.weight(1f))` pomiędzy elementami lub `Arrangement.SpaceEvenly`. 3 elementy jako `TextButton` z ikoną powyżej etykiety (lub `Column(icon, text)`): ikona `check_box_outline_blank` + `outbox_quick_action_select_all` → `onSelectAll()`; ikona `refresh` + `outbox_quick_action_refresh` → `onRefresh()`; ikona `send` + `outbox_quick_action_send` → `onSend()`. `Divider` pod paskiem. Plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxQuickActionsBar.kt`

  > **Zależność**: T002.

---

## Phase 9: User Story 5 — Przejście do szczegółów pozycji (P3)

**Cel**: Nawigacja list→detail działa: kliknięcie karty otwiera placeholder, Back wraca do listy.

**Niezależny test**: Kliknięcie elementu listy przenosi na ekran szczegółów. Ekran posiada przycisk cofnięcia, który wraca do listy.

- [ ] T023 [US5] Utwórz `OutboxDetailPlaceholderScreen` composable: parametry `itemId: String`, `navController: NavController`. Scaffold z `TopAppBar` (tytuł `stringResource(R.string.outbox_detail_placeholder)`, ikona `arrow_back` → `navController.popBackStack()`), content: `Text` wyśrodkowany „Szczegóły przesyłki wkrótce". Plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/OutboxDetailPlaceholderScreen.kt`

  > **Zależność**: T002.

---

## Phase 10: Integracja nawigacyjna

**Cel**: Wpięcie OutboxScreen i OutboxDetailPlaceholderScreen do AppNavGraph oraz aktualizacja BottomNavBar w istniejącym InboxBottomNavBar.

- [ ] T024 Dodaj destynacje `"outbox"` i `"outbox_detail/{itemId}"` (z `navArgument("itemId")` typu `NavType.StringType`) do `AppNavGraph.kt` zgodnie z kontraktem nawigacyjnym z `contracts/navigation-contract.md` w `app/src/main/java/com/edoreczenia/AppNavGraph.kt`

  > **Zależność**: T018, T023.

- [ ] T025 [P] Zaktualizuj `InboxBottomNavBar.kt`: handler kliknięcia zakładki „Do wysyłki" → `navController.navigate("outbox")` w `app/src/main/java/com/edoreczenia/feature/inbox/presentation/components/InboxBottomNavBar.kt`

  > **Zależność**: T024.

- [ ] T026 [P] Zaktualizuj `OutboxBottomNavBar.kt`: zweryfikuj/uzupełnij handler kliknięcia zakładki „Poczta" → `navController.navigate("inbox")` (jeśli nie został zaimplementowany w T019) w `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxBottomNavBar.kt`

  > **Zależność**: T019.

---

## Phase 11: Testy jednostkowe

**Cel**: Pokrycie testami FakeRepository, use case'ów i ViewModelu.

- [ ] T027 Zaimplementuj `FakeOutboxRepositoryTest`: test że `getItems()` emituje 4 rekordy mockowe; test że `refresh()` zwraca `Result.success(Unit)`; test że `refresh()` z `shouldSimulateError = true` zwraca `Result.failure`; test że lista po `refresh()` pozostaje niezmieniona. Plik: `app/src/test/java/com/edoreczenia/feature/outbox/data/repository/FakeOutboxRepositoryTest.kt`

  > **Zależność**: T008.

- [ ] T028 [P] Zaimplementuj `GetOutboxItemsUseCaseTest`: test że use case zwraca `Flow` emitujący listę z repozytorium; test zachowania przy pustej liście; test że use case nie modyfikuje kolejności ani zawartości listy. Plik: `app/src/test/java/com/edoreczenia/feature/outbox/domain/usecase/GetOutboxItemsUseCaseTest.kt`

  > **Zależność**: T006, T008.

- [ ] T029 [P] Zaimplementuj `RefreshOutboxItemsUseCaseTest`: test że use case zwraca `Result.success` przy poprawnym refresh; test że zwraca `Result.failure(AppError.Network)` przy błędzie sieciowym (via `shouldSimulateError = true`); test mapowania błędów na `AppError`. Plik: `app/src/test/java/com/edoreczenia/feature/outbox/domain/usecase/RefreshOutboxItemsUseCaseTest.kt`

  > **Zależność**: T007, T008.

- [ ] T030 Zaimplementuj `OutboxViewModelTest` z użyciem `kotlinx-coroutines-test` i `FakeOutboxRepository`:
  - Test stanu `Loading` po inicjalizacji (przed emisją z Flow)
  - Test stanu `Success` po emisji listy z `GetOutboxItemsUseCase`
  - Test stanu `Empty` gdy use case emituje pustą listę
  - Test stanu `Error` gdy kolekcja Flow rzuca wyjątek
  - Test akcji `Refresh`: `isRefreshing = true` → wywołuje use case → `isRefreshing = false`
  - Test akcji `Synchronize`: identyczne jak Refresh + aktualizacja `lastSyncTime`
  - Test akcji `SelectAll`: brak zmiany stanu (no-op)
  - Test akcji `Send`: brak zmiany stanu (no-op)
  - Test akcji `ItemClicked(id)`: emitowany efekt `NavigateToDetail(id)`
  - Test akcji `RetryLoad`: stan wraca do `Loading`
  - Test obliczenia `unsendCount` = liczba elementów na liście
  - Plik: `app/src/test/java/com/edoreczenia/feature/outbox/presentation/OutboxViewModelTest.kt`

  > **Zależność**: T012, T008.

---

## Phase 12: Polish — Weryfikacja zgodności z makietą

**Cel**: Końcowe dopasowanie wizualne do `docs/screens/do_wysy_ki/screen.png`.

- [ ] T031 Zweryfikuj i popraw wizualną zgodność `OutboxItemCard` z makietą: kolory krawędzi (`error`, `tertiaryFixedDim`, `surfaceVariant`), kolory badge'ów (`errorContainer`, `surfaceContainer`, `surfaceContainerHigh`), widoczność `errorMessage` tylko dla SEND_ERROR, typografia (`bodyLarge SemiBold` dla adresata, `bodyMedium` dla tematu) — plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxItemCard.kt`

- [ ] T032 [P] Zweryfikuj i popraw wizualną zgodność `OutboxStatusBanner` z makietą: ikona `sync_problem`, tło banera, układ elementów, padding — plik: `app/src/main/java/com/edoreczenia/feature/outbox/presentation/components/OutboxStatusBanner.kt`

- [ ] T033 [P] Zweryfikuj brak hardcoded stringów w całym module `feature/outbox` — wszystkie teksty UI muszą być pobierane z `stringResource()`

---

## Podsumowanie zależności między fazami

```
Phase 1 (Setup)
    └─► Phase 2 (Domain models + Repository interface)
            └─► Phase 3 (Use cases)
            └─► Phase 4 (FakeRepository)
                    └─► Phase 5 (Presentation stack: ViewModel + UI states + OutboxScreen)
                    └─► Phase 11 (Testy: FakeRepo + UseCases + ViewModel)
                            └─► Phase 6 (OutboxItemCard — statusy)
                            └─► Phase 7 (OutboxStatusBanner)
                            └─► Phase 8 (OutboxQuickActionsBar)
                            └─► Phase 9 (OutboxDetailPlaceholderScreen)
                                    └─► Phase 10 (Integracja nawigacyjna)
                                            └─► Phase 12 (Polish)
```

---

## Możliwości równoległe (per faza)

| Faza | Zadania równoległe |
|------|--------------------|
| Phase 1 | T001 ∥ T002 |
| Phase 2 | T003 → T004 ∥ (T005 po T004) |
| Phase 3 | T006 ∥ T007 |
| Phase 5 | T010 ∥ T011 ∥ T013 ∥ T014 ∥ T015 ∥ T016 ∥ T017 (po T009/T012) |
| Phase 6 | T019 ∥ T020 |
| Phase 11 | T027 → (T028 ∥ T029) → T030 |
| Phase 12 | T031 ∥ T032 ∥ T033 |

---

## Rekomendowane MVP

**MVP = Phase 1 + Phase 2 + Phase 3 + Phase 4 + Phase 5 + Phase 6 (T020) + Phase 10**

Daje to działający ekran z listą 4 mockowych pozycji, stanem loading/error/empty, nawigacją do placeholdera szczegółów i wpięciem do AppNavGraph — bez banera statusu i paska quick actions.

---

## Statystyki

| Metryka | Wartość |
|---------|---------|
| Łączna liczba tasków | 33 |
| Phase 1 (Setup) | 2 |
| Phase 2–4 (Foundational) | 6 |
| Phase 5 (US1 — P1 MVP) | 10 |
| Phase 6 (US2 — P2) | 2 |
| Phase 7 (US3 — P2) | 1 |
| Phase 8 (US4 — P3) | 1 |
| Phase 9 (US5 — P3) | 1 |
| Phase 10 (Integracja) | 3 |
| Phase 11 (Testy) | 4 |
| Phase 12 (Polish) | 3 |

