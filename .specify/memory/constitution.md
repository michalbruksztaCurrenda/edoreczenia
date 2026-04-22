<!--
SYNC IMPACT REPORT
==================
Version change:    (template) → 1.0.0
Added sections:    All 21 zasad konstytucyjnych, Governance, Aktualny stos technologiczny
Removed sections:  Wszystkie placeholdery szablonu
Modified principles: N/A (pierwsze wypełnienie szablonu)

Templates requiring updates:
  ✅  .specify/memory/constitution.md  (ten plik)
  ⚠   .specify/templates/plan-template.md    (nie istnieje — do utworzenia)
  ⚠   .specify/templates/spec-template.md    (nie istnieje — do utworzenia)
  ⚠   .specify/templates/tasks-template.md   (nie istnieje — do utworzenia)

Deferred TODOs:
  TODO(ANDROID_REQUIREMENTS_MD): Plik android_requirements.md jeszcze nie istnieje.
    Musi zostać utworzony i traktowany jako nadrzędne źródło wymagań biznesowych.
-->

# Konstytucja Projektu: eDoreczenia

**Wersja:** 1.0.0
**Data ratyfikacji:** 2026-04-22
**Ostatnia zmiana:** 2026-04-22
**Namespace:** `com.edoreczenia`
**minSdk:** 34 | **targetSdk / compileSdk:** 36 | **JVM:** 17

---

## Źródła prawdy

| Źródło | Rola |
|---|---|
| `android_requirements.md` | Nadrzędne wymagania biznesowe i funkcjonalne |
| `app/build.gradle.kts` | Konfiguracja budowania, SDK, zależności runtime |
| `gradle/libs.versions.toml` | Wersje wszystkich bibliotek i pluginów |
| Ten plik (`.specify/memory/constitution.md`) | Nadrzędne zasady architektoniczne, jakościowe i organizacyjne |

**Hierarchia rozstrzygania sprzeczności:** Konstytucja → `android_requirements.md` → implementacja.

---

## Aktualny stos technologiczny (źródło: `libs.versions.toml` + `build.gradle.kts`)

| Komponent | Wersja |
|---|---|
| Android Gradle Plugin | 9.0.0 |
| Kotlin | 2.3.20 |
| compileSdk / targetSdk | 36 |
| minSdk | 34 |
| Core KTX | 1.18.0 |
| Lifecycle Runtime KTX | 2.10.0 |
| Activity Compose | 1.13.0 |
| Compose BOM | 2026.03.00 |
| Material 3 | (via Compose BOM) |
| AndroidX Biometric | 1.1.0 |
| CameraX | 1.5.0 |
| ML Kit Face Detection | 16.1.7 |
| JVM toolchain | 17 |

---

## Core Principles

### I. Cel projektu

Aplikacja mobilna Android przeznaczona do bezpiecznego zarządzania dokumentami
i korespondencją elektroniczną (eDoręczenia).

Zakres funkcjonalny systemu:
- Logowanie i rejestracja użytkownika
- Skrzynka odbiorcza, wysłane, robocze (outbox)
- Wyszukiwanie adresatów w bazie ADE
- Tworzenie i wysyłanie wiadomości
- Obsługa załączników (upload, podgląd, pobieranie)
- Ustawienia aplikacyjne
- Bezpieczeństwo urządzenia: biometria, blokada ekranu, zarządzanie sesją

### II. Platforma i stos technologiczny

Projekt tworzony wyłącznie dla systemu **Android**.

MUST:
- Stosować wyłącznie technologie zgodne z `build.gradle.kts` i `libs.versions.toml`.
- Centralizować wszystkie wersje bibliotek w `libs.versions.toml` — nigdzie indziej.
- Opakowywać integracje z zewnętrznymi bibliotekami we wewnętrzne abstrakcje
  (wrapper/adapter), aby ograniczyć wpływ zmian zewnętrznych na resztę systemu.
- Używać wyłącznie stabilnych, sprawdzonych wersji bibliotek.

MUST NOT:
- Projektować kodu sztywno uzależnionego od konkretnej wersji biblioteki, jeśli nie
  wynika to z bezpośrednich ograniczeń technicznych.
- Deklarować wersji bibliotek bezpośrednio w `build.gradle.kts` — tylko przez
  katalog wersji (`libs.*`).

### III. Architektura warstwowa (NON-NEGOTIABLE)

Aplikacja MUSI być rozwijana w architekturze warstwowej, czytelnej i testowalnej.

```
presentation/   → UI, stan ekranu (UiState), nawigacja, ViewModele
domain/         → logika biznesowa, use case'y, reguły domenowe,
                  kontrakty repozytoriów
data/           → implementacje repozytoriów, klienty API, lokalne źródła danych,
                  mapowanie modeli DTO ↔ domenowych
core/           → konfiguracja, bezpieczeństwo, wspólne komponenty UI,
                  obsługa błędów, narzędzia, modele bazowe, helpery
```

Dozwolony kierunek zależności:

```
presentation ──► domain ──► core
presentation ──► core
data         ──► domain (tylko kontrakty/modele), core
```

`core` NIE może zależeć od `presentation`.
`domain` NIE może zależeć od implementacji warstwy `data` ani szczegółów UI.
Nie wolno omijać warstw ani mieszać odpowiedzialności pakietów.

### IV. Organizacja feature'ów i modularność

Każdy feature jako osobny pakiet z wewnętrznym podziałem na warstwy:

```
feature/login, registration, inbox, sent, outbox,
        ade-search, compose-message, attachments,
        settings, security
```

MUST:
- Wydzielać każdą większą funkcjonalność jako osobny feature z podziałem
  na UI, logikę domenową i dane.
- Projektować strukturę z myślą o przyszłych rozszerzeniach: nowe skrzynki,
  kanały komunikacji, OCR, podpis elektroniczny, notyfikacje, synchronizacja,
  tryb offline.

MUST NOT:
- Tworzyć klas wielozadaniowych (god objects).
- Silnie sprzęgać feature'y ze sobą przez bezpośrednie zależności.

### V. Zarządzanie zależnościami i wersjami

MUST:
- Korzystać z wersji stabilnych i sprawdzonych.
- Zarządzać wersjami centralnie przez `libs.versions.toml`.
- Opakowywać integracje biblioteczne tak, aby zmiany wersji zewnętrznych
  bibliotek miały minimalny wpływ na warstwy `domain` i `presentation`.

MUST NOT:
- Uzależniać logiki biznesowej od niestabilnych szczegółów implementacyjnych
  konkretnych wersji bibliotek.
- Hardcodować wersji w plikach `build.gradle.kts`.

### VI. Zakaz hardcodowania (NON-NEGOTIABLE)

Nigdy nie wolno hardcodować w kodzie źródłowym:
- Adresów URL i endpointów API
- Kluczy API, tokenów, sekretów, certyfikatów
- Danych użytkownika i środowiskowych
- Identyfikatorów środowisk (dev / staging / prod)
- Konfiguracji bezpieczeństwa
- Tekstów biznesowych podatnych na zmiany
- Wartości konfiguracyjnych zależnych od środowiska lub wdrożenia

Dozwolone źródła konfiguracji: `BuildConfig` | bezpieczny storage (Keystore /
EncryptedPrefs) | warstwa ustawień | backend | zmienne środowiskowe CI/CD.

Komunikaty błędów i etykiety UI podatne na lokalizację MUSZĄ być zarządzane
centralnie (zasoby stringów lub i18n).

### VII. Obsługa błędów — 5 stanów (NON-NEGOTIABLE)

Każdy ekran i każda operacja biznesowa MUSI obsługiwać 5 stanów UI:

```
Initial  →  Loading  →  Success<T>
                     →  Empty
                     →  Error(AppError)
```

Typy błędów do rozróżnienia:

| Typ | Przykłady |
|---|---|
| Sieciowy | brak połączenia, timeout |
| HTTP | 4xx, 5xx |
| Autoryzacyjny | 401, 403, wygaśnięcie sesji |
| Walidacyjny | nieprawidłowe dane wejściowe |
| Biznesowy | reguły domenowe |
| Techniczny | nieoczekiwane wyjątki |

MUST:
- Stosować wspólny model `AppError` i wspólne mapowanie na komunikaty użytkownika.
- Zapewniać czytelne, bezpieczne i użyteczne komunikaty błędów.

MUST NOT:
- Zwracać surowych wyjątków (`Exception`, `Throwable`) ani stacktrace'ów do UI.
- Wyświetlać technicznych komunikatów błędów użytkownikowi końcowemu.

### VIII. Bezpieczeństwo danych (NON-NEGOTIABLE)

Bezpieczeństwo danych jest zasadą nadrzędną — ma pierwszeństwo przed wygodą
implementacji.

MUST:
- Przechowywać dane wrażliwe wyłącznie przez Android Keystore /
  EncryptedSharedPreferences.
- Stosować HTTPS z odpowiednią konfiguracją `network_security_config.xml`.
- Implementować bezpieczne zarządzanie sesją: odświeżanie tokenów,
  wylogowanie, czyszczenie danych po wygaśnięciu sesji.
- Chronić dostęp do aplikacji przez biometrię lub mechanizmy urządzenia
  (zgodnie z `android_requirements.md`).

