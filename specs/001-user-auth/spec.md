# Feature Specification: Uwierzytelnianie Użytkownika (User Authentication)

**Feature Branch**: `001-user-auth`  
**Created**: 2026-04-22  
**Status**: Draft  
**Input**: User description: "Moduł uwierzytelniania użytkownika — logowanie, rejestracja, weryfikacja konta e-mail, utrzymanie sesji, wylogowanie."

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Logowanie do aplikacji (Priority: P1)

Użytkownik posiadający aktywne, zweryfikowane konto może zalogować się do aplikacji podając login (nazwę użytkownika) oraz hasło. Po poprawnym uwierzytelnieniu trafia do głównego widoku aplikacji (skrzynki odbiorczej). Sesja jest utrzymywana między uruchomieniami aplikacji do czasu jej wygaśnięcia lub wylogowania.

**Why this priority**: Logowanie jest bramą dostępu do całej aplikacji — bez niego żaden inny moduł nie jest dostępny. Dostarcza wartość niezwłocznie po zaimplementowaniu.

**Independent Test**: Można przetestować w izolacji: wystarczy mieć istniejące konto, uruchomić ekran logowania i zweryfikować, że poprawne dane prowadzą do głównego widoku, a niepoprawne — do czytelnego komunikatu błędu.

**Acceptance Scenarios**:

1. **Given** użytkownik jest na ekranie logowania, **When** wprowadza poprawny login i hasło i zatwierdza formularz, **Then** aplikacja wyświetla stan ładowania, a następnie przekierowuje użytkownika do głównego widoku aplikacji.
2. **Given** użytkownik jest na ekranie logowania, **When** wprowadza niepoprawne dane (błędny login lub hasło) i zatwierdza formularz, **Then** aplikacja wyświetla czytelny komunikat błędu uwierzytelniania bez ujawniania, które pole jest błędne (bezpieczeństwo).
3. **Given** użytkownik jest na ekranie logowania, **When** próbuje zatwierdzić formularz z pustymi polami, **Then** aplikacja wyświetla komunikaty walidacyjne przy odpowiednich polach, nie wysyłając żądania do backendu.
4. **Given** użytkownik jest na ekranie logowania, **When** konto istnieje, ale nie zostało jeszcze zweryfikowane, **Then** aplikacja informuje użytkownika, że konto oczekuje na weryfikację e-mail i oferuje opcję ponownego wysłania kodu.
5. **Given** sesja użytkownika jest aktywna (nie wygasła) i użytkownik restartuje aplikację, **When** aplikacja się uruchamia, **Then** użytkownik trafia bezpośrednio do głównego widoku bez konieczności ponownego logowania.
6. **Given** sesja użytkownika wygasła, **When** aplikacja się uruchamia lub użytkownik wykonuje chronioną operację, **Then** użytkownik zostaje przekierowany na ekran logowania z odpowiednim komunikatem.
7. **Given** brak połączenia z siecią, **When** użytkownik próbuje się zalogować, **Then** aplikacja wyświetla czytelny komunikat o braku połączenia z możliwością ponowienia próby.

---

### User Story 2 — Rejestracja nowego konta (Priority: P2)

Nowy użytkownik może założyć konto w systemie podając wymagane dane: nazwę urządzenia, nazwę użytkownika, adres e-mail, hasło i potwierdzenie hasła. Po pomyślnej rejestracji użytkownik jest informowany o konieczności weryfikacji adresu e-mail.

**Why this priority**: Bez możliwości rejestracji aplikacja nie pozyskuje nowych użytkowników. Rejestracja zależy od P1 (logowania) jako naturalny przepływ onboardingu.

**Independent Test**: Można przetestować przez wypełnienie formularza rejestracyjnego poprawnymi danymi i weryfikację, że system tworzy konto i przechodzi do ekranu weryfikacji e-mail.

**Acceptance Scenarios**:

