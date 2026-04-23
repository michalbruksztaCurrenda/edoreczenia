# Android Requirements — eDoreczenia

## 1. Cel aplikacji
Aplikacja eDoreczenia jest natywną aplikacją Android do obsługi korespondencji i dokumentów elektronicznych. Umożliwia użytkownikowi logowanie, rejestrację, weryfikację konta, przeglądanie skrzynki wiadomości, wyszukiwanie adresatów, tworzenie wiadomości, obsługę załączników oraz zarządzanie ustawieniami korespondencji.

## 2. Zakres funkcjonalny
Aplikacja ma wspierać co najmniej następujące obszary:
- logowanie użytkownika
- rejestracja nowego użytkownika
- potwierdzenie konta kodem weryfikacyjnym wysłanym na e-mail
- skrzynka odebrane
- skrzynka wysłane
- skrzynka do wysyłki
- wyszukiwarka ADE
- tworzenie nowej wiadomości
- dodawanie i obsługa załączników
- ustawienia modułu korespondencji
- funkcje bezpieczeństwa i zarządzania sesją

## 3. Platforma
- system docelowy: Android
- namespace aplikacji: `com.edoreczenia`
- minSdk: 34
- targetSdk: 36
- compileSdk: 36
- JVM: 17

## 4. Główne ekrany
- Logowanie
- Rejestracja użytkownika
- Potwierdzenie e-mail
- Odebrane
- Wysłane
- Do wysyłki
- Wyszukiwarka ADE
- Nowa wiadomość
- Ustawienia korespondencji

## 5. Moduł uwierzytelniania
### 5.1 Logowanie
Użytkownik loguje się za pomocą loginu/nazwy użytkownika i hasła.

### 5.2 Rejestracja
Użytkownik rejestruje konto, podając:
- nazwę urządzenia
- nazwę użytkownika
- adres e-mail
- hasło
- potwierdzenie hasła

### 5.3 Weryfikacja konta
Po rejestracji użytkownik potwierdza konto kodem wysłanym na adres e-mail.

### 5.4 Sesja
- sesja trwa tylko w czasie działania aplikacji
- sesja nie przetrwa między uruchomieniami aplikacji
- po poprawnej weryfikacji konta użytkownik wraca do ekranu logowania
- użytkownik nie jest logowany automatycznie po weryfikacji

### 5.5 Poza zakresem modułu auth
- reset hasła
- logowanie biometryczne

## 6. Bezpieczeństwo
- aplikacja nie przechowuje lokalnie hasła użytkownika
- aplikacja nie przechowuje lokalnie kodu weryfikacyjnego
- dane uwierzytelniające i dane konta są przechowywane po stronie backendu
- aplikacja korzysta z backendowego API jako źródła prawdy
- komunikacja sieciowa musi odbywać się przez HTTPS
- sesja i dane wrażliwe muszą być obsługiwane zgodnie z zasadami bezpieczeństwa określonymi w konstytucji projektu

## 7. Założenia architektoniczne
- aplikacja jest klientem mobilnym
- backend i baza użytkowników znajdują się poza aplikacją Android
- logika biznesowa nie może być mieszana z UI
- architektura aplikacji musi być warstwowa: presentation, domain, data, core
- wersje bibliotek są zarządzane centralnie przez `libs.versions.toml`

## 8. UX i UI
- aplikacja ma być mobile-first
- interfejs ma być spójny i zgodny z charakterem aplikacji urzędowej / dokumentowej
- należy stosować spójne wzorce walidacji, błędów, loadingu i pustych stanów
- materiały referencyjne ekranów znajdują się w katalogu `docs/screens/`

## 9. Źródła referencyjne ekranów
- `docs/screens/logowanie/`
- `docs/screens/rejestracja_u_ytkownika/`
- `docs/screens/potwierdzenie_e_mail/`
- `docs/screens/odebrane/`
- `docs/screens/wys_ane/`
- `docs/screens/do_wysy_ki/`
- `docs/screens/nowa_wiadomo/`
- `docs/screens/wyszukiwarka_ade/`
- `docs/screens/ustawienia_korespondencji/`

## 10. Priorytet realizacji
Pierwszy realizowany feature:
- `001-user-auth`

Kolejne planowane feature’y:
- mailbox management
- compose message
- ADE search
- settings and security

## 11. Zasada realizacji
Każda większa funkcjonalność musi przejść przez:
- specification
- clarification
- technical plan
- task breakdown
- implementation

Kodowanie bez zakończonych wcześniejszych etapów nie jest dozwolone.