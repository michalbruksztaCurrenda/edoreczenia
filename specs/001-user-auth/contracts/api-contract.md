# API Contract: Auth Module

**Feature**: 001-user-auth
**Base URL**: `{BASE_URL}/api/v1/auth`
**Date**: 2026-04-23
**Status**: Obowiązujący — nie modyfikować bez weryfikacji z backendem

> Kontrakt API jest narzucony przez wymagania projektowe i stanowi jedyne
> źródło prawdy dla warstwy `data`. Aplikacja Android jest konsumentem,
> nie projektantem tego API.

---

## Wspólne nagłówki

### Request (endpointy chronione)
```
Authorization: Bearer <accessToken>
Content-Type: application/json
Accept: application/json
```

### Request (endpointy publiczne — register, login, verify, resend)
```
Content-Type: application/json
Accept: application/json
```

---

## Wspólny model błędu

```json
{
  "status": "VALIDATION_ERROR | AUTH_ERROR | BUSINESS_ERROR",
  "code": "STRING_CODE",
  "message": "Komunikat dla użytkownika.",
  "fieldErrors": {
    "fieldName": ["Komunikat błędu pola."]
  }
}
```

Pole `fieldErrors` jest opcjonalne (nullable).

---

## Endpoint 1: Rejestracja

```
POST /api/v1/auth/register
```

### Request

```json
{
  "deviceName": "Samsung Galaxy S24",
  "username": "jan.kowalski",
  "email": "jan.kowalski@example.com",
  "password": "Haslo123!",
  "confirmPassword": "Haslo123!"
}
```

### Walidacja po stronie backendu

| Pole | Reguła |
|---|---|
| deviceName | wymagane, 3–100 znaków |
| username | wymagane, unikalne, 3–50 znaków |
| email | wymagane, unikalne, poprawny format |
| password | zgodne z polityką haseł |
| confirmPassword | zgodne z password |

### Responses

**HTTP 201 Created**
```json
{
  "status": "REGISTRATION_PENDING_VERIFICATION",
  "userId": "usr_8f4c3f8e",
  "username": "jan.kowalski",
  "email": "jan.kowalski@example.com",
  "verificationRequired": true,
  "message": "Konto zostało utworzone. Wysłano kod weryfikacyjny na adres e-mail."
}
```

**HTTP 400 Bad Request**
```json
{
  "status": "VALIDATION_ERROR",
  "code": "INVALID_REQUEST",
  "message": "Dane formularza są nieprawidłowe.",
  "fieldErrors": {
    "deviceName": ["Nazwa urządzenia jest wymagana."],
    "email": ["Niepoprawny format adresu e-mail."]
  }
}
```

**HTTP 409 Conflict — username zajęty**
```json
{
  "status": "BUSINESS_ERROR",
  "code": "USERNAME_ALREADY_EXISTS",
  "message": "Nazwa użytkownika jest już zajęta."
}
```

**HTTP 409 Conflict — email zajęty**
```json
{
  "status": "BUSINESS_ERROR",
  "code": "EMAIL_ALREADY_EXISTS",
  "message": "Adres e-mail jest już używany."
}
```

**HTTP 422 Unprocessable Entity — naruszenie polityki haseł**
```json
{
  "status": "VALIDATION_ERROR",
  "code": "PASSWORD_POLICY_VIOLATION",
  "message": "Hasło nie spełnia wymagań bezpieczeństwa.",
  "fieldErrors": {
    "password": [
      "Hasło musi mieć co najmniej 8 znaków, zawierać małą i dużą literę oraz cyfrę."
    ]
  }
}
```

---

## Endpoint 2: Weryfikacja kodu e-mail

```
POST /api/v1/auth/verify-email-code
```

### Request

```json
{
  "username": "jan.kowalski",
  "verificationCode": "123456"
}
```

### Responses

**HTTP 200 OK**
```json
{
  "status": "ACCOUNT_VERIFIED",
  "userId": "usr_8f4c3f8e",
  "accountStatus": "ACTIVE",
  "message": "Konto zostało aktywowane."
}
```

**HTTP 400 Bad Request**
```json
{
  "status": "VALIDATION_ERROR",
  "code": "INVALID_VERIFICATION_CODE_FORMAT",
  "message": "Kod weryfikacyjny ma niepoprawny format."
}
```

**HTTP 401 Unauthorized**
```json
{
  "status": "AUTH_ERROR",
  "code": "INVALID_VERIFICATION_CODE",
  "message": "Kod weryfikacyjny jest nieprawidłowy."
}
```

**HTTP 410 Gone — kod wygasł**
```json
{
  "status": "BUSINESS_ERROR",
  "code": "VERIFICATION_CODE_EXPIRED",
  "message": "Kod weryfikacyjny wygasł."
}
```

**HTTP 423 Locked — za dużo błędnych prób**
```json
{
  "status": "BUSINESS_ERROR",
  "code": "VERIFICATION_LOCKED",
  "message": "Proces weryfikacji został tymczasowo zablokowany po zbyt wielu błędnych próbach."
}
```

---

## Endpoint 3: Ponowne wysłanie kodu

```
POST /api/v1/auth/resend-verification-code
```

### Request

```json
{
  "username": "jan.kowalski"
}
```

### Responses

**HTTP 200 OK**
```json
{
  "status": "VERIFICATION_CODE_RESENT",
  "message": "Wysłano nowy kod weryfikacyjny."
}
```

**HTTP 404 Not Found**
```json
{
  "status": "BUSINESS_ERROR",
  "code": "USER_NOT_FOUND",
  "message": "Nie znaleziono użytkownika."
}
```