1. **Given** użytkownik jest na ekranie rejestracji, **When** wypełnia wszystkie wymagane pola poprawnymi danymi i zatwierdza formularz, **Then** system tworzy konto, wyświetla stan ładowania, a następnie przekierowuje na ekran weryfikacji e-mail.
2. **Given** użytkownik jest na ekranie rejestracji, **When** podaje nazwę użytkownika lub adres e-mail już zajęty w systemie, **Then** aplikacja wyświetla czytelny komunikat informujący o konflikcie (zajęty login lub e-mail).
3. **Given** użytkownik jest na ekranie rejestracji, **When** pole „Potwierdź hasło" różni się od pola „Hasło", **Then** aplikacja blokuje wysyłkę formularza i wyświetla komunikat walidacyjny przy polu potwierdzenia.
4. **Given** użytkownik jest na ekranie rejestracji, **When** hasło nie spełnia wymagań polityki haseł, **Then** aplikacja wyświetla komunikat walidacyjny opisujący wymagania.
5. **Given** użytkownik jest na ekranie rejestracji, **When** adres e-mail ma niepoprawny format, **Then** aplikacja wyświetla komunikat walidacyjny przed wysłaniem formularza.
6. **Given** użytkownik jest na ekranie rejestracji, **When** próbuje zatwierdzić formularz z pustymi wymaganymi polami, **Then** aplikacja wyświetla komunikaty walidacyjne przy każdym pustym wymaganym polu.
7. **Given** użytkownik jest na ekranie rejestracji, **When** kliknie link/przycisk powrotu do logowania, **Then** aplikacja przechodzi na ekran logowania bez utraty stanu (lub z pytaniem, jeśli formularz był częściowo wypełniony).

---

### User Story 3 — Weryfikacja konta kodem e-mail (Priority: P3)

Po rejestracji użytkownik otrzymuje na podany adres e-mail kod weryfikacyjny. Musi wprowadzić ten kod w ekranie weryfikacji, aby aktywować konto i móc się zalogować.

**Why this priority**: Weryfikacja e-mail jest wymagana do aktywacji konta — bez niej użytkownik nie może się zalogować. Zależy od P2 (rejestracji).

**Independent Test**: Można przetestować przez symulację otrzymania kodu i weryfikację, że poprawny kod aktywuje konto, a błędny kod — wyświetla odpowiedni komunikat.

**Acceptance Scenarios**:

1. **Given** użytkownik jest na ekranie weryfikacji e-mail po rejestracji, **When** wprowadza poprawny, nieważący kod weryfikacyjny i zatwierdza, **Then** konto zostaje aktywowane i użytkownik jest przekierowany do ekranu logowania (lub automatycznie zalogowany).
2. **Given** użytkownik jest na ekranie weryfikacji e-mail, **When** wprowadza niepoprawny kod, **Then** aplikacja wyświetla czytelny komunikat błędu z informacją o pozostałych próbach (jeśli system stosuje limit prób).
3. **Given** użytkownik jest na ekranie weryfikacji e-mail, **When** kod weryfikacyjny wygasł, **Then** aplikacja informuje o wygaśnięciu kodu i oferuje opcję ponownego wysłania.
4. **Given** użytkownik jest na ekranie weryfikacji e-mail, **When** klika „Wyślij kod ponownie", **Then** system wysyła nowy kod na ten sam adres e-mail, a stary kod traci ważność.
5. **Given** użytkownik wraca do aplikacji po dłuższym czasie bez weryfikacji konta, **When** loguje się z niezweryfikowanym kontem, **Then** aplikacja przekierowuje go na ekran weryfikacji z możliwością ponownego wysłania kodu.

---

### User Story 4 — Wylogowanie (Priority: P4)

Zalogowany użytkownik może wylogować się z aplikacji w dowolnym momencie. Po wylogowaniu sesja jest unieważniana, dane sesji czyszczone, a użytkownik przekierowywany na ekran logowania.

**Why this priority**: Wylogowanie jest ważną funkcją bezpieczeństwa, ale nie blokuje podstawowego korzystania z aplikacji — może być dostarczane po funkcjach P1–P3.

**Independent Test**: Można przetestować przez wylogowanie z poziomu ustawień lub dedykowanego przycisku i weryfikację, że sesja jest czyszczona, a kolejny restart aplikacji trafia na ekran logowania.

**Acceptance Scenarios**:

