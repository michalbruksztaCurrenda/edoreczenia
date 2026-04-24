# Specyfikacja Feature: Ekran Odebrane (Inbox)

**Feature Branch**: `002-inbox-odebrane`  
**Created**: 2026-04-24  
**Status**: Draft  
**Powiązane feature**: `001-user-auth` (nawigacja po zalogowaniu)

---

## Clarifications

### Session 2026-04-24

- Q: Jakie dokładnie pola ma mieć pojedyncza przesyłka na liście Odebrane w pierwszej implementacji mockowej? → A: Każda pozycja listy musi zawierać: (1) numer/sygnaturę sprawy — label-caps, kolor zależny od statusu (secondary/primary dla nieprzeczytanych, outline dla przeczytanych); (2) nazwę nadawcy — h3, kolor primary dla nieprzeczytanych, on-surface-variant dla przeczytanych; (3) temat/tytuł — body-md, pogrubiony (font-semibold) dla nieprzeczytanych, normalny dla przeczytanych; (4) fragment treści (preview) — body-sm, line-clamp-1; (5) datę/czas — body-sm top-right; (6) ikonę koperty — `mail` (FILL=1) dla nieprzeczytanych, `drafts` (FILL=0) dla przeczytanych; (7) ikonę gwiazdki — FILL=1, kolor secondary-container dla ważnych; FILL=0, kolor outline dla pozostałych; (8) wskaźnik statusu — pomarańczowa kropka (8×8 dp, #fd8b00) dla nieprzeczytanych, pusta przestrzeń 8×8 dp dla przeczytanych; (9) lewy pionowy pasek (width=4dp, kolor secondary-container) — widoczny dla nieprzeczytanych, nieobecny dla przeczytanych. Pole `preview` musi być dodane do FR-003 obok pozostałych pól.
- Q: Czy kliknięcie w pozycję listy ma już coś robić, czy na tym etapie ma być tylko przygotowane pod przyszły feature szczegółów? → A: Na tym etapie wystarczy przygotowany punkt nawigacyjny (NavController z zarejestrowaną trasą `inbox_detail/{messageId}`). Kliknięcie wywołuje tylko log debugowy lub Toast "Szczegóły wiadomości — wkrótce". Implementacja ekranu szczegółów jest out-of-scope.
- Q: Jak ma wyglądać pusty stan ekranu? → A: Dwa warianty: (A) koniec pełnej listy — ikona `archive` w tle surface-container, nagłówek "Koniec wiadomości", podpis "Wszystkie dokumenty zostały wyświetlone", dashed-border kontener bg-white/50 — wyświetlany pod listą; (B) brak wyników filtrowania/wyszukiwania — ikona `search_off`, nagłówek "Brak wyników", podpis "Nie znaleziono wiadomości spełniających kryteria" — zamiast listy. Oba warianty wyśrodkowane, mt-8 od góry obszaru listy.
- Q: Jak ma wyglądać stan błędu? → A: Stan błędu (domyślne założenie, brak w HTML makiety): ikona `cloud_off` (48dp, kolor error #ba1a1a) w okrągłym tle error-container, nagłówek "Nie udało się pobrać wiadomości" (h3), podpis kontekstowy (np. "Sprawdź połączenie z internetem"), przycisk "Spróbuj ponownie" (filled, secondary-container). Układ wyśrodkowany pionowo i poziomo w obszarze listy. Tapnięcie przycisku wywołuje re-trigger Flow w ViewModel.
- Q: Czy ekran ma wspierać pull-to-refresh już w pierwszej wersji, czy wystarczy przycisk / akcja odświeżenia? → A: Pull-to-refresh MUSI być zaimplementowany w pierwszej wersji (FR-009, User Story 8). Używamy `PullRefreshIndicator` z Compose Material. Gest wywołuje ponowne załadowanie danych z FakeRepository z symulowanym opóźnieniem 500–1000 ms. Przycisk odświeżenia jako alternatywa nie jest wymagany.
- Q: Jakie przykładowe rekordy mockowych przesyłek mają być pokazane, aby dobrze odwzorować makietę? → A: Cztery rekordy zgodne z HTML makiety (w kolejności): (1) id=msg-001, nr=KM 1/23, nadawca=Sąd Rejonowy w Krakowie, temat=wezwanie o zaliczkę, preview=W nawiązaniu do wniosku o wszczęcie egzekucji uprzejmie informujemy..., data=10:45, isRead=false, isStarred=false; (2) id=msg-002, nr=GKM 45/22, nadawca=Ministerstwo Finansów, temat=wezwanie do usunięcia braków formalnych, preview=Prosimy o uzupełnienie podpisu elektronicznego pod załączonym..., data=Wczoraj, isRead=true, isStarred=true; (3) id=msg-003, nr=KM 124/23, nadawca=Jan Kowalski - Pełnomocnik, temat=zawiadomienie, preview=Przesyłam zawiadomienie o zmianie miejsca zamieszkania dłużnika..., data=15 Lis, isRead=false, isStarred=false; (4) id=msg-004, nr=KMS 7/23, nadawca=Urząd Skarbowy Warszawa, temat=odpowiedź na pismo, preview=Potwierdzamy otrzymanie zajęcia rachunku bankowego numer..., data=14 Lis, isRead=true, isStarred=false.
- Q: Jakie elementy nawigacji z makiety mają być już aktywne, a które tylko wizualnie przygotowane? → A: Aktywna: zakładka Poczta (mail) — podświetlona bg-blue-50/text #003366, bez akcji nawigacyjnej (bieżący ekran). Nieaktywne (wizualne only, no-op): Do wysyłki (outbox), ADE (manage_search), Ustawienia (settings) — kolor slate-500/outline. FAB (edit, secondary-container) — wizualnie obecny, kliknięcie no-op lub Toast. Top App Bar: menu (hamburger), ikona powiadomień, avatar — wszystkie no-op.
- Q: Czy lista ma być paginowana już teraz, czy pierwsza wersja ma mieć statyczną listę mocków? → A: Pierwsza wersja ma statyczną listę 4 rekordów bez paginacji. `LazyColumn` jest stosowany ze względów architektonicznych (wydajność i rozszerzalność), ale bez infinite scroll ani load-more. Paginacja jest explicitnie out-of-scope dla tego feature'a.
- Q: Jakie statusy / typy przesyłek mają być pokazane na liście w pierwszej wersji? → A: Dwa statusy odczytu: nieprzeczytana (isRead=false) i przeczytana (isRead=true). Dwa stany gwiazdki: ważna (isStarred=true) i nieważna (isStarred=false). Brak dodatkowych typów (archiwalna, oczekująca, usunięta) — out-of-scope. Wizualne różnicowanie: nieprzeczytane → lewy pasek + pomarańczowa kropka + pogrubiony tekst + ikona `mail`; przeczytane → muted kolory + ikona `drafts`, brak paska i kropki.
- Q: Co dokładnie jest out-of-scope dla tego feature'a, aby nie mieszać go z przyszłym feature'em szczegółów wiadomości? → A: Explicitnie out-of-scope: (1) implementacja ekranu szczegółów przesyłki; (2) zmiana isRead=true po kliknięciu pozycji — to należy do feature'u szczegółów; (3) backend API; (4) persystencja (Room, DataStore) — stan gwiazdki żyje tylko w pamięci ViewModel; (5) powiadomienia push; (6) ekrany zakładek Do wysyłki/ADE/Ustawienia; (7) ekran tworzenia nowej wiadomości; (8) zarządzanie sesją i biometria; (9) paginacja; (10) auto-wylogowanie przy błędzie 401.

---

## Cel i kontekst

Po poprawnym zalogowaniu (feature `001-user-auth`) użytkownik trafia na ekran **Odebrane**, który jest głównym widokiem aplikacji e-Komornik. Ekran prezentuje listę odebranych przesyłek / korespondencji elektronicznej i stanowi punkt wyjścia do nawigacji po całej aplikacji.

Ekran ma być wiernie odwzorowany zgodnie z makietą (`docs/screens/odebrane/screen.png` i `docs/screens/odebrane/code.html`).

Na etapie pierwszej implementacji ekran ma wyświetlać **przykładowe przesyłki** (dane mockowe), aby umożliwić wierną prezentację i demonstrację działania listy bez potrzeby podłączonego backendu.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Przeglądanie listy odebranych przesyłek (Priority: P1)

Użytkownik zalogowany w aplikacji widzi ekran Odebrane jako pierwszy ekran po zalogowaniu. Na liście wyświetlone są odebrane przesyłki z podstawowymi informacjami. W trybie development lista zawiera przykładowe dane mockowe, dzięki czemu ekran nie jest pusty.

**Why this priority**: Jest to główny i jedyny ekran tego feature'a. Bez listy przesyłek nie ma możliwości demonstracji ani weryfikacji pozostałych wymagań.

**Independent Test**: Można przetestować uruchamiając aplikację po zalogowaniu — ekran Odebrane musi się pojawić, a lista musi zawierać przykładowe przesyłki ze wszystkimi wymaganymi danymi.

**Acceptance Scenarios**:

1. **Given** zalogowany użytkownik, **When** aplikacja przechodzi do ekranu Odebrane, **Then** wyświetlona zostaje lista przesyłek zawierająca co najmniej 4 przykładowe pozycje z danymi: numer sprawy, nadawca, temat, data, status przeczytania.
2. **Given** lista przesyłek, **When** przesyłka jest nieprzeczytana, **Then** jest wizualnie wyróżniona (pogrubiona treść, kolorowy pasek po lewej stronie, pomarańczowa kropka statusu, wypełniona ikona koperty).
3. **Given** lista przesyłek, **When** przesyłka jest przeczytana, **Then** wyświetlana jest bez wyróżnienia (szary tekst, brak paska, brak kropki, ikona otwartej koperty).
4. **Given** lista przesyłek, **When** ekran jest gotowy, **Then** widoczna jest liczba nowych wiadomości (badge "X nowe") obok nagłówka "Odebrane".

---

### User Story 2 — Filtrowanie listy (Priority: P2)

Użytkownik może filtrować listę przesyłek według dostępnych kategorii: Wszystkie, Nieprzeczytane, Ważne. Domyślnie aktywny jest filtr "Wszystkie".

**Why this priority**: Filtrowanie jest widocznym elementem makiety i wpływa na użyteczność ekranu. Możliwe do implementacji niezależnie od backendu dzięki danym mockowym.

**Independent Test**: Kliknięcie każdego filtru musi zmienić wizualnie aktywną zakładkę i ograniczyć wyświetlaną listę do właściwych pozycji.

**Acceptance Scenarios**:

1. **Given** ekran Odebrane, **When** użytkownik klika filtr "Nieprzeczytane", **Then** lista ogranicza się do przesyłek z nieprzeczytanym statusem, a filtr "Nieprzeczytane" staje się wizualnie aktywny.
2. **Given** ekran Odebrane, **When** użytkownik klika filtr "Ważne", **Then** lista ogranicza się do oznaczonych gwiazdką przesyłek.
3. **Given** aktywny filtr "Nieprzeczytane" lub "Ważne" bez pasujących wiadomości, **When** lista jest pusta, **Then** wyświetlany jest stan pustej listy z odpowiednim komunikatem.
4. **Given** aktywny filtr inny niż "Wszystkie", **When** użytkownik klika "Wszystkie", **Then** przywracana jest pełna lista przesyłek.

---

### User Story 3 — Wyszukiwanie w skrzynce (Priority: P2)

Użytkownik może wyszukiwać przesyłki po nadawcy, temacie lub numerze sprawy, wpisując frazę w pole wyszukiwania widoczne na ekranie.

**Why this priority**: Pole wyszukiwania jest widocznym elementem makiety i stanowi kluczowy element UX dla skrzynek z wieloma wiadomościami.

**Independent Test**: Wpisanie fragmentu nazwy nadawcy lub tematu musi filtrować listę w czasie rzeczywistym.

**Acceptance Scenarios**:

1. **Given** lista przesyłek, **When** użytkownik wpisuje frazę w pole "Szukaj w skrzynce", **Then** lista filtruje się do pozycji pasujących do frazy (nadawca, temat, numer sprawy).
2. **Given** aktywne wyszukiwanie, **When** wpisana fraza nie pasuje do żadnej przesyłki, **Then** wyświetlany jest stan pustej listy z komunikatem informującym o braku wyników.
3. **Given** aktywne wyszukiwanie, **When** użytkownik usuwa wpisaną frazę, **Then** przywracana jest pełna lista przesyłek.

---

### User Story 4 — Oznaczanie gwiazdką (Priority: P3)

Użytkownik może oznaczyć przesyłkę gwiazdką (ważna) lub usunąć to oznaczenie bezpośrednio z poziomu listy.

**Why this priority**: Funkcja widoczna w makiecie, ale nie blokuje demonstracji podstawowego działania ekranu.

**Independent Test**: Tapnięcie ikony gwiazdki przy przesyłce zmienia jej stan (zaznaczona / odznaczona) widocznie w UI.

**Acceptance Scenarios**:

1. **Given** przesyłka bez oznaczenia gwiazdką, **When** użytkownik tapnie ikonę gwiazdki, **Then** gwiazdka zmienia stan na "zaznaczona" (wypełniona ikona, zmiana koloru).
2. **Given** przesyłka oznaczona gwiazdką, **When** użytkownik tapnie ikonę gwiazdki, **Then** oznaczenie zostaje usunięte.

---

### User Story 5 — Stan ładowania listy (Priority: P1)

Podczas pobierania listy przesyłek (lub inicjalizacji danych mockowych) użytkownik widzi wskaźnik ładowania, a nie pusty ekran.

**Why this priority**: Stan loading jest wymaganiem architektonicznym (zasada VII konstytucji: 5 stanów UI) i wpływa na postrzeganą jakość aplikacji.

**Independent Test**: Można przetestować przez symulację opóźnienia w dostarczaniu danych — wskaźnik loadingu musi być widoczny przed pojawieniem się listy.

**Acceptance Scenarios**:

1. **Given** ekran Odebrane jest otwierany, **When** dane są pobierane / ładowane, **Then** wyświetlany jest wskaźnik ładowania (spinner lub skeleton).
2. **Given** dane zostały załadowane, **When** lista jest gotowa, **Then** wskaźnik ładowania znika i pojawia się lista przesyłek.

---

### User Story 6 — Stan błędu pobierania (Priority: P1)

Gdy pobranie listy przesyłek zakończy się błędem (np. brak sieci), użytkownik widzi czytelny komunikat z możliwością ponowienia próby.

**Why this priority**: Wymaganie architektoniczne (zasada VII) — każdy ekran musi obsługiwać stan błędu.

**Independent Test**: Można przetestować przez zasymulowanie błędu repozytorium — ekran musi pokazać komunikat błędu i przycisk ponowienia.

**Acceptance Scenarios**:

1. **Given** ekran Odebrane, **When** pobieranie listy kończy się błędem (np. brak sieci, błąd serwera), **Then** wyświetlany jest komunikat błędu zrozumiały dla użytkownika oraz przycisk "Spróbuj ponownie".
2. **Given** ekran błędu, **When** użytkownik tapnie "Spróbuj ponownie", **Then** aplikacja ponawia próbę pobrania listy i przechodzi do stanu loading.
3. **Given** ekran błędu, **When** użytkownik wraca na ekran po naprawieniu połączenia, **Then** lista przesyłek ładuje się poprawnie.

---

### User Story 7 — Stan pustej listy (Priority: P1)

Gdy skrzynka odebrana jest pusta (brak przesyłek spełniających kryteria filtru lub wyszukiwania), użytkownik widzi przyjazny stan pustej listy.

**Why this priority**: Wymaganie architektoniczne (zasada VII) — wymagany stan Empty.

**Independent Test**: Można przetestować przez zastosowanie filtru, który nie zwraca żadnych wyników.

**Acceptance Scenarios**:

1. **Given** skrzynka bez wiadomości lub filtr bez wyników, **When** lista jest pusta, **Then** wyświetlany jest czytelny komunikat (np. "Brak wiadomości" lub "Brak wyników wyszukiwania") z odpowiednią ikoną.
2. **Given** stan pustej listy, **When** użytkownik może odświeżyć listę (pull-to-refresh lub przycisk), **Then** ponawiana jest próba pobrania danych.

---

### User Story 8 — Odświeżenie listy (Priority: P2)

Użytkownik może odświeżyć listę przesyłek, wykonując gest "pull-to-refresh" lub korzystając z dostępnej opcji odświeżania.

**Why this priority**: Standard UX dla list w aplikacjach mobilnych.

**Independent Test**: Gest pull-to-refresh na liście musi wyzwolić ponowne załadowanie danych i pokazać wskaźnik ładowania.

**Acceptance Scenarios**:

1. **Given** widoczna lista przesyłek, **When** użytkownik przeciąga listę w dół (pull-to-refresh), **Then** wyświetlany jest wskaźnik odświeżania i lista jest ponownie ładowana.
2. **Given** odświeżanie w toku, **When** nowe dane zostaną załadowane, **Then** lista aktualizuje się i wskaźnik odświeżania znika.

---

### User Story 9 — Nawigacja do szczegółów przesyłki (Priority: P3 — zakres przyszły)

Użytkownik może tapnąć przesyłkę, aby przejść do jej szczegółów.

**Why this priority**: Szczegóły przesyłki są poza zakresem tego feature'a. W bieżącej iteracji wystarczy przygotowanie punktu nawigacyjnego bez implementacji ekranu docelowego.

**Independent Test**: Tapnięcie przesyłki musi wyzwolić zdarzenie nawigacyjne (np. placeholder screen lub log).

**Acceptance Scenarios**:

1. **Given** lista przesyłek, **When** użytkownik tapnie wybraną przesyłkę, **Then** aplikacja inicjuje nawigację do ekranu szczegółów (implementacja ekranu szczegółów jest poza zakresem tego feature'a — wystarczy placeholder lub brak akcji z zaznaczeniem w planie).

---

### Edge Cases

- Co się dzieje, gdy liczba przesyłek przekracza widok ekranu? → Lista musi być przewijalna.
- Co jeśli tytuł lub nazwa nadawcy jest bardzo długa? → Tekst musi być obcięty (`line-clamp`) bez łamania layoutu.
- Co się dzieje przy równoczesnym zastosowaniu filtra i wyszukiwania? → Oba warunki muszą działać łącznie (AND).
- Co się dzieje po obróceniu urządzenia? → Layout musi zachowywać się poprawnie (Compose domyślnie obsługuje obrót).
- Co się dzieje, gdy sesja wygaśnie podczas przeglądania skrzynki? → Aplikacja musi przekierować do ekranu logowania (obsługa błędu autoryzacyjnego 401).
- Co jeśli mockowe dane są puste (lista mockowa jest pusta)? → Ekran musi wejść w stan Empty, a nie się zawiesić.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Ekran Odebrane MUSI być pierwszym ekranem wyświetlanym po poprawnym zalogowaniu użytkownika.
- **FR-002**: Ekran MUSI wyświetlać listę przesyłek pobieraną z warstwy danych. Na etapie pierwszej implementacji warstwa danych MUSI dostarczać przykładowe dane mockowe.
- **FR-003**: Każda pozycja listy MUSI prezentować: numer/sygnaturę sprawy, nazwę nadawcy/jednostki, temat/tytuł przesyłki, fragment treści (preview, line-clamp-1), datę/czas, ikonę koperty zależną od statusu (`mail` dla nieprzeczytanych, `drafts` dla przeczytanych), ikonę gwiazdki (FILL=1 dla ważnych), wskaźnik statusu (pomarańczowa kropka dla nieprzeczytanych) oraz lewy pionowy pasek koloru secondary-container (widoczny dla nieprzeczytanych).
- **FR-004**: Ekran MUSI obsługiwać pięć stanów UI: Initial, Loading, Success (lista z danymi), Empty (pusta lista), Error (błąd pobierania) zgodnie z zasadą VII konstytucji.
- **FR-005**: Ekran MUSI zawierać pasek wyszukiwania umożliwiający filtrowanie listy po nadawcy, temacie i numerze sprawy w czasie rzeczywistym.
- **FR-006**: Ekran MUSI zawierać zakładki/chipy filtrowania: "Wszystkie", "Nieprzeczytane", "Ważne". Domyślnie aktywny jest filtr "Wszystkie".
- **FR-007**: Nagłówek ekranu MUSI wyświetlać liczbę nowych (nieprzeczytanych) wiadomości w formie badge'a (np. "4 nowe").
- **FR-008**: Użytkownik MUSI mieć możliwość oznaczenia przesyłki jako ważna / usunięcia oznaczenia gwiazdką bezpośrednio z poziomu listy.
- **FR-009**: Ekran MUSI obsługiwać gest pull-to-refresh umożliwiający odświeżenie listy.
- **FR-010**: Stan błędu MUSI prezentować czytelny komunikat dla użytkownika oraz opcję ponowienia pobierania.
- **FR-011**: Stan pustej listy MUSI prezentować czytelny komunikat i ikonę (zgodnie z makietą: ikona archiwum, "Koniec wiadomości" lub "Brak wiadomości").
- **FR-012**: Ekran MUSI zawierać dolną nawigację (Bottom Navigation Bar) z czterema pozycjami: Poczta (aktywna), Do wysyłki, ADE, Ustawienia — zgodnie z makietą.
- **FR-013**: Ekran MUSI zawierać górny pasek aplikacji (Top App Bar) z menu, tytułem aplikacji "e-Komornik", ikoną powiadomień oraz avatarem użytkownika — zgodnie z makietą.
- **FR-014**: Ekran MUSI zawierać pływający przycisk akcji (FAB) z ikoną edycji — zgodnie z makietą.
- **FR-015**: Tapnięcie pozycji na liście MUSI inicjować nawigację do ekranu szczegółów przesyłki. Implementacja ekranu szczegółów jest poza zakresem tego feature'a — wystarczy przygotowany punkt nawigacyjny.
- **FR-016**: Logika biznesowa (filtrowanie, obsługa stanów) NIE MOŻE być umieszczona bezpośrednio w komponentach UI (Composable).
- **FR-017**: Dane listy MUSZĄ pochodzić z warstwy `data` przez kontrakt repozytorium zdefiniowany w warstwie `domain`.
- **FR-018**: Lista MUSI być zaimplementowana jako `LazyColumn` ze względów architektonicznych, jednak paginacja, infinite scroll oraz load-more są explicitnie out-of-scope dla tej wersji — wyświetlane są wyłącznie 4 statyczne rekordy mockowe.

### Mockowe dane developerskie (wymaganie jawne)

- **FR-MOCK-001**: W trybie development warstwa danych MUSI dostarczać implementację `FakeInboxRepository` (lub równoważną) z predefiniowaną listą przykładowych przesyłek.
- **FR-MOCK-002**: Przykładowe przesyłki mockowe MUSZĄ zawierać co najmniej 4 rekordy wiernie odwzorowujące makietę:

| Nr | id       | Numer sprawy | Nadawca                      | Temat                                       | Preview (skrót)                                             | Data    | isRead | isStarred |
|----|----------|-------------|------------------------------|---------------------------------------------|-------------------------------------------------------------|---------|--------|-----------|
| 1  | msg-001  | KM 1/23     | Sąd Rejonowy w Krakowie      | wezwanie o zaliczkę                         | W nawiązaniu do wniosku o wszczęcie egzekucji uprzejmie…    | 10:45   | false  | false     |
| 2  | msg-002  | GKM 45/22   | Ministerstwo Finansów        | wezwanie do usunięcia braków formalnych     | Prosimy o uzupełnienie podpisu elektronicznego pod załącz…  | Wczoraj | true   | true      |
| 3  | msg-003  | KM 124/23   | Jan Kowalski - Pełnomocnik   | zawiadomienie                               | Przesyłam zawiadomienie o zmianie miejsca zamieszkania dł… | 15 Lis  | false  | false     |
| 4  | msg-004  | KMS 7/23    | Urząd Skarbowy Warszawa      | odpowiedź na pismo                          | Potwierdzamy otrzymanie zajęcia rachunku bankowego numer…   | 14 Lis  | true   | false     |

- **FR-MOCK-003**: Każda przykładowa przesyłka MUSI zawierać fragment treści (preview), który jest wyświetlany jako skrócony podgląd na liście.
- **FR-MOCK-004**: Mockowe dane MUSZĄ odwzorowywać co najmniej dwa stany odczytu (przeczytana / nieprzeczytana) i co najmniej jedno oznaczenie gwiazdką, aby umożliwić demonstrację filtrów.

### Key Entities

- **InboxMessage** (przesyłka odebrana): identyfikator (`id: String`), numer/sygnatura sprawy (`caseNumber: String`), nadawca (`senderName: String`), temat/tytuł (`subject: String`), fragment treści (`preview: String`), data i czas (`displayDate: String`), status odczytu (`isRead: Boolean`), oznaczenie gwiazdką (`isStarred: Boolean`).
- **InboxFilter**: wartość filtra aktywnego na liście (Wszystkie / Nieprzeczytane / Ważne).
- **InboxUiState**: stan ekranu — Initial | Loading | Success(lista) | Empty | Error(komunikat).

### Non-Functional Requirements

- **NFR-001**: Ekran MUSI być zbudowany w **Jetpack Compose** zgodnie z technologicznym stosem projektu.
- **NFR-002**: Layout, kolorystyka, spacing, ikony i hierarchia treści MUSZĄ być zgodne z makietą (`docs/screens/odebrane/screen.png`) możliwie wiernie.
- **NFR-003**: Aplikacja MUSI stosować system kolorów zgodny z dotychczasowymi ekranami i paletą e-Komornik (primary: #001e40, primary-container: #003366, secondary-container: #fd8b00 — zgodnie z makietą).
- **NFR-004**: Architektura feature'a MUSI być warstwowa: `presentation` / `domain` / `data`, bez mieszania warstw.
- **NFR-005**: Żadna logika biznesowa NIE MOŻE znajdować się bezpośrednio w Composable.
- **NFR-006**: Feature MUSI być realizowany **bez Hilt** (zgodnie z decyzją projektową dla bieżącej fazy).
- **NFR-007**: Room nie jest wymagany w tym feature'ze, chyba że wyniknie z etapu planowania z uzasadnieniem.
- **NFR-008**: Lista z wieloma wiadomościami MUSI być płynnie przewijalna bez degradacji wydajności.
- **NFR-009**: Wszystkie ciągi tekstowe widoczne dla użytkownika MUSZĄ być zdefiniowane w zasobach strings (`res/values/strings.xml`).
- **NFR-010**: Ikony MUSZĄ pochodzić z zestawu Material Symbols / Material Icons dostępnego w projekcie.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Po zalogowaniu użytkownik dociera do ekranu Odebrane w ciągu 1 sekundy (bez uwzględnienia czasu logowania).
- **SC-002**: Na ekranie Odebrane wyświetlana jest lista co najmniej 4 przykładowych przesyłek z pełnymi danymi (numer, nadawca, temat, data, status) — bez potrzeby podłączonego backendu.
- **SC-003**: Każdy z 5 stanów UI (Initial, Loading, Success, Empty, Error) jest osiągalny i weryfikowalny podczas testowania.
- **SC-004**: Wszystkie 3 filtry (Wszystkie, Nieprzeczytane, Ważne) działają poprawnie na danych mockowych — zmiana filtru widocznie wpływa na listę.
- **SC-005**: Wyszukiwanie po fragmencie nadawcy lub tematu zwraca poprawnie odfiltrowane wyniki w czasie rzeczywistym.
- **SC-006**: Ekran jest wizualnie zgodny z makietą — układ, kolory, ikony i hierarchia treści odpowiadają referencji (`screen.png`) w ocenie wizualnej.
- **SC-007**: Dolna nawigacja jest widoczna i zawiera 4 pozycje zgodne z makietą (Poczta, Do wysyłki, ADE, Ustawienia).
- **SC-008**: Kod ekranu jest zorganizowany warstwowo — weryfikowalna separacja presentation / domain / data podczas przeglądu kodu (code review).
- **SC-009**: Pull-to-refresh inicjuje stan Loading i ponowne załadowanie danych.
- **SC-010**: Błąd pobierania (np. symulowany w FakeRepository) powoduje wyświetlenie stanu Error z przyciskiem ponowienia.

---

## Zakres i Out-of-Scope

### W zakresie tego feature'a

- Ekran Odebrane z pełną listą przesyłek (dane mockowe)
- Stany UI: Loading, Success, Empty, Error
- Filtrowanie: Wszystkie, Nieprzeczytane, Ważne
- Wyszukiwanie po nadawcy / temacie / numerze sprawy
- Oznaczanie gwiazdką (lokalnie, bez backendowej persystencji)
- Pull-to-refresh
- Dolna nawigacja (Bottom Navigation Bar) — wizualna prezentacja, aktywna zakładka "Poczta"
- Górny pasek aplikacji (Top App Bar)
- FAB z ikoną edycji
- Punkt nawigacyjny do szczegółów przesyłki (bez implementacji ekranu docelowego)
- FakeRepository z przykładowymi danymi

### Poza zakresem tego feature'a (Out-of-Scope)

- Ekran szczegółów przesyłki (oddzielny feature)
- Zmiana isRead=true po kliknięciu pozycji (należy do feature'u szczegółów)
- Podłączenie do backendowego API
- Persystencja danych (Room, DataStore)
- Powiadomienia push
- Ekrany: Wysłane, Do wysyłki, ADE, Ustawienia (oddzielne feature'y)
- Ekran tworzenia nowej wiadomości (oddzielny feature)
- Biometria i zarządzanie sesją (poza zakresem bieżącego feature'a)
- Paginacja, infinite scroll, load-more
- Auto-wylogowanie przy błędzie 401
- Reset hasła

---

## Nawigacja po zalogowaniu (powiązanie z 001-user-auth)

Feature `002-inbox-odebrane` jest bezpośrednią kontynuacją `001-user-auth`. Po poprawnym zalogowaniu użytkownika (ekran logowania) aplikacja musi wykonać nawigację do ekranu Odebrane. Ekran Odebrane musi być zarejestrowany jako docelowy ekran startowy po zalogowaniu w grafie nawigacji aplikacji.

---

## Assumptions

- Backend API dla skrzynki odbiorczej nie jest dostępny na etapie realizacji tego feature'a — `FakeRepository` jest akceptowalnym i oczekiwanym rozwiązaniem na tym etapie.
- Oznaczanie gwiazdką w tej fazie działa wyłącznie lokalnie (stan w pamięci / ViewModel), bez synchronizacji z backendem.
- Oznaczanie wiadomości jako przeczytanej po otwarciu szczegółów jest poza zakresem (brak ekranu szczegółów w tym feature'ze).
- Dolna nawigacja w tym feature'ze jest wizualnie kompletna (4 zakładki), ale zakładki "Do wysyłki", "ADE" i "Ustawienia" nie prowadzą jeszcze do zaimplementowanych ekranów.
- Liczba nowych wiadomości w badge'u jest obliczana lokalnie na podstawie danych mockowych.
- Kolorystyka i system designu: Material 3 z paletą e-Komornik jak w `code.html` (primary-container: `#003366`, secondary-container: `#fd8b00`).
- Projekt nie używa Hilt na bieżącym etapie — zależności (FakeRepository, ViewModel) są dostarczane ręcznie lub przez prosty factory.

