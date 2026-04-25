# Quickstart: 003-do-wysylki — Ekran Do wysyłki (Outbox)

**Phase 1 Output** | **Date**: 2026-04-25

---

## Cel

Przewodnik dla developera rozpoczynającego implementację feature'u `003-do-wysylki`.

---

## Wymagania wstępne

- Ukończony feature `001-user-auth` (AppError, Theme, nawigacja auth)
- Ukończony feature `002-inbox-odebrane` (InboxScreen, AppNavGraph z destynacją `inbox`, BottomNavBar)
- Android Studio z Kotlin 2.3.20+, compileSdk 36
- Urządzenie / emulator z Android 14+ (minSdk 34)

---

## Kolejność implementacji (rekomendowana)

### Krok 1 — Domain: model i kontrakt

1. Utwórz `OutboxStatus.kt` w `feature/outbox/domain/model/`
2. Utwórz `OutboxItem.kt` w `feature/outbox/domain/model/`
3. Utwórz `OutboxRepository.kt` w `feature/outbox/domain/repository/`
4. Utwórz `GetOutboxItemsUseCase.kt` w `feature/outbox/domain/usecase/`
5. Utwórz `RefreshOutboxItemsUseCase.kt` w `feature/outbox/domain/usecase/`

### Krok 2 — Data: FakeRepository

6. Utwórz `FakeOutboxRepository.kt` w `feature/outbox/data/repository/`
   - Zainicjalizuj `_items = MutableStateFlow(OutboxMockData.ITEMS)`
   - Zaimplementuj `getItems()` → `_items.asStateFlow()`
   - Zaimplementuj `refresh()` → `delay(1500)` + `Result.success(Unit)`

### Krok 3 — Presentation: stan i ViewModel

7. Utwórz `OutboxUiState.kt`, `OutboxAction.kt`, `OutboxEffect.kt`
8. Utwórz `OutboxViewModel.kt` z logiką inicjalizacji i obsługą akcji
9. Utwórz `OutboxViewModelFactory.kt`

### Krok 4 — Presentation: komponenty UI

10. Utwórz `OutboxTopAppBar.kt`
11. Utwórz `OutboxStatusBanner.kt`
12. Utwórz `OutboxQuickActionsBar.kt`
13. Utwórz `OutboxItemCard.kt`
14. Utwórz `OutboxLoadingState.kt`, `OutboxEmptyState.kt`, `OutboxErrorState.kt`
15. Utwórz `OutboxBottomNavBar.kt`

### Krok 5 — Ekran główny i placeholder szczegółów

16. Utwórz `OutboxScreen.kt` (Scaffold + PullToRefreshBox + renderowanie stanów)
17. Utwórz `OutboxDetailPlaceholderScreen.kt`

### Krok 6 — Nawigacja

18. Dodaj destynacje `"outbox"` i `"outbox_detail/{itemId}"` do `AppNavGraph.kt`
19. Zaktualizuj handlery BottomNavBar w `InboxBottomNavBar` (kliknięcie „Do wysyłki")
20. Zaktualizuj handlery BottomNavBar w `OutboxBottomNavBar` (kliknięcie „Poczta")

### Krok 7 — Strings

21. Dodaj nowe klucze do `app/src/main/res/values/strings.xml` (lista w `data-model.md`)

### Krok 8 — Testy

22. Zaimplementuj testy jednostkowe (kolejność: FakeRepo → UseCases → ViewModel)

---

## Weryfikacja po implementacji

- [ ] Ekran „Do wysyłki" wyświetla 4 przykładowe rekordy bez połączenia z siecią
- [ ] Baner statusu pokazuje liczbę rekordów i bieżący czas
- [ ] Kliknięcie „Synchronizuj" i „Odśwież listę" wywołuje loading (1–2 sek.) → reload
- [ ] Pull-to-refresh wywołuje ten sam efekt co „Odśwież listę"
- [ ] „Zaznacz wszystko" i „Wyślij" są widoczne, ale niereaktywne
- [ ] Karta z `SEND_ERROR` pokazuje czerwoną krawędź i komunikat błędu
- [ ] Karta z `PENDING_APPROVAL` pokazuje pomarańczową krawędź, brak błędu
- [ ] Karta z `WAITING` pokazuje szarą krawędź, brak błędu
- [ ] Kliknięcie karty → ekran szczegółów (placeholder) → Back → lista
- [ ] BottomNavBar z aktywną zakładką „Do wysyłki"
- [ ] Zakładka „Poczta" w BottomNav → powrót do InboxScreen
- [ ] Brak hardcoded stringów w kodzie Kotlin / Composable
- [ ] Brak logiki biznesowej w composable
- [ ] Wszystkie testy przechodzą: `./gradlew test`

---

## Uruchamianie testów

```shell
# Testy jednostkowe feature outbox
./gradlew :app:test --tests "com.edoreczenia.feature.outbox.*"

# Wszystkie testy
./gradlew test
```

---

## Pliki referencyjne

| Zasób | Ścieżka |
|---|---|
| Makieta ekranu | `docs/screens/do_wysy_ki/screen.png` |
| Kod HTML makiety | `docs/screens/do_wysy_ki/code.html` |
| Spec feature | `specs/003-do-wysylki/spec.md` |
| Data model | `specs/003-do-wysylki/data-model.md` |
| Research | `specs/003-do-wysylki/research.md` |
| Kontrakt nawigacji | `specs/003-do-wysylki/contracts/navigation-contract.md` |
| Referencyjny plan (inbox) | `specs/002-inbox-odebrane/plan.md` |
| Konstytucja | `.specify/memory/constitution.md` |