1. **Given** użytkownik jest zalogowany, **When** inicjuje wylogowanie, **Then** aplikacja czyści dane sesji (tokeny, stan użytkownika) i przekierowuje na ekran logowania.
2. **Given** użytkownik wylogował się i restartuje aplikację, **When** aplikacja się uruchamia, **Then** użytkownik trafia na ekran logowania, nie do głównego widoku.
3. **Given** sesja wygasła po stronie serwera podczas aktywnej sesji aplikacji, **When** użytkownik wykonuje dowolną chronioną operację, **Then** aplikacja automatycznie wylogowuje użytkownika, czyści stan sesji i przekierowuje na ekran logowania z odpowiednim komunikatem.

---

### Edge Cases

- Co się stanie, gdy użytkownik wielokrotnie (np. 5+ razy) poda błędne hasło? → Aplikacja musi obsłużyć odpowiedź z backendu (np. tymczasowa blokada konta) i wyświetlić stosowny komunikat.
- Co się stanie, gdy e-mail weryfikacyjny nie dotrze? → Użytkownik musi mieć możliwość ponownego wysłania kodu z ekranu weryfikacji.
- Co się stanie, gdy użytkownik ma wiele urządzeń z aktywną sesją i wyloguje się na jednym? → Sesja na danym urządzeniu jest czyszczona; zachowanie pozostałych sesji zależy od polityki backendu (zakładane: niezależne sesje per urządzenie).
- Co się stanie przy jednoczesnej utracie sieci i wygaśnięciu sesji? → Aplikacja powinna zachować spójność — po przywróceniu sieci wymagane jest ponowne zalogowanie.
- Co się stanie, gdy użytkownik cofnie się (back) z ekranu weryfikacji po rejestracji? → Aplikacja powinna powrócić do ekranu rejestracji lub logowania, bez możliwości cofnięcia się do stanu „już zarejestrowany bez weryfikacji" w nieskończoność.
- Co się stanie, gdy kod weryfikacyjny zostanie wpisany ze spacjami lub innymi białymi znakami? → Aplikacja powinna oczyścić (trim) dane wejściowe przed walidacją.
- Co się stanie, gdy formularz rejestracyjny zostanie wysłany wielokrotnie (podwójne kliknięcie)? → Aplikacja musi zabezpieczyć przed duplikatami żądań przez dezaktywację przycisku podczas stanu ładowania.
- Co się stanie, gdy aplikacja przejdzie w tło podczas procesu logowania/rejestracji? → Stan formularza musi być zachowany (lub odtworzony) po powrocie na pierwszy plan.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Logowanie

- **FR-001**: System MUSI umożliwiać użytkownikowi zalogowanie się przy użyciu nazwy użytkownika (loginu) i hasła.
- **FR-002**: System MUSI wyświetlać stan ładowania podczas weryfikacji danych logowania.
- **FR-003**: System MUSI po poprawnym zalogowaniu przekierować użytkownika do głównego widoku aplikacji.
- **FR-004**: System MUSI wyświetlać czytelny, ujednolicony komunikat błędu przy niepoprawnych danych logowania, bez wskazywania, które pole jest błędne.
- **FR-005**: System MUSI walidować formularz logowania po stronie klienta (pola niepuste) przed wysłaniem żądania.
- **FR-006**: System MUSI obsługiwać błędy sieciowe podczas logowania z możliwością ponowienia próby.
- **FR-007**: System MUSI informować użytkownika próbującego zalogować się na niezweryfikowane konto o konieczności weryfikacji e-mail i oferować ponowne wysłanie kodu.

#### Rejestracja

