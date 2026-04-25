# Research: 003-do-wysylki — Ekran Do wysyłki (Outbox)

**Phase 0 Output** | **Date**: 2026-04-25

---

## Decyzje techniczne

### 1. Wzorzec FakeRepository — dlaczego MutableStateFlow

**Decision**: `FakeOutboxRepository` przechowuje dane jako `MutableStateFlow<List<OutboxItem>>` i udostępnia je przez `Flow<List<OutboxItem>>`.

**Rationale**: Spójność z `FakeInboxRepository` (002). `StateFlow` zapewnia, że ViewModel automatycznie otrzymuje aktualizacje stanu po operacji `refresh()`. Prosta podmiana na `OutboxRepositoryImpl` w przyszłości — ta sama sygnatura kontraktu.

**Alternatives considered**:
- `List` zwracany przez suspend function: brak reaktywności, konieczność ręcznego odświeżania w ViewModel.
- `LiveData`: zbędna zależność od frameworka Android w warstwie data.

---

### 2. Symulacja refresh — delay i brak mutacji danych

**Decision**: `refresh()` wykonuje `delay(1000..2000 ms)` i zwraca `Result.success(Unit)` bez modyfikacji `_items`. Dane pozostają niezmienione — użytkownik widzi „ponowne załadowanie" tych samych mockowych rekordów.

**Rationale**: Zgodne z clarify (2026-04-25): klik Synchronizuj / Odśwież → krótki loading → te same dane. `delay` z zakresu `1000..2000 ms` losowo symuluje opóźnienie sieciowe.

**Alternatives considered**:
- Brak delay: natychmiastowy reload nie oddaje prawdziwego zachowania sieciowego.
- Mutacja danych (np. zmiana timestamp): zbędna złożoność w wersji mockowej.

---

### 3. Czas ostatniej synchronizacji — gdzie przechowywany

**Decision**: `lastSyncTime` jako `String` ("HH:mm") przechowywany wyłącznie w `OutboxViewModel` jako `MutableStateFlow<String>`. Inicjalizowany bieżącym czasem przy starcie ViewModel. Aktualizowany po każdej akcji `Refresh` / `Synchronize`.

**Rationale**: Czas synchronizacji to stan prezentacyjny, nie domenowy. Nie należy do `OutboxItem` ani do repozytorium. Przy podłączeniu API — serwer zwróci timestamp w odpowiedzi, który zostanie zmapowany w warstwie `data` i przekazany do ViewModelu.

**Alternatives considered**:
- Pole w `OutboxUiState.Success.lastSyncTime`: zdecydowano. ViewModel oblicza wartość i umieszcza ją w stanie.
- Pole w `OutboxItem`: zbędne — czas dotyczy całej listy, nie pojedynczej pozycji.

---

### 4. Kolor lewej krawędzi — tokeny M3 vs. hardcoded kolory

**Decision**: Kolory krawędzi i badge'y mapowane przez `OutboxStatus` na tokeny `MaterialTheme.colorScheme.*`, nie na hardcoded hex.

**Rationale**: Konstytucja zasada VI (zakaz hardcodowania). Tokeny M3 zapewniają poprawność w trybie ciemnym/jasnym. Zgodność z paletą zdefiniowaną w `Color.kt` projektu.

| OutboxStatus | Token krawędzi | Token badge |
|---|---|---|
| `SEND_ERROR` | `MaterialTheme.colorScheme.error` | `MaterialTheme.colorScheme.errorContainer` |
| `PENDING_APPROVAL` | `MaterialTheme.colorScheme.tertiary` | `MaterialTheme.colorScheme.surfaceContainer` |
| `WAITING` | `MaterialTheme.colorScheme.surfaceVariant` | `MaterialTheme.colorScheme.surfaceContainerHigh` |

**Alternatives considered**:
- Hardcoded hex w composable: narusza zasadę VI Konstytucji.
- Dedykowane kolory poza M3: zbędna złożoność przy istniejącej palecie.

---

### 5. Pull-to-refresh — biblioteka

**Decision**: Użycie `androidx.compose.material3.pulltorefresh` (`PullToRefreshBox`) dostępnego w Material 3 Compose (Compose BOM 2026.03.00 zawiera stabilną wersję).

**Rationale**: Spójność z M3, brak dodatkowych zależności poza istniejącym BOM. API: `PullToRefreshBox(isRefreshing, onRefresh)` opakowuje `content`.

**Alternatives considered**:
- Accompanist SwipeRefresh: deprecated, zastąpiony przez M3 pull-to-refresh.
- Ręczna implementacja: zbędna złożoność.

---

### 6. ViewModelFactory — bez Hilt

**Decision**: `OutboxViewModelFactory` implementuje `ViewModelProvider.Factory`, tworzy `OutboxViewModel` z ręcznie wstrzykniętymi zależnościami.

**Rationale**: Spójność z podejściem z feature'u 002 (inbox). Hilt jest celowo wyłączony na tym etapie projektu — udokumentowany wyjątek tymczasowy.

**Alternatives considered**:
- Hilt `@HiltViewModel`: wykluczone decyzją projektową dla bieżącej fazy.
- Singleton repozytorium: możliwe, ale ryzyko wycieku stanu między testami.

---

### 7. Nawigacja — Navigation Compose

**Decision**: Nawigacja realizowana przez `Navigation Compose` (już w projekcie z feature 001/002). Destynacje dodane do istniejącego `AppNavGraph.kt`.

**Rationale**: Spójność z istniejącą architekturą. Brak potrzeby osobnego modułu nawigacyjnego dla tej iteracji.

**Alternatives considered**:
- Osobny NavGraph dla feature outbox: uzasadnione przy większej skali, przedwczesne w tej iteracji.

---

### 8. BottomNavigationBar — współdzielony czy duplikowany

**Decision**: `OutboxBottomNavBar` jako osobny composable w `feature/outbox/presentation/components/`, analogiczny do `InboxBottomNavBar` (002). Aktywna zakładka różna (`Do wysyłki`). Reużycie tych samych kluczy `strings.xml` dla etykiet.

**Rationale**: W pierwszej iteracji duplikacja jest akceptowalna — wspólny komponent BottomNav jest elementem ekranu głównego, który zostanie wydzielony przy refaktoryzacji nawigacji (osobny feature lub podczas implementacji `sent`/`settings`).

**Alternatives considered**:
- Wspólny `AppBottomNavBar` w `core/ui`: właściwy kierunek docelowy, ale wymaga refaktoryzacji istniejącego `InboxBottomNavBar` z feature 002 — poza zakresem tej iteracji.

