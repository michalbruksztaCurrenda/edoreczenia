# Feature Specification: Do wysyłki (Outbox)

**Feature Branch**: `003-do-wysylki`  
**Created**: 2026-04-25  
**Status**: Draft  
**Feature Directory**: `specs/003-do-wysylki`

---

## Clarifications

### Session 2026-04-25

- Q: Jak ma działać przycisk „Synchronizuj" w mockowej implementacji? → A: Klik → krótki loading (1–2 sek.) → ponowne załadowanie tych samych mock-danych → czas ostatniej próby aktualizowany do bieżącego czasu.
- Q: Jak mają zachowywać się przyciski „Zaznacz wszystko" i „Wyślij" w mockowej wersji? → A: Widoczne i aktywne, kliknięcie → brak reakcji (brak feedbacku, brak Snackbara w mock).
- Q: Czy pull-to-refresh ma być aktywny już teraz? → A: Tak, pull-to-refresh jest aktywny i wywołuje ten sam efekt co przycisk „Odśwież listę" w quick actions (loading → reload tych samych mock-danych).
- Q: Jakie konkretne teksty mają mieć stan pusty i stan błędu? → A: Pusty: „Brak pozycji do wysyłki" / „Wszystkie przesyłki zostały wysłane". Błąd: „Nie udało się pobrać kolejki" / „Spróbuj ponownie". Klucze: `outbox_empty_title`, `outbox_empty_subtitle`, `outbox_error_title`, `outbox_error_retry_button` — analogia do konwencji inbox (002).
- Q: Czy UI multi-select (checkboxy) ma być obecne wizualnie w tej wersji? → A: Nie — brak checkboxów i jakiegokolwiek UI zaznaczenia. Brak stanu zaznaczenia w `OutboxUiState`. „Zaznacz wszystko" w quick actions jest widoczne, ale niereaktywne.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Przeglądanie listy pozycji do wysyłki (Priority: P1)

Użytkownik wchodzi do sekcji „Do wysyłki" i widzi listę przesyłek oczekujących na wysłanie. Każda pozycja na liście pokazuje adresata, temat/tytuł, numer sprawy, status przesyłki oraz ewentualny komunikat błędu. Dzięki temu użytkownik szybko ocenia, które pozycje wymagają jego uwagi.

**Why this priority**: Podstawowy cel ekranu — użytkownik musi zobaczyć, co czeka na wysyłkę. Bez tej funkcji ekran nie ma wartości.

**Independent Test**: Uruchomić aplikację, przejść do sekcji „Do wysyłki". Na ekranie pojawia się lista z co najmniej 4 przykładowymi pozycjami zawierającymi adresata, temat, numer sprawy i badge statusu.

**Acceptance Scenarios**:

1. **Given** użytkownik jest zalogowany i przechodzi do sekcji „Do wysyłki", **When** ekran się wczytuje, **Then** widzi listę pozycji; każda pozycja prezentuje: numer sprawy, adresata, temat, badge statusu i ewentualny komunikat błędu.
2. **Given** lista wczytuje się po raz pierwszy, **When** trwa pobieranie danych, **Then** wyświetlany jest stan ładowania (loading indicator).
3. **Given** lista jest pusta, **When** dane zostaną pobrane, **Then** wyświetlany jest stan pusty z czytelną informacją dla użytkownika.
4. **Given** wystąpił błąd pobierania danych, **When** dane nie mogą zostać załadowane, **Then** wyświetlany jest stan błędu z opcją ponowienia próby.

---

### User Story 2 — Rozróżnianie statusów przesyłek (Priority: P2)

Użytkownik widzi wizualne oznaczenie statusu każdej przesyłki: błąd wysyłki, do zatwierdzenia, oczekuje na wysyłkę. Dzięki temu może natychmiast zorientować się, które pozycje wymagają działania, a które oczekują na przetworzenie.

**Why this priority**: Status przesyłki decyduje o wymaganej akcji — kolor lewej krawędzi elementu i badge statusu są kluczowymi wskazówkami wizualnymi dla użytkownika.

**Independent Test**: Na liście widoczne są elementy z co najmniej 3 różnymi statusami (Błąd wysyłki, Do zatwierdzenia, Oczekuje na wysyłkę). Każdy status ma odmienny badge i kolor krawędzi.

**Acceptance Scenarios**:

1. **Given** lista zawiera pozycję ze statusem „Błąd wysyłki", **When** element jest wyświetlany, **Then** posiada czerwony badge statusu, czerwoną lewą krawędź i widoczny komunikat błędu (np. „Brak podpisu kwalifikowanego").
2. **Given** lista zawiera pozycję ze statusem „Do zatwierdzenia", **When** element jest wyświetlany, **Then** posiada badge „Do zatwierdzenia" i pomarańczową lewą krawędź.
3. **Given** lista zawiera pozycję ze statusem „Oczekuje na wysyłkę", **When** element jest wyświetlany, **Then** posiada badge „Oczekuje na wysyłkę" i szarą lewą krawędź.

---

### User Story 3 — Status baner z informacją o niewysłanych elementach (Priority: P2)

Użytkownik na górze ekranu widzi baner z podsumowaniem: liczba niewysłanych elementów, czas ostatniej próby synchronizacji i przycisk „Synchronizuj". Daje to szybki wgląd w ogólny stan kolejki.

**Why this priority**: Baner jest ważnym elementem UX widocznym w makiecie — komunikuje ogólny stan bez konieczności przeglądania listy.

**Independent Test**: Na ekranie widoczny baner z ikoną `sync_problem`, liczbą niewysłanych elementów, czasem ostatniej próby i przyciskiem synchronizacji.

**Acceptance Scenarios**:

1. **Given** w kolejce są niewysłane elementy, **When** ekran się wczytuje, **Then** baner pokazuje liczbę niewysłanych elementów i czas ostatniej próby.
2. **Given** użytkownik klika przycisk „Synchronizuj" w banerze, **When** akcja zostaje wyzwolona, **Then** lista odświeża się i baner aktualizuje stan.

---

### User Story 4 — Szybkie akcje (Quick Actions) (Priority: P3)

Użytkownik widzi poziomy pasek z szybkimi akcjami: „Zaznacz wszystko", „Odśwież listę", „Wyślij". Akcje widoczne w makiecie są obecne w UI, choć pełna logika zaznaczania i masowej wysyłki jest poza zakresem tej specyfikacji.

**Why this priority**: Elementy są widoczne w makiecie i budują spójność wizualną. Pełna funkcjonalność zaznaczania i wysyłki to odrębna implementacja.

**Independent Test**: Pasek szybkich akcji jest widoczny pod banerem statusu. Przyciski są wyświetlane poprawnie. Kliknięcie „Odśwież listę" inicjuje odświeżenie.

**Acceptance Scenarios**:

1. **Given** ekran jest załadowany, **When** użytkownik widzi pasek akcji, **Then** wyświetlone są przyciski: „Zaznacz wszystko", „Odśwież listę", „Wyślij".
2. **Given** użytkownik kliknie „Odśwież listę", **When** akcja zostaje wyzwolona, **Then** lista jest odświeżana.

---

### User Story 5 — Przejście do szczegółów pozycji (Priority: P3)

Użytkownik klika wybraną pozycję na liście i przechodzi do ekranu szczegółów. Na etapie tej specyfikacji ekran szczegółów może być placeholderem — istotne jest, żeby nawigacja działała i użytkownik mógł wrócić do listy.

**Why this priority**: Nawigacja do szczegółów jest standardowym wzorcem list-to-detail, spójnym z feature'em inbox. Pełna implementacja szczegółów to odrębny feature.

**Independent Test**: Kliknięcie elementu listy przenosi na ekran szczegółów. Ekran szczegółów posiada przycisk cofnięcia, który wraca do listy „Do wysyłki".

**Acceptance Scenarios**:

1. **Given** użytkownik klika element listy, **When** nawigacja zostaje wywołana, **Then** otwiera się ekran szczegółów z identyfikatorem przesyłki.
2. **Given** użytkownik jest na ekranie szczegółów, **When** klika przycisk cofnięcia lub używa systemowego Back, **Then** wraca do listy „Do wysyłki".

---

### Edge Cases