- **FR-008**: System MUSI umożliwiać rejestrację nowego konta z podaniem: nazwy urządzenia, nazwy użytkownika, adresu e-mail, hasła i potwierdzenia hasła.
- **FR-009**: System MUSI walidować wszystkie pola formularza rejestracyjnego po stronie klienta przed wysłaniem żądania.
- **FR-010**: System MUSI sprawdzać, czy pole „Potwierdź hasło" jest identyczne z polem „Hasło" — w przypadku niezgodności blokować wysyłkę i wyświetlać komunikat walidacyjny.
- **FR-011**: System MUSI walidować format adresu e-mail po stronie klienta.
- **FR-012**: System MUSI weryfikować, czy hasło spełnia wymagania polityki haseł i wyświetlać czytelną informację o wymaganiach przy niespełnieniu warunków.
- **FR-013**: System MUSI obsługiwać odpowiedź z backendu informującą o zajętej nazwie użytkownika lub adresie e-mail i prezentować stosowny komunikat użytkownikowi.
- **FR-014**: System MUSI dezaktywować przycisk wysyłki formularza podczas stanu ładowania, aby zapobiec duplikatom żądań.
- **FR-015**: System MUSI po udanej rejestracji przekierować użytkownika na ekran weryfikacji e-mail.

#### Weryfikacja konta

- **FR-016**: System MUSI umożliwiać użytkownikowi wprowadzenie kodu weryfikacyjnego otrzymanego na adres e-mail.
- **FR-017**: System MUSI aktywować konto po wprowadzeniu poprawnego, ważnego kodu weryfikacyjnego.
- **FR-018**: System MUSI informować użytkownika o niepoprawnym kodzie weryfikacyjnym z czytelnym komunikatem błędu.
- **FR-019**: System MUSI informować użytkownika o wygaśnięciu kodu i oferować możliwość ponownego wysłania.
- **FR-020**: System MUSI umożliwiać ponowne wysłanie kodu weryfikacyjnego na ten sam adres e-mail, unieważniając poprzedni kod.
- **FR-021**: System MUSI oczyścić (trim) kod weryfikacyjny z białych znaków przed walidacją.

#### Sesja

- **FR-022**: System MUSI utrzymywać aktywną sesję użytkownika między restartami aplikacji, o ile sesja nie wygasła i użytkownik nie wylogował się.
- **FR-023**: System MUSI przechowywać dane sesji (tokeny uwierzytelniające) wyłącznie w bezpiecznym, szyfrowanym magazynie urządzenia.
- **FR-024**: System MUSI automatycznie przekierować użytkownika na ekran logowania przy wykryciu wygasłej lub unieważnionej sesji, z czytelnym komunikatem.
- **FR-025**: System MUSI po wylogowaniu trwale usunąć dane sesji z urządzenia.

#### Nawigacja

- **FR-026**: Ekran logowania MUSI zawierać możliwość przejścia do ekranu rejestracji.
- **FR-027**: Ekran rejestracji MUSI zawierać możliwość powrotu do ekranu logowania.
- **FR-028**: Ekran weryfikacji e-mail MUSI zawierać możliwość powrotu do ekranu rejestracji lub logowania.
- **FR-029**: Po pomyślnej weryfikacji konta system MUSI przekierować użytkownika na ekran logowania lub — jeśli sesja jest aktywna — bezpośrednio do głównego widoku aplikacji.

#### Stany UI (zgodnie z Konstytucją — zasada VII)

- **FR-030**: Każdy z ekranów modułu (logowanie, rejestracja, weryfikacja) MUSI obsługiwać stany: Initial, Loading, Success, Error.
- **FR-031**: Ekran logowania w stanie Loading MUSI blokować interakcję użytkownika z formularzem.
- **FR-032**: Stany Error MUSZĄ prezentować użytkownikowi czytelny komunikat z możliwością podjęcia akcji naprawczej (ponów, popraw dane, itp.).

### Walidacja pól

| Pole | Wymagania walidacyjne |
|---|---|
| Login / Nazwa użytkownika | Niepuste |
| Hasło (logowanie) | Niepuste |
| Nazwa urządzenia | Niepuste |
| Nazwa użytkownika (rejestracja) | Niepuste, unikalność weryfikowana przez backend |
| Adres e-mail | Niepuste, poprawny format e-mail, unikalność weryfikowana przez backend |
| Hasło (rejestracja) | Niepuste, spełnienie polityki haseł (minimalna długość i złożoność per wymagania backendu) |
| Potwierdzenie hasła | Niepuste, zgodność z polem Hasło |
| Kod weryfikacyjny | Niepuste, numeryczny lub alfanumeryczny wg formatu backendu, trim białych znaków |

### Key Entities

