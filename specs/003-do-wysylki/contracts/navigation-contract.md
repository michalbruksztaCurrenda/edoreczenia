# Navigation Contract: 003-do-wysylki

**Phase 1 Output** | **Date**: 2026-04-25

---

## Destynacje nawigacyjne

| Route | Ekran | Plik |
|---|---|---|
| `outbox` | `OutboxScreen` | `feature/outbox/presentation/OutboxScreen.kt` |
| `outbox_detail/{itemId}` | `OutboxDetailPlaceholderScreen` | `feature/outbox/presentation/OutboxDetailPlaceholderScreen.kt` |

---

## Parametry nawigacyjne

### `outbox_detail/{itemId}`

| Parametr | Typ | Źródło | Opis |
|---|---|---|---|
| `itemId` | `String` | `OutboxItem.id` | Identyfikator klikniętej pozycji |

---

## Punkty wejścia do ekranu `outbox`

| Skąd | Jak | Warunek |
|---|---|---|
| `InboxScreen` → BottomNavBar zakładka „Do wysyłki" | `navController.navigate("outbox")` | Użytkownik jest zalogowany |

---

## Punkty wyjścia z ekranu `outbox`

| Akcja | Dokąd | Jak |
|---|---|---|
| BottomNavBar → zakładka „Poczta" | `InboxScreen` | `navController.navigate("inbox")` |
| Kliknięcie karty pozycji | `OutboxDetailPlaceholderScreen` | `navController.navigate("outbox_detail/$itemId")` |
| Systemowy Back | poprzedni ekran w back stacku | `popBackStack()` (domyślne) |

---

## Punkty wyjścia z ekranu `outbox_detail/{itemId}`

| Akcja | Dokąd | Jak |
|---|---|---|
| Przycisk cofnięcia (TopAppBar) | `OutboxScreen` | `navController.popBackStack()` |
| Systemowy Back | `OutboxScreen` | `popBackStack()` (domyślne) |

---

## Integracja z AppNavGraph

Zmiany wymagane w `AppNavGraph.kt`:

```
composable("outbox") {
    OutboxScreen(navController = navController)
}
composable(
    route = "outbox_detail/{itemId}",
    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
    OutboxDetailPlaceholderScreen(itemId = itemId, navController = navController)
}
```

---

## Zmiany w istniejącym kodzie (002-inbox-odebrane)

- `InboxBottomNavBar.kt`: handler kliknięcia zakładki „Do wysyłki" → `navController.navigate("outbox")`
- `OutboxBottomNavBar.kt`: handler kliknięcia zakładki „Poczta" → `navController.navigate("inbox")`