- Co się dzieje, gdy lista jest pusta po załadowaniu? → Wyświetlany jest dedykowany stan pusty: tytuł „Brak pozycji do wysyłki", podtytuł „Wszystkie przesyłki zostały wysłane".
- Co się dzieje przy braku połączenia lub błędzie sieciowym? → Wyświetlany jest stan błędu: tytuł „Nie udało się pobrać kolejki", przycisk „Spróbuj ponownie".
- Co jeśli pozycja ma nieznany/niezdefiniowany status? → Wyświetlana jest jako „Oczekuje na wysyłkę" (domyślny fallback).
- Co jeśli komunikat błędu dla pozycji jest pusty? → Pole błędu nie jest wyświetlane (warunkowa widoczność).
- Co jeśli lista ma bardzo wiele pozycji? → Lista jest przewijalna; brak paginacji w pierwszej implementacji (mockowe dane).

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Ekran MUSI wyświetlać listę pozycji oczekujących na wysyłkę.
- **FR-002**: Każda pozycja na liście MUSI prezentować: numer sprawy, adresata (imię i nazwisko lub nazwa podmiotu), temat/tytuł przesyłki, badge statusu.
- **FR-003**: Każda pozycja MUSI wizualnie komunikować status przez: badge statusu (kolorowy chip z etykietą) oraz kolorową lewą krawędź elementu, zgodnie z makietą.
- **FR-004**: Pozycja ze statusem „Błąd wysyłki" MUSI wyświetlać komunikat błędu pod danymi adresata.
- **FR-005**: Ekran MUSI obsługiwać pięć stanów UI: Initial, Loading, Success (lista z danymi), Empty (pusta lista), Error (błąd pobierania).
- **FR-006**: Ekran MUSI wyświetlać baner statusu z liczbą niewysłanych elementów, czasem ostatniej próby i przyciskiem „Synchronizuj".
- **FR-007**: Ekran MUSI zawierać poziomy pasek szybkich akcji: „Zaznacz wszystko", „Odśwież listę", „Wyślij".
- **FR-008**: Kliknięcie „Odśwież listę" MUSI odświeżać listę pozycji (krótki loading → ponowne załadowanie mock-danych).
- **FR-008a**: Ekran MUSI obsługiwać pull-to-refresh — efekt identyczny jak „Odśwież listę" (loading → reload tych samych mock-danych).
- **FR-008b**: Przycisk „Synchronizuj" w banerze MUSI wywołać krótki loading (1–2 sek.) → reload tych samych mock-danych → aktualizację czasu ostatniej próby do bieżącego czasu.
- **FR-008c**: Przyciski „Zaznacz wszystko" i „Wyślij" w pasku quick actions są widoczne i aktywne, ale kliknięcie nie wywołuje żadnej reakcji (brak Snackbara, brak logiki w mock).
- **FR-008d**: Brak checkboxów i jakiegokolwiek UI multi-select w tej wersji. Brak stanu zaznaczenia w `OutboxUiState`.
- **FR-009**: Kliknięcie pozycji na liście MUSI prowadzić do ekranu szczegółów (może być placeholder).
- **FR-010**: Ekran szczegółów MUSI umożliwiać powrót do listy „Do wysyłki" przez przycisk cofnięcia lub systemowy Back.
- **FR-011**: Ekran MUSI posiadać TopAppBar z tytułem „Do wysyłki", ikoną menu i ikoną wyszukiwania, zgodnie z makietą.
- **FR-012**: Ekran MUSI posiadać BottomNavigationBar z 4 pozycjami: Poczta, Do wysyłki (aktywna), ADE, Ustawienia.
- **FR-013**: Dane listy MUSZĄ pochodzić z warstwy data (repozytorium); w pierwszej implementacji źródłem jest `FakeOutboxRepository` z przykładowymi rekordami.
- **FR-014**: Logika biznesowa NIE MOŻE być umieszczona bezpośrednio w composable.
- **FR-015**: Teksty i etykiety UI MUSZĄ być zarządzane przez zasoby strings (`strings.xml`), bez hardcodowania w kodzie. Nowe klucze dla 003:
  - `outbox_empty_title` = „Brak pozycji do wysyłki"
  - `outbox_empty_subtitle` = „Wszystkie przesyłki zostały wysłane"
  - `outbox_error_title` = „Nie udało się pobrać kolejki"
  - `outbox_error_retry_button` = „Spróbuj ponownie"

### Obsługiwane statusy przesyłki (z makiety)

| Status | Etykieta PL | Kolor krawędzi | Kolor badge |
|--------|-------------|----------------|-------------|
| `SEND_ERROR` | Błąd wysyłki | `error` (czerwony) | `error-container` |
| `PENDING_APPROVAL` | Do zatwierdzenia | `tertiary-fixed-dim` (pomarańczowy) | `surface-container` |
| `WAITING` | Oczekuje na wysyłkę | `surface-dim` (szary) | `surface-container-high` |

### Key Entities