- **Użytkownik**: Podmiot rejestrujący się i uwierzytelniający w systemie. Posiada: nazwę użytkownika, adres e-mail, stan konta (niezweryfikowane / aktywne / zablokowane), powiązane urządzenia.
- **Sesja**: Reprezentacja aktywnego uwierzytelnienia użytkownika na urządzeniu. Posiada: identyfikator sesji, czas wygaśnięcia, dane uwierzytelniające (tokeny). Przechowywana wyłącznie w bezpiecznym magazynie.
- **Kod weryfikacyjny**: Jednorazowy kod wysyłany na adres e-mail użytkownika po rejestracji. Posiada: wartość, czas wygaśnięcia, flagę wykorzystania.
- **Urządzenie**: Kontekst rejestracji i sesji — rozróżnia konta zarejestrowane z różnych urządzeń przez tego samego użytkownika.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Użytkownik z aktywnym, zweryfikowanym kontem może zalogować się do aplikacji w czasie poniżej 5 sekund przy standardowym połączeniu sieciowym.
- **SC-002**: Użytkownik może zarejestrować nowe konto i dotrzeć do ekranu weryfikacji e-mail w czasie poniżej 3 minut.
- **SC-003**: 100% ekranów modułu uwierzytelniania obsługuje wszystkie wymagane stany UI (Initial, Loading, Error) i nie wyświetla niezobsługiwanych wyjątków ani technicznych komunikatów błędów użytkownikowi końcowemu.
- **SC-004**: Dane uwierzytelniające (tokeny sesji, hasła) nigdy nie są widoczne w logach aplikacji ani przechowywane poza bezpiecznym magazynem urządzenia.
- **SC-005**: Użytkownik z aktywną sesją (nie wygasłą) po restarcie aplikacji trafia bezpośrednio do głównego widoku bez konieczności ponownego logowania — w 100% przypadków.
- **SC-006**: Użytkownik po wylogowaniu i restarcie aplikacji zawsze trafia na ekran logowania — sesja jest czyszczona natychmiastowo i trwale.
- **SC-007**: Wszystkie pola formularzy walidowane są po stronie klienta — użytkownik otrzymuje komunikat walidacyjny bez oczekiwania na odpowiedź sieciową w przypadku oczywistych błędów (puste pole, błędny format e-mail, niezgodne hasła).
- **SC-008**: Ekrany modułu uwierzytelniania są dostępne (a11y) — obsługują TalkBack i skalowanie tekstu bez utraty funkcjonalności.

---

## Assumptions

- Backend dostarcza API do rejestracji, logowania, weryfikacji kodu e-mail oraz odświeżania i unieważniania sesji. Szczegóły kontraktu API zostaną zdefiniowane w planie technicznym.
- Polityka haseł (minimalna długość, złożoność) jest definiowana po stronie backendu; klient wyświetla wymagania na podstawie konfiguracji lub stałych zdefiniowanych poza kodem źródłowym.
- Kod weryfikacyjny e-mail jest wysyłany przez backend — aplikacja nie wysyła e-maili samodzielnie.
- Sesja jest identyfikowana tokenem (lub parą tokenów: access + refresh) zarządzanym przez backend. Czas wygaśnięcia tokenu jest dostarczany przez backend.
- Zakłada się model jednej aktywnej sesji per urządzenie — wiele urządzeń może mieć jednocześnie aktywne sesje tego samego użytkownika.
- Funkcja biometryczna (odblokowanie aplikacji biometrią) jest zakresem osobnego feature'a (security) i nie wchodzi w zakres tego modułu. Ten moduł odpowiada wyłącznie za uwierzytelnienie hasłem przy logowaniu.
- Resetowanie zapomnianego hasła (forgot password) jest poza zakresem tego modułu i zostanie zaadresowane osobną specyfikacją.
- Materiały referencyjne UI (ekrany w `docs/screens/`) służą jako odniesienie wizualne i strukturalne — nie są nadrzędnym źródłem wymagań.
- Aplikacja działa na urządzeniach z minSdk 34 i wyższym zgodnie z konfiguracją projektu.
- Komunikaty błędów są zarządzane centralnie (zasoby stringów) i zgodne z zasadą VI Konstytucji.

