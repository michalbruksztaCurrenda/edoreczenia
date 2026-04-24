# Quickstart: 002-inbox-odebrane — Ekran Odebrane (Inbox)

**Data**: 2026-04-24 | **Feature**: `002-inbox-odebrane`

---

## Wymagania wstępne

- Feature `001-user-auth` zaimplementowany (lub co najmniej ekran logowania i `LoginEffect.NavigateToMain`)
- Android Studio Ladybug+ z obsługą Compose
- minSdk 34, compileSdk 36, JVM 17

---

## Jak uruchomić ekran Odebrane

### 1. Sprawdź nawigację po zalogowaniu

Po zalogowaniu `LoginViewModel` emituje `LoginEffect.NavigateToMain`. Upewnij się, że `AppNavGraph` (lub odpowiednik w `MainActivity`) obsługuje trasę `"inbox"`:

```kotlin
// AppNavGraph.kt lub MainActivity.kt
composable("inbox") {
    InboxScreen(
        navController = navController,
        viewModel = viewModel(
            factory = InboxViewModelFactory(
                getInboxMessagesUseCase = GetInboxMessagesUseCase(FakeInboxRepository()),
                toggleStarUseCase = ToggleStarUseCase(FakeInboxRepository())
            )
        )
    )
}
```

> **Uwaga**: `FakeInboxRepository` powinien być jedną instancją współdzieloną między use case'ami, aby `toggleStar` wpływał na dane zwracane przez `getMessages`.

### 2. Prawidłowe wstrzyknięcie FakeInboxRepository

```kotlin
// InboxViewModelFactory.kt
class InboxViewModelFactory(
    private val getInboxMessagesUseCase: GetInboxMessagesUseCase,
    private val toggleStarUseCase: ToggleStarUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InboxViewModel(getInboxMessagesUseCase, toggleStarUseCase) as T
    }
}

// Użycie w NavGraph — jedna instancja repository
val repository = FakeInboxRepository()
val factory = InboxViewModelFactory(
    getInboxMessagesUseCase = GetInboxMessagesUseCase(repository),
    toggleStarUseCase = ToggleStarUseCase(repository)
)
```

---

## Jak weryfikować 5 stanów UI

### Stan Loading
Stan Loading jest emitowany przy inicjalizacji ViewModel przed pierwszą emisją danych. Aby go zobaczyć wyraźnie, można tymczasowo zwiększyć opóźnienie w `FakeInboxRepository.getMessages()` (np. `delay(2000)`).

### Stan Success
Domyślny stan po uruchomieniu. Wyświetla 4 rekordy mockowe.

### Stan Empty — wariant B (brak wyników)
1. Kliknij filtr "Ważne"
2. Jeśli żadna wiadomość nie ma `isStarred=true`, pojawi się stan Empty z wariantem B
3. Alternatywnie: wpisz w polu wyszukiwania frazę, która nie pasuje do żadnej wiadomości (np. "xxxxxx")

### Stan Empty — wariant A (koniec wiadomości)
Wymaga pustej listy mockowej (`MOCK_MESSAGES = emptyList()`). Tymczasowo zmień dane w `FakeInboxRepository` na potrzeby weryfikacji.

### Stan Error
W `FakeInboxRepository` ustaw flagę `shouldSimulateError = true` i wywołaj odświeżenie lub uruchom aplikację z tą flagą.

---

## Weryfikacja filtrów

| Akcja | Oczekiwany wynik |
|---|---|
| Kliknij "Wszystkie" | 4 wiadomości (KM 1/23, GKM 45/22, KM 124/23, KMS 7/23) |
| Kliknij "Nieprzeczytane" | 2 wiadomości (KM 1/23, KM 124/23) |
| Kliknij "Ważne" | 1 wiadomość (GKM 45/22 — Ministerstwo Finansów) |
| Wpisz "Sąd" w wyszukiwarce | 1 wiadomość (KM 1/23 — Sąd Rejonowy) |
| Wpisz "Kowalski" | 1 wiadomość (KM 124/23 — Jan Kowalski) |
| Wpisz "wezwanie" | 2 wiadomości (KM 1/23, GKM 45/22) |

---

## Weryfikacja wizualna wiadomości

### Nieprzeczytana (msg-001, msg-003)
- [ ] Lewy pionowy pasek koloru `#fd8b00` (4dp szerokości)
- [ ] Pomarańczowa kropka statusu (8×8dp, `#fd8b00`)
- [ ] Ikona `mail` (wypełniona)
- [ ] Pogrubiony temat (`FontWeight.SemiBold`)
- [ ] Nadawca w kolorze `primary` (`#001e40`)
- [ ] Badge numeru sprawy w kolorze `primary`/`secondary`

### Przeczytana (msg-002, msg-004)
- [ ] Brak lewego paska
- [ ] Brak kropki statusu (pusta przestrzeń 8dp)
- [ ] Ikona `drafts` (otwarta koperta, outline)
- [ ] Normalny tekst tematu
- [ ] Nadawca w kolorze `onSurfaceVariant`
- [ ] Badge numeru sprawy w kolorze `outline`

### Oznaczona gwiazdką (msg-002)
- [ ] Ikona `star` wypełniona (`Icons.Filled.Star`)
- [ ] Kolor gwiazdki `secondaryContainer` (`#fd8b00`)

### Bez gwiazdki (msg-001, msg-003, msg-004)
- [ ] Ikona `star` outline (`Icons.Outlined.StarBorder`)
- [ ] Kolor gwiazdki `outline` (szary)

---

## Weryfikacja pull-to-refresh

1. Przeciągnij listę w dół
2. Powinien pojawić się wskaźnik odświeżania
3. Po 500–1000 ms wskaźnik zniknie i lista powróci do normalnego stanu

---

## Weryfikacja badge'u "X nowe"

Przy domyślnych danych mockowych badge powinien wyświetlać "**2 nowe**" (msg-001 i msg-003 mają `isRead=false`).

---

## Weryfikacja oznaczania gwiazdką

1. Tapnij ikonę gwiazdki przy msg-001 (bez gwiazdki)
2. Ikona powinna zmienić się na wypełnioną gwiazdkę `#fd8b00`
3. Tapnij ponownie — gwiazdka wraca do outline
4. Kliknij filtr "Ważne" — msg-001 powinien teraz pojawić się w filtrze

---

## Uruchamianie testów

```bash
# Testy jednostkowe
./gradlew test

# Konkretny moduł/klasa
./gradlew :app:testDebugUnitTest --tests "com.edoreczenia.feature.inbox.*"
```

---

## Znane ograniczenia (pierwsze wydanie)

- Oznaczanie gwiazdką NIE jest trwałe — reset po zamknięciu aplikacji
- `displayDate` jest statycznym stringiem ("Wczoraj" zawsze wyświetla się jako "Wczoraj")
- Tapnięcie wiadomości wyświetla placeholder Toast — ekran szczegółów nie jest zaimplementowany
- Zakładki dolnej nawigacji (Do wysyłki, ADE, Ustawienia) są wizualne — nie nawigują
- FAB (ikona edycji) wyświetla Toast — tworzenie wiadomości nie jest zaimplementowane