- **OutboxItem**: Reprezentuje pojedynczą pozycję w kolejce do wysyłki.
  - `id: String` — unikalny identyfikator
  - `caseNumber: String` — numer sprawy (np. „KM 124/23")
  - `recipientName: String` — nazwa adresata / odbiorcy
  - `subject: String` — temat / tytuł przesyłki
  - `status: OutboxStatus` — enum: `SEND_ERROR`, `PENDING_APPROVAL`, `WAITING`
  - `errorMessage: String?` — opcjonalny komunikat błędu (tylko dla `SEND_ERROR`)

- **OutboxStatus**: Enum reprezentujący stan przesyłki.

---

## Mockowe dane developerskie

Pierwsza implementacja MA używać `FakeOutboxRepository` z poniższymi przykładowymi rekordami (odwzorowanie makiety):

| id | caseNumber | recipientName | subject | status | errorMessage |
|----|------------|---------------|---------|--------|--------------|
| `out-001` | KM 124/23 | Janusz Kowalski | Wezwanie do zapłaty - Zaległe alimenty | `SEND_ERROR` | Brak podpisu kwalifikowanego |
| `out-002` | GKM 45/24 | Anna Nowak | Postanowienie o zajęciu wynagrodzenia | `PENDING_APPROVAL` | null |
| `out-003` | KM 902/22 | PKO Bank Polski S.A. | Zapytanie o stan konta dłużnika | `WAITING` | null |
| `out-004` | KM 11/24 | Marek Wójcik | Postanowienie o umorzeniu | `WAITING` | null |

Ekran po implementacji MA wyświetlać te przykładowe dane bez połączenia z backendem.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Ekran „Do wysyłki" wyświetla listę przykładowych pozycji natychmiast po uruchomieniu (w trybie mockowym, bez backendu) — widoczne co najmniej 4 rekordy.
- **SC-002**: Użytkownik może odróżnić wizualnie pozycje o różnych statusach (Błąd wysyłki, Do zatwierdzenia, Oczekuje) bez dodatkowych wyjaśnień.
- **SC-003**: Użytkownik przechodzi do ekranu szczegółów po kliknięciu pozycji i wraca do listy jednym działaniem (przycisk cofnięcia lub systemowy Back).
- **SC-004**: Ekran obsługuje wszystkie 5 stanów UI — użytkownik nigdy nie widzi pustego, nieokreślonego ekranu bez informacji.
- **SC-005**: Wizualny układ ekranu jest zgodny z makietą (`docs/screens/do_wysy_ki/screen.png`) pod względem hierarchii treści, sekcji, ikon i kolorystyki.
- **SC-006**: Kod ekranu jest zgodny z architekturą warstwową projektu — brak logiki biznesowej w composable, dane w warstwie data.

---

## Out of Scope

- Pełna implementacja masowego zaznaczania i wysyłki pozycji.
- Pełna implementacja ekranu szczegółów przesyłki (może być placeholder).
- Integracja z backendem / prawdziwym API.
- Edycja pozycji z listy.
- Usuwanie pozycji z listy.
- Powiadomienia push o zmianie statusu.
- Obsługa załączników na ekranie listy.
- Paginacja / infinite scroll.
- Tryb offline z lokalnym cache'em (Room).

---

## Assumptions

- Dane listy w pierwszej implementacji pochodzą z `FakeOutboxRepository` — tryb mockowy jest oczekiwany i akceptowalny na etapie feature 003.
- Nawigacja do sekcji „Do wysyłki" jest realizowana przez BottomNavigationBar, spójnie z ekranem „Odebrane" (feature 002).
- Ekran szczegółów pozycji będzie osobnym feature'em w przyszłości; w ramach 003 dopuszczalny jest placeholder analogiczny do `InboxDetailPlaceholderScreen`.
- Kolorystyka i ikony są zgodne z paletą zdefiniowaną w `code.html` (Material 3, paleta e-Komornik).
- Nie jest używany Hilt ani Room (brak uzasadnienia na etapie mockowym).
- BottomNavBar „Do wysyłki" używa ikony `outbox` (Material Symbols), zgodnie z makietą.
- Baner statusu wyświetla liczbę i czas ostatniej próby na podstawie stanu z warstwy danych — w mockowej implementacji wartości są statyczne; kliknięcie „Synchronizuj" symuluje loading (1–2 sek.) i aktualizuje czas do bieżącego.
- Pull-to-refresh jest aktywny i wywołuje identyczny efekt jak przycisk „Odśwież listę": loading → reload tych samych mock-danych.
- Przyciski „Zaznacz wszystko" i „Wyślij" są widoczne w quick actions, ale niereaktywne w mockowej implementacji — brak multi-select UI i brak stanu zaznaczenia w `OutboxUiState`.
- Zarz…dzanie sesjami i uwierzytelnienie jest poza zakresem tego feature'a.

