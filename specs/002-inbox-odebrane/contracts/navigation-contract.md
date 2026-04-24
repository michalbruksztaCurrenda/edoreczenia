# Navigation Contract: 002-inbox-odebrane

**Data**: 2026-04-24 | **Feature**: `002-inbox-odebrane`

---

## Trasy nawigacyjne (Routes)

### Trasy zdefiniowane w tym feature'ze

| Trasa | Typ | Parametry | Ekran docelowy | Status |
|---|---|---|---|---|
| `"inbox"` | Destynacja stała | brak | `InboxScreen` | ✅ Zaimplementowana |
| `"inbox_detail/{messageId}"` | Destynacja z argumentem | `messageId: String` | `InboxDetailScreen` (placeholder) | ⏳ Placeholder — ekran docelowy out-of-scope |

---

## Wejście w ekran Inbox

### Źródło nawigacji

**Feature**: `001-user-auth`  
**Punkt emisji**: `LoginViewModel` → efekt `LoginEffect.NavigateToMain`  
**Akcja navController**:
```kotlin
navController.navigate("inbox") {
    popUpTo("login") { inclusive = true }
}
```
LoginScreen usuwany ze stosu — użytkownik nie może wrócić do logowania przyciskiem Back z ekranu Odebrane.

---

## Wyjście z ekranu Inbox

### Nawigacja do szczegółów (out-of-scope implementacja)

**Trigger**: `InboxAction.MessageClicked(messageId)` → `InboxEffect.NavigateToMessageDetail(messageId)`  
**Akcja navController**:
```kotlin
navController.navigate("inbox_detail/$messageId")
```

**Destynacja placeholder**:
```kotlin
composable(
    route = "inbox_detail/{messageId}",
    arguments = listOf(navArgument("messageId") { type = NavType.StringType })
) { backStackEntry ->
    val messageId = backStackEntry.arguments?.getString("messageId") ?: ""
    // Placeholder: Toast lub Text "Szczegóły wiadomości — wkrótce"
    InboxDetailPlaceholderScreen(messageId = messageId)
}
```

---

## Graf nawigacji aplikacji (AppNavGraph) — integracja

```kotlin
NavHost(navController = navController, startDestination = "auth_graph") {

    // Graf auth (001-user-auth)
    navigation(startDestination = "login", route = "auth_graph") {
        composable("login") { LoginScreen(...) }
        composable("registration") { RegistrationScreen(...) }
        composable("verify_email") { VerifyEmailScreen(...) }
    }

    // Ekran Inbox (002-inbox-odebrane)
    composable("inbox") {
        InboxScreen(
            navController = navController,
            viewModel = viewModel(factory = InboxViewModelFactory(...))
        )
    }

    // Placeholder szczegółów (002-inbox-odebrane — in-scope rejestracja)
    composable(
        route = "inbox_detail/{messageId}",
        arguments = listOf(navArgument("messageId") { type = NavType.StringType })
    ) { backStackEntry ->
        InboxDetailPlaceholderScreen(
            messageId = backStackEntry.arguments?.getString("messageId") ?: ""
        )
    }
}
```

---

## Dolna nawigacja (Bottom Navigation Bar)

Zakładki dolnej nawigacji w `InboxBottomNavBar` — stan aktywacji i docelowe trasy:

| Zakładka | Ikona | Trasa | Stan w tym feature'ze |
|---|---|---|---|
| Poczta | `mail` | `"inbox"` | ✅ Aktywna (`selected=true`), bieżący ekran |
| Do wysyłki | `outbox` | `"outbox"` (niezarejestrowana) | 🔘 Wizualna only, `onClick = {}` |
| ADE | `manage_search` | `"ade"` (niezarejestrowana) | 🔘 Wizualna only, `onClick = {}` |
| Ustawienia | `settings` | `"settings"` (niezarejestrowana) | 🔘 Wizualna only, `onClick = {}` |

---

## Back navigation

| Sytuacja | Zachowanie |
|---|---|
| Back z `InboxScreen` | Wyjście z aplikacji (LoginScreen usunięty ze stosu) |
| Back z `InboxDetailPlaceholderScreen` | Powrót do `InboxScreen` |

---

## Przyszłe rozszerzenia (out-of-scope)

| Trasa | Feature | Opis |
|---|---|---|
| `"inbox_detail/{messageId}"` | 003+ | Pełna implementacja ekranu szczegółów |
| `"outbox"` | Outbox feature | Ekran Do wysyłki |
| `"ade"` | ADE feature | Wyszukiwarka adresatów |
| `"settings"` | Settings feature | Ustawienia aplikacji |
| `"compose_message"` | ComposeMessage feature | Tworzenie nowej wiadomości (FAB) |