MUST NOT:
- Przechowywać sekretów, kluczy ani tokenów w kodzie źródłowym, repozytorium
  ani w jawnych plikach konfiguracyjnych.
- Logować danych wrażliwych (tokeny, treści wiadomości, dane identyfikacyjne).

### IX. Dane lokalne i przechowywanie

Każde przechowywanie danych lokalnych MUSI być uzasadnione funkcjonalnie.

| Typ danych | Wymagany mechanizm |
|---|---|
| Wrażliwe (tokeny, sesja) | Android Keystore / EncryptedSharedPreferences |
| Konfiguracyjne | DataStore (Preferences lub Proto) |
| Cache | Room z kontrolowanym TTL |
| Trwałe | Room |
| Pliki tymczasowe (kamera, załączniki) | Cache dir z jawnym czyszczeniem |

Załączniki, pliki tymczasowe i obrazy z aparatu MUSZĄ być zarządzane ze
świadomością cyklu życia: jawne czyszczenie zasobów, ograniczanie wycieków danych.

### X. Komunikacja sieciowa

MUST:
- Kierować całą komunikację z backendem wyłącznie przez warstwę `data`
  i repozytoria.
- Stosować jawne modele DTO dla każdego zapytania i odpowiedzi.
- Mapować DTO na modele domenowe w warstwie `data`.
- Obsługiwać: timeout, retry dla wybranych operacji, błędy HTTP, brak sieci,
  błędy autoryzacji (401/403 z odświeżaniem tokenu), niespójne dane odpowiedzi.

MUST NOT:
- Wykonywać wywołań sieciowych z warstwy `presentation`.
- Ujawniać szczegółów implementacyjnych HTTP w warstwie `domain`.

### XI. UI i UX

MUST:
- Stosować Material 3 jako system designu.
- Projektować każdy ekran z myślą o: czytelności, prostocie, dostępności (a11y),
  przewidywalnym zachowaniu.
- Zachowywać jednolite wzorce: nawigacja, formularze, walidacja, komunikaty,
  listy, puste stany, akcje użytkownika.
- Budować komponenty UI jako reużywalne composable'e jeśli występują
  w więcej niż jednym miejscu.

Interfejs ma charakter aplikacji urzędowej / dokumentowej — mobile-first,
spójny wizualnie i funkcjonalnie.

### XII. Walidacja danych wejściowych

MUST:
- Walidować dane wejściowe po stronie klienta przed wysłaniem do backendu
  (tam gdzie uzasadnione).
- Zarządzać walidacją centralnie (dedykowane klasy walidatorów w warstwie
  `domain` lub `core`).
- Być zgodna z wymaganiami zdefiniowanymi w `android_requirements.md`.

MUST NOT:
- Traktować walidacji klienckiej jako zastępstwa walidacji serwerowej.
- Rozpraszać reguł walidacji bezpośrednio po komponentach UI (Composable).

### XIII. Testowalność i jakość kodu

MUST:
- Projektować logikę biznesową, walidację, mapowanie modeli i ViewModele
  jako jednostkowo testowalne.
- Stosować dependency injection (Hilt) we wszystkich komponentach wymagających
  zewnętrznych zależności.
- Wydzielać każdą istotną logikę do testowalnych komponentów.

MUST NOT:
- Tworzyć ukrytych zależności (static calls, singletony bez uzasadnienia).
- Osadzać logiki biznesowej bezpośrednio w komponentach UI (Composable).
- Budować integracji trudnych do mockowania w testach.

### XIV. Czytelność i utrzymywalność kodu

MUST:
- Stosować nazewnictwo jednoznaczne i zgodne z domeną systemu
  (eDoręczenia, skrzynka, wiadomość, adresat, załącznik, korespondencja).
- Preferować czytelną separację odpowiedzialności nad pozorną prostotą.

MUST NOT:
- Tworzyć skrótowych, nieczytelnych struktur dla zmniejszenia liczby plików.
- Stosować ogólnych nazw niepowiązanych z domeną (`Manager`, `Helper`, `Utils`
  bez kontekstu domenowego).

### XV. Logging i diagnostyka

MUST:
- Stosować poziomy logowania: `DEBUG`, `INFO`, `WARN`, `ERROR`.
- W buildach release ograniczać logowanie do minimum, bez danych wrażliwych.

MUST NOT:
- Logować: sekretów, tokenów, pełnych treści wiadomości, pełnych danych
  dokumentów, pełnych danych identyfikacyjnych użytkownika.
- Obniżać poziomu bezpieczeństwa w imię ułatwienia diagnostyki.

### XVI. Uprawnienia urządzenia i funkcje sprzętowe