**HTTP 409 Conflict — konto już zweryfikowane**
```json
{
  "status": "BUSINESS_ERROR",
  "code": "ACCOUNT_ALREADY_VERIFIED",
  "message": "Konto zostało już wcześniej zweryfikowane."
}
```

**HTTP 429 Too Many Requests**
```json
{
  "status": "BUSINESS_ERROR",
  "code": "RESEND_LIMIT_EXCEEDED",
  "message": "Przekroczono limit ponownych wysłań kodu."
}
```

---

## Endpoint 4: Logowanie

```
POST /api/v1/auth/login
```

### Request

```json
{
  "username": "jan.kowalski",
  "password": "Haslo123!"
}
```

### Responses

**HTTP 200 OK**
```json
{
  "status": "AUTHENTICATED",
  "user": {
    "id": "usr_8f4c3f8e",
    "username": "jan.kowalski",
    "email": "jan.kowalski@example.com",
    "accountStatus": "ACTIVE"
  },
  "session": {
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "tokenType": "Bearer",
    "expiresInSeconds": 900,
    "refreshExpiresInSeconds": 28800
  }
}
```

**HTTP 400 Bad Request**
```json
{
  "status": "VALIDATION_ERROR",
  "code": "INVALID_REQUEST",
  "message": "Dane logowania są nieprawidłowe."
}
```

**HTTP 401 Unauthorized**
```json
{
  "status": "AUTH_ERROR",
  "code": "INVALID_CREDENTIALS",
  "message": "Nieprawidłowy login lub hasło."
}
```

**HTTP 403 Forbidden — konto nieaktywne**
```json
{
  "status": "AUTH_ERROR",
  "code": "ACCOUNT_NOT_ACTIVE",
  "message": "Konto nie jest aktywne."
}
```

**HTTP 423 Locked**
```json
{
  "status": "AUTH_ERROR",
  "code": "ACCOUNT_LOCKED",
  "message": "Konto zostało zablokowane."
}
```

**HTTP 429 Too Many Requests**
```json
{
  "status": "AUTH_ERROR",
  "code": "TOO_MANY_LOGIN_ATTEMPTS",
  "message": "Przekroczono limit prób logowania."
}
```

---

## Endpoint 5: Wylogowanie

```
POST /api/v1/auth/logout
Authorization: Bearer <accessToken>
```

### Request

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### Responses

**HTTP 200 OK**
```json
{
  "status": "LOGGED_OUT",
  "message": "Użytkownik został wylogowany."
}
```

---

## Endpoint 6: Odświeżenie sesji

```
POST /api/v1/auth/refresh
```

### Request

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### Responses

**HTTP 200 OK**
```json
{
  "status": "TOKEN_REFRESHED",
  "session": {
    "accessToken": "new-jwt-access-token",
    "refreshToken": "new-jwt-refresh-token",
    "tokenType": "Bearer",
    "expiresInSeconds": 900,
    "refreshExpiresInSeconds": 28800
  }
}
```

**HTTP 401 Unauthorized — token wygasł**
```json
{
  "status": "AUTH_ERROR",
  "code": "INVALID_REFRESH_TOKEN",
  "message": "Sesja wygasła."
}
```

**HTTP 403 Forbidden — token odwołany**
```json
{
  "status": "AUTH_ERROR",
  "code": "REFRESH_TOKEN_REVOKED",
  "message": "Sesja została unieważniona."
}
```

---

## Endpoint 7: Sprawdzenie sesji (opcjonalne)

```
GET /api/v1/auth/session
Authorization: Bearer <accessToken>
```

### Responses

**HTTP 200 OK**
```json
{
  "status": "SESSION_ACTIVE",
  "user": {
    "id": "usr_8f4c3f8e",
    "username": "jan.kowalski",
    "email": "jan.kowalski@example.com",
    "accountStatus": "ACTIVE"
  }
}
```

---

## Kody domenowe — pełna lista

### AccountStatus

| Wartość | Znaczenie |
|---|---|
| `PENDING_VERIFICATION` | Konto czeka na weryfikację e-mail |
| `ACTIVE` | Konto aktywne i gotowe do użycia |
| `LOCKED` | Konto zablokowane (np. zbyt wiele prób logowania) |
| `DISABLED` | Konto wyłączone administracyjnie |

### AuthResponseStatus

| Wartość | Endpoint |
|---|---|
| `REGISTRATION_PENDING_VERIFICATION` | POST /register |
| `VERIFICATION_CODE_RESENT` | POST /resend-verification-code |
| `ACCOUNT_VERIFIED` | POST /verify-email-code |
| `AUTHENTICATED` | POST /login |
| `TOKEN_REFRESHED` | POST /refresh |
| `LOGGED_OUT` | POST /logout |
| `SESSION_ACTIVE` | GET /session |

---

## Elementy odroczone (wymagają potwierdzenia z backendem)

| Element | Status |
|---|---|
| TTL access tokenu (`expiresInSeconds`) | Przykładowa wartość 900s — do potwierdzenia |
| TTL refresh tokenu (`refreshExpiresInSeconds`) | Przykładowa wartość 28800s — do potwierdzenia |
| Dokładny format kodu weryfikacyjnego | Zakładamy 6 cyfr — do potwierdzenia |
| Limit resendów i cooldown | Do ustalenia z backendem |
| Polityka blokady konta (ile prób, na jak długo) | Do ustalenia z backendem |
| Zachowanie przy wielosesjowości (wiele urządzeń) | Backend decyduje |

