# Data Model: 002-inbox-odebrane — Ekran Odebrane (Inbox)

**Data**: 2026-04-24 | **Feature**: `002-inbox-odebrane`

---

## Modele domenowe

### `InboxMessage`

Pakiet: `com.edoreczenia.feature.inbox.domain.model`

```kotlin
data class InboxMessage(
    val id: String,
    val caseNumber: String,
    val senderName: String,
    val subject: String,
    val preview: String,
    val displayDate: String,
    val isRead: Boolean,
    val isStarred: Boolean
)
```

**Opis pól**:

| Pole | Typ | Ograniczenia | Opis |
|---|---|---|---|
| `id` | `String` | niepusty, unikalny | Identyfikator przesyłki (np. "msg-001") |
| `caseNumber` | `String` | niepusty | Sygnatura sprawy (np. "KM 1/23", "GKM 45/22") |
| `senderName` | `String` | niepusty | Pełna nazwa nadawcy / jednostki |
| `subject` | `String` | niepusty | Temat / tytuł przesyłki |
| `preview` | `String` | może być pusty | Fragment treści wiadomości — pełny tekst, skracany przez UI (`maxLines=1`, `overflow=Ellipsis`) |
| `displayDate` | `String` | niepusty | Preformatowana data/czas do wyświetlenia ("10:45", "Wczoraj", "15 Lis") |
| `isRead` | `Boolean` | — | Status odczytu: `true` = przeczytana, `false` = nieprzeczytana |
| `isStarred` | `Boolean` | — | Oznaczenie ważności: `true` = oznaczona gwiazdką |

---

### `InboxFilter`

Pakiet: `com.edoreczenia.feature.inbox.domain.model`

```kotlin
enum class InboxFilter {
    ALL,
    UNREAD,
    STARRED
}
```

| Wartość | Etykieta UI (strings.xml) | Logika filtrowania |
|---|---|---|
| `ALL` | `inbox_filter_all` = "Wszystkie" | brak filtrowania |
| `UNREAD` | `inbox_filter_unread` = "Nieprzeczytane" | `message.isRead == false` |
| `STARRED` | `inbox_filter_starred` = "Ważne" | `message.isStarred == true` |

---

### `EmptyReason`

Pakiet: `com.edoreczenia.feature.inbox.presentation`

```kotlin
enum class EmptyReason {
    NO_MESSAGES,   // pusta lista bez aktywnych filtrów
    NO_RESULTS     // brak wyników dla aktywnego filtra lub wyszukiwania
}
```

---

### `InboxUiState`

Pakiet: `com.edoreczenia.feature.inbox.presentation`

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
```

---

### `InboxAction`

Pakiet: `com.edoreczenia.feature.inbox.presentation`

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

---

### `InboxEffect`

Pakiet: `com.edoreczenia.feature.inbox.presentation`

```kotlin
sealed class InboxEffect {
    data class NavigateToMessageDetail(val messageId: String) : InboxEffect()
    data class ShowToast(val message: String) : InboxEffect()
}
```

---

## Dane mockowe

### `InboxMockData` (lub `companion object` w `FakeInboxRepository`)

Pakiet: `com.edoreczenia.feature.inbox.data.repository`

Pełne rekordy zgodne z clarify (spec sekcja Clarifications 2026-04-24):

```kotlin
val MOCK_MESSAGES = listOf(
    InboxMessage(
        id = "msg-001",
        caseNumber = "KM 1/23",
        senderName = "Sąd Rejonowy w Krakowie",
        subject = "wezwanie o zaliczkę",
        preview = "W nawiązaniu do wniosku o wszczęcie egzekucji uprzejmie informujemy...",
        displayDate = "10:45",
        isRead = false,
        isStarred = false
    ),
    InboxMessage(
        id = "msg-002",
        caseNumber = "GKM 45/22",
        senderName = "Ministerstwo Finansów",
        subject = "wezwanie do usunięcia braków formalnych",
        preview = "Prosimy o uzupełnienie podpisu elektronicznego pod załączonym...",
        displayDate = "Wczoraj",
        isRead = true,
        isStarred = true
    ),
    InboxMessage(
        id = "msg-003",
        caseNumber = "KM 124/23",
        senderName = "Jan Kowalski - Pełnomocnik",
        subject = "zawiadomienie",
        preview = "Przesyłam zawiadomienie o zmianie miejsca zamieszkania dłużnika...",
        displayDate = "15 Lis",
        isRead = false,
        isStarred = false
    ),
    InboxMessage(
        id = "msg-004",
        caseNumber = "KMS 7/23",
        senderName = "Urząd Skarbowy Warszawa",
        subject = "odpowiedź na pismo",
        preview = "Potwierdzamy otrzymanie zajęcia rachunku bankowego numer...",
        displayDate = "14 Lis",
        isRead = true,
        isStarred = false
    )
)
```

---

## Zasoby strings.xml (nowe wpisy dla feature'a inbox)

Pakiet: `app/src/main/res/values/strings.xml`

```xml
<!-- Inbox — etykiety ogólne -->
<string name="inbox_title">Odebrane</string>
<string name="inbox_new_badge">%d nowe</string>
<string name="inbox_search_placeholder">Szukaj w skrzynce</string>

<!-- Inbox — filtry -->
<string name="inbox_filter_all">Wszystkie</string>
<string name="inbox_filter_unread">Nieprzeczytane</string>
<string name="inbox_filter_starred">Ważne</string>

<!-- Inbox — stany puste -->
<string name="inbox_empty_no_messages_title">Koniec wiadomości</string>
<string name="inbox_empty_no_messages_subtitle">Wszystkie dokumenty zostały wyświetlone</string>
<string name="inbox_empty_no_results_title">Brak wyników</string>
<string name="inbox_empty_no_results_subtitle">Nie znaleziono wiadomości spełniających kryteria</string>

<!-- Inbox — stan błędu -->
<string name="inbox_error_title">Nie udało się pobrać wiadomości</string>
<string name="inbox_error_subtitle_network">Sprawdź połączenie z internetem</string>
<string name="inbox_error_subtitle_technical">Wystąpił nieoczekiwany błąd</string>
<string name="inbox_error_retry_button">Spróbuj ponownie</string>

<!-- Inbox — nawigacja dolna -->
<string name="inbox_nav_mail">Poczta</string>
<string name="inbox_nav_outbox">Do wysyłki</string>
<string name="inbox_nav_ade">ADE</string>
<string name="inbox_nav_settings">Ustawienia</string>

<!-- Inbox — FAB i inne akcje -->
<string name="inbox_fab_create_message">Utwórz wiadomość</string>
<string name="inbox_detail_coming_soon">Szczegóły wiadomości — wkrótce</string>
<string name="inbox_create_coming_soon">Tworzenie wiadomości — wkrótce</string>
```

---

## Stany przejść (State Transitions)

```
FakeInboxRepository.refresh(shouldSimulateError=false)
    ↓
ViewModel.init
    ↓ Initial
    ↓ Loading
    ↓ Success(4 rekordy, unreadCount=2, filter=ALL, query="", isRefreshing=false)

FilterChanged(UNREAD)
    ↓ Success(2 rekordy: msg-001, msg-003, ...)

FilterChanged(STARRED)
    ↓ Success(1 rekord: msg-002, ...)

FilterChanged(ALL) + SearchQueryChanged("brak")
    ↓ Empty(reason=NO_RESULTS, filter=ALL, query="brak")

Refresh (podczas Success)
    ↓ Success(isRefreshing=true)
    ↓ [delay 500-1000ms]
    ↓ Success(isRefreshing=false)

RetryLoad (podczas Error)
    ↓ Loading
    ↓ Success(...) lub Error(...)

FakeInboxRepository(shouldSimulateError=true)
    ↓ Loading
    ↓ Error("Sprawdź połączenie z internetem")

ToggleStar("msg-001")
    ↓ Success(messages[msg-001.isStarred=true], ...)
```