MUST:
- Jawnie i kontrolowane zarządzać uprawnieniami: kamera, biometria, storage.
- Obsługiwać: odmowę zgody, cofnięcie zgody, brak dostępności sprzętu.
- Stosować graceful degradation przy braku uprawnień lub sprzętu.

MUST NOT:
- Zakładać, że każda funkcja sprzętowa jest zawsze dostępna.
- Żądać uprawnień bez uzasadnienia funkcjonalnego.

### XVII. Dokumentacja i źródła prawdy

`android_requirements.md` MUSI istnieć i być traktowany jako nadrzędne źródło
wymagań biznesowych i funkcjonalnych.

`app/build.gradle.kts` i `gradle/libs.versions.toml` są źródłem prawdy dla
warstwy technicznej i zależności.

Ta konstytucja jest nadrzędnym zbiorem zasad architektonicznych, jakościowych
i organizacyjnych. Obowiązuje wszystkie specyfikacje, plany i zadania
implementacyjne.

### XVIII. Proces dostarczania funkcji — Spec-Driven Development

Każda większa funkcja MUSI przejść przez pełny proces:

```
1. specification   → opis wymagań, zakres, scenariusze, kryteria akceptacji
2. clarification   → pytania, doprecyzowanie, decyzje projektowe
3. technical plan  → architektura, komponenty, modele, API, schemat nawigacji
4. task breakdown  → lista atomowych zadań implementacyjnych
5. implementation  → kodowanie ściśle według zatwierdzonego planu
```

MUST NOT:
- Przechodzić do kodowania bez zatwierdzonej specyfikacji i planu technicznego.
- Implementować funkcji na skróty lub na podstawie domysłów.

### XIX. Zasady decyzji architektonicznych

Decyzje projektowe MUSZĄ być podejmowane z uwzględnieniem poniższych priorytetów
(w kolejności malejącej ważności):

1. **Bezpieczeństwo** — ochrona danych i użytkownika
2. **Utrzymywalność** — łatwość rozwoju przez kolejnych programistów
3. **Testowalność** — możliwość weryfikacji poprawności automatycznymi testami
4. **Odporność na zmiany** — izolacja przed zmianami zewnętrznych zależności
5. **Zgodność z wymaganiami** — realizacja wymagań z `android_requirements.md`
6. **Możliwość dalszego rozwoju** — otwartość architektury na nowe funkcje

W przypadku wątpliwości: preferuj rozwiązania prostsze, bezpieczniejsze
i łatwiejsze do utrzymania zamiast nadmiernie skomplikowanych.

### XX. Zakazy bezwzględne (NON-NEGOTIABLE)

| Zakaz | Uzasadnienie |
|---|---|
| Omijanie warstw architektury | Narusza separację odpowiedzialności |
| Mieszanie logiki biznesowej z UI | Uniemożliwia testowanie i reużywalność |
| Przechowywanie sekretów w kodzie lub repozytorium | Krytyczne ryzyko bezpieczeństwa |
| Uzależnianie systemu od szczegółów jednej biblioteki | Blokuje aktualizacje i migracje |
| Budowanie funkcji bez obsługi stanów błędów | Degraduje jakość UX i niezawodność |
| Budowanie funkcji bez uwzględnienia bezpieczeństwa | Narusza zasadę VIII |
| Wywołania sieciowe z warstwy presentation | Narusza zasadę X |
| Zwracanie surowych wyjątków do UI | Narusza zasadę VII |
| Hardcodowanie URL, kluczy, tokenów, konfiguracji | Narusza zasadę VI |

---

## Governance

### Procedura zmiany konstytucji

1. Propozycja zmiany z uzasadnieniem i oceną wpływu na istniejące zasady.
2. Weryfikacja zgodności z hierarchią źródeł prawdy.
3. Aktualizacja wersji wg SemVer:
   - **MAJOR** — usunięcie lub redefinicja zasady (wstecznie niekompatybilne).
   - **MINOR** — nowa zasada lub istotne rozszerzenie istniejącej.
   - **PATCH** — doprecyzowanie, poprawka redakcyjna, brak zmiany semantycznej.
4. Aktualizacja `Last Amended` na datę zmiany (format `YYYY-MM-DD`).
5. Propagacja zmian do powiązanych szablonów (plan, spec, tasks).

### Zgodność

Każdy PR i każda zmiana architektoniczna MUSZĄ być weryfikowane pod kątem
zgodności z zasadami tej konstytucji przed scaleniem do gałęzi głównej.

Konstytucja ma prowadzić cały projekt i obowiązuje wszystkie kolejne
specyfikacje, plany i zadania implementacyjne.

**Version**: 1.0.0 | **Ratified**: 2026-04-22 | **Last Amended**: 2026-04-22
