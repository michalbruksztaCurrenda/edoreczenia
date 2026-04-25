# Data Model: 003-do-wysylki — Ekran Do wysyłki (Outbox)

**Phase 1 Output** | **Date**: 2026-04-25

---

## Encje domenowe

### `OutboxItem`

Reprezentuje pojedynczą pozycję w kolejce do wysyłki.

| Pole | Typ | Nullable | Opis |
|---|---|---|---|
| `id` | `String` | Nie | Unikalny identyfikator pozycji |
| `caseNumber` | `String` | Nie | Numer/sygnatura sprawy (np. „KM 124/23") |
| `recipientName` | `String` | Nie | Nazwa adresata (osoba lub podmiot) |
| `subject` | `String` | Nie | Temat / tytuł przesyłki |
| `status` | `OutboxStatus` | Nie | Enum statusu przesyłki |
| `errorMessage` | `String?` | Tak | Komunikat błędu — tylko dla `SEND_ERROR`, `null` w pozostałych |

**Reguły walidacji**:
- `id` — niepusty, unikalny w zakresie listy
- `caseNumber` — niepusty
- `recipientName` — niepusty
- `subject` — niepusty
- `errorMessage` — jeśli `status != SEND_ERROR`, wartość powinna być `null` (soft rule, UI ignoruje gdy null)

---

### `OutboxStatus`

Enum reprezentujący stan przesyłki w kolejce do wysyłki.

| Wartość | Etykieta UI | Kolor krawędzi (token M3) | Kolor badge (token M3) |
|---|---|---|---|
| `SEND_ERROR` | „Błąd wysyłki" | `colorScheme.error` | `colorScheme.errorContainer` |
| `PENDING_APPROVAL` | „Do zatwierdzenia" | `colorScheme.tertiary` | `colorScheme.surfaceContainer` |
| `WAITING` | „Oczekuje na wysyłkę" | `colorScheme.surfaceVariant` | `colorScheme.surfaceContainerHigh` |

**Fallback**: nieznany status → traktowany jak `WAITING`.

---

## Kontrakty repozytorium i use case'ów

### `OutboxRepository` — interfejs

```
interface OutboxRepository {
    fun getItems(): Flow<List<OutboxItem>>
    suspend fun refresh(): Result<Unit>
}
```

Przyszłe rozszerzenia (poza zakresem tej iteracji):
- `suspend fun getItemById(id: String): Result<OutboxItem>`
- `suspend fun sendItems(ids: List<String>): Result<Unit>`
- `fun getItems(page: Int, size: Int): Flow<PagingData<OutboxItem>>`

---

### `GetOutboxItemsUseCase`

**Sygnatura**: `operator fun invoke(): Flow<List<OutboxItem>>`

**Zależności**: `OutboxRepository`

**Logika**: Deleguje do `repository.getItems()`. Brak filtrowania w tej wersji.

---

### `RefreshOutboxItemsUseCase`

**Sygnatura**: `suspend operator fun invoke(): Result<Unit>`

**Zależności**: `OutboxRepository`

**Logika**: Deleguje do `repository.refresh()`. Błędy opakowuje w `AppError`.

---

## Stany UI

### `OutboxUiState`

```
sealed class OutboxUiState {
    object Initial : OutboxUiState()
    object Loading : OutboxUiState()
    data class Success(
        val items: List<OutboxItem>,
        val unsendCount: Int,
        val lastSyncTime: String,
        val isRefreshing: Boolean
    ) : OutboxUiState()
    object Empty : OutboxUiState()
    data class Error(val message: String) : OutboxUiState()
}
```

**Przejścia stanów**:
```
Initial → Loading → Success(items)
                  → Empty
                  → Error(message)
Success(isRefreshing=false) → Success(isRefreshing=true) → Success(isRefreshing=false)
Error → Loading (RetryLoad) → Success / Empty / Error
```

---

## Akcje i efekty

### `OutboxAction`

```
sealed class OutboxAction {
    object Refresh : OutboxAction()
    object Synchronize : OutboxAction()
    object SelectAll : OutboxAction()        // no-op w mock
    object Send : OutboxAction()             // no-op w mock
    data class ItemClicked(val itemId: String) : OutboxAction()
    object RetryLoad : OutboxAction()
}
```

### `OutboxEffect`

```
sealed class OutboxEffect {
    data class NavigateToDetail(val itemId: String) : OutboxEffect()
}
```

---

## Mockowe dane (`OutboxMockData`)

| id | caseNumber | recipientName | subject | status | errorMessage |
|----|------------|---------------|---------|--------|--------------|
| `out-001` | KM 124/23 | Janusz Kowalski | Wezwanie do zapłaty - Zaległe alimenty | `SEND_ERROR` | „Brak podpisu kwalifikowanego" |
| `out-002` | GKM 45/24 | Anna Nowak | Postanowienie o zajęciu wynagrodzenia | `PENDING_APPROVAL` | null |
| `out-003` | KM 902/22 | PKO Bank Polski S.A. | Zapytanie o stan konta dłużnika | `WAITING` | null |
| `out-004` | KM 11/24 | Marek Wójcik | Postanowienie o umorzeniu | `WAITING` | null |

---

## Nowe klucze `strings.xml`

| Klucz | Wartość PL |
|---|---|
| `outbox_title` | Do wysyłki |
| `outbox_empty_title` | Brak pozycji do wysyłki |
| `outbox_empty_subtitle` | Wszystkie przesyłki zostały wysłane |
| `outbox_error_title` | Nie udało się pobrać kolejki |
| `outbox_error_retry_button` | Spróbuj ponownie |
| `outbox_status_send_error` | Błąd wysyłki |
| `outbox_status_pending_approval` | Do zatwierdzenia |
| `outbox_status_waiting` | Oczekuje na wysyłkę |
| `outbox_banner_unsent_count` | %d niewysłanych elementów |
| `outbox_banner_last_sync` | Ostatnia próba: %s |
| `outbox_banner_sync_button` | Synchronizuj |
| `outbox_quick_action_select_all` | Zaznacz wszystko |
| `outbox_quick_action_refresh` | Odśwież listę |
| `outbox_quick_action_send` | Wyślij |
| `outbox_detail_placeholder` | Szczegóły przesyłki wkrótce |
| `outbox_back` | Wróć do Do wysyłki |

