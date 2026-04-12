# 🩺 Smart Health Monitor — Android App

A production-grade Android health monitoring app combining **BLE sensor integration**, **Firebase backend**, **Jetpack Compose UI**, **WorkManager background sync**, and **AI-powered health suggestions**.

---

## 📁 Project Structure

```
app/src/main/java/com/healthmonitor/
├── data/
│   ├── ble/
│   │   └── BleManager.kt              Mock + real BLE scanner/GATT client
│   ├── local/
│   │   ├── dao/HealthDao.kt           Room queries (readings, alerts, daily stats)
│   │   ├── database/HealthDatabase.kt Room DB definition
│   │   ├── entity/                    HealthReadingEntity, HealthAlertEntity
│   │   └── UserPreferencesRepository  DataStore (toggles + alert thresholds)
│   ├── remote/firebase/
│   │   └── FirebaseService.kt         Auth + Firestore batch upload
│   └── repository/
│       └── HealthRepositoryImpl.kt    Single source of truth; inline alert generation
│
├── domain/
│   ├── model/HealthModels.kt          HealthReading, DailyStats, HealthAlert, AISuggestion
│   ├── repository/HealthRepository.kt Interface (Clean Architecture boundary)
│   └── usecase/HealthUseCases.kt      ObserveLatestReading, ObserveReadings,
│                                      ObserveDailyStats, SaveReading, ObserveAlerts,
│                                      SyncHealthData, GetAISuggestions
│
├── presentation/
│   ├── navigation/NavGraph.kt         All routes + back-stack setup
│   ├── theme/Theme.kt                 Material3 colors, typography, dark mode
│   ├── ui/
│   │   ├── auth/                      AuthViewModel (reactive Firebase listener) + AuthScreen
│   │   ├── dashboard/                 DashboardViewModel + DashboardScreen (sparkline, alert banner)
│   │   ├── history/                   HistoryViewModel + HistoryScreen (tabs: stats + readings)
│   │   ├── alerts/                    AlertsViewModel + AlertsScreen (dismiss / clear)
│   │   ├── ai/                        AISuggestionsViewModel + AISuggestionsScreen
│   │   ├── ble/                       BleScannerViewModel + BleScannerScreen
│   │   ├── profile/                   ProfileViewModel (DataStore) + ProfileScreen (sliders)
│   │   └── MainScaffold.kt            Bottom nav with live badge count
│
├── di/                                AppModules, DatabaseModule, FirebaseModule,
│                                      RepositoryModule, WorkManagerModule, DataStoreModule
├── worker/HealthSyncWorker.kt         HiltWorker — periodic Firestore sync
├── notification/                      HealthNotificationManager + HealthFcmService
├── HealthApp.kt
└── MainActivity.kt

app/src/test/                          Unit tests (JVM)
│   ├── GetAISuggestionsUseCaseTest    10 tests covering AI suggestion logic
│   ├── HealthRepositoryImplTest       8 tests covering alert threshold logic
│   ├── DashboardViewModelTest         7 tests with Turbine Flow testing
│   └── BleManagerTest                 5 tests for mock data correctness

app/src/androidTest/                   Instrumented tests
│   ├── HealthDaoTest                  16 Room DAO tests (in-memory DB)
│   └── AuthScreenTest                 6 Compose UI tests
```

---

## ⚙️ Tech Stack

| Concern            | Library / API                                           |
|--------------------|---------------------------------------------------------|
| UI                 | Jetpack Compose + Material 3                            |
| Architecture       | MVVM + Clean Architecture (Domain / Data / Presentation)|
| Dependency injection | Hilt + KSP                                            |
| Async              | Kotlin Coroutines + Flow + StateFlow                    |
| Local storage      | Room DB (readings + alerts)                             |
| Preferences        | DataStore (notifications, thresholds, BLE mode)         |
| BLE                | Android BLE API (real GATT) + mock generator            |
| Auth               | Firebase Authentication (email/password)                |
| Cloud DB           | Firebase Firestore (batch upload, offline sync)         |
| Push notifications | Firebase Cloud Messaging                                |
| Background work    | WorkManager + HiltWorker (15-min periodic sync)         |
| Charts             | Canvas-drawn sparklines + bar charts (no library needed)|
| Testing            | JUnit 4, MockK, Turbine, Coroutines-Test, Room testing  |

---

## 🚀 Setup (5 steps)

### 1 — Clone
```bash
git clone https://github.com/yourname/HealthMonitor
cd HealthMonitor
```

### 2 — Firebase
1. Open [Firebase Console](https://console.firebase.google.com) → New project
2. Add Android app → package `com.healthmonitor`
3. Download `google-services.json` → replace `app/google-services.json`
4. Enable **Email / Password** authentication
5. Create a **Firestore** database (start in test mode)
6. Enable **Cloud Messaging**

### 3 — Android SDK
Update `local.properties` with your SDK path:
```
sdk.dir=/Users/yourname/Library/Android/sdk
```

### 4 — Build
```bash
./gradlew assembleDebug
```

### 5 — Run tests
```bash
./gradlew test                    # Unit tests (JVM)
./gradlew connectedAndroidTest    # Instrumented tests (device / emulator)
```

---

## 📡 BLE Integration

### Mock mode (default — no permissions required)
Generates a realistic data stream every 3 seconds:
- **Heart rate** — 60–100 bpm with slow sinusoidal drift ± noise
- **Steps** — increments during day hours (07:00–22:00)
- **SpO₂** — 97–99% with micro-dips

### Real BLE mode
Toggle via **Profile → Simulate BLE data** off, then tap **Find device** on Dashboard.

Targets standard BLE health profiles:
| Service                  | UUID     | Characteristic   | UUID     |
|--------------------------|----------|------------------|----------|
| Heart Rate               | `0x180D` | HR Measurement   | `0x2A37` |
| Pulse Oximeter           | `0x1822` | SpO₂             | `0x2A5F` |

Compatible with Polar H10, Garmin HRMs, Withings devices, and any standard BLE heart rate strap.

---

## 🚨 Alert System

Alerts are generated in `HealthRepositoryImpl` on every saved reading:

| Condition              | Threshold         | Type               |
|------------------------|-------------------|--------------------|
| Heart rate high        | > configured high | `HIGH_HEART_RATE`  |
| Heart rate critically high | > 140 bpm    | `HIGH_HEART_RATE`  |
| Heart rate low         | < configured low  | `LOW_HEART_RATE`   |
| SpO₂ low               | < configured low  | `LOW_OXYGEN`       |
| SpO₂ critical          | < 90%             | `CRITICAL_OXYGEN`  |

Thresholds are user-configurable via sliders in **Profile**. Each alert is:
- Persisted to Room DB (visible in the Alerts tab, dismissable individually)
- Shown as an Android notification (HIGH / MAX priority based on severity)
- Synced to Firestore on the next WorkManager cycle

---

## 🤖 AI Suggestions Logic (`GetAISuggestionsUseCase`)

Pure Kotlin — no network required. Analyses the last 50 readings + 7 days of daily stats:

| Signal                    | Trigger                  | Category     |
|---------------------------|--------------------------|--------------|
| Avg heart rate elevated   | > 100 bpm average        | STRESS       |
| Avg heart rate very low   | < 55 bpm average         | ACTIVITY     |
| SpO₂ between 90–94%       | Current reading          | BREATHING    |
| Steps very low            | < 2 000 steps today      | ACTIVITY     |
| Steps high                | > 10 000 steps today     | HYDRATION    |
| Late night elevated HR    | Hour ≥ 22 + avg HR > 80  | SLEEP        |
| Everything normal         | Fallback                 | ACTIVITY     |

To upgrade to a real LLM: replace `GetAISuggestionsUseCase.invoke()` body with an API call to Gemini / GPT-4, passing the readings as context.

---

## ☁️ Background Sync

`HealthSyncWorker` (HiltWorker) runs every **15 minutes** when:
- Network is **connected**
- Battery is **not low**

On each run:
1. Loads all `isSynced = false` readings from Room
2. Batch-uploads to Firestore under `users/{uid}/readings/{readingId}`
3. Marks rows as synced in Room
4. Shows a silent sync notification

Retries up to 3× with exponential backoff on network failure.

---

## 🗺️ Data Flow

```
BLE Device ──────────► BleManager.observeHealthData()
   (or mock)                    │ Flow<BleHealthData> every 3s
                                ▼
                       DashboardViewModel
                                │ SaveReadingUseCase
                                ▼
                    HealthRepositoryImpl
                      ├── dao.insertReading()     ──► Room DB (local-first)
                      └── checkAndCreateAlerts()  ──► dao.insertAlert()
                                                       └─► Notification
                                ▼ (every 15 min)
                       HealthSyncWorker
                                │ getUnsyncedReadings()
                                ▼
                          Firebase Firestore
                           (batch commit)

UI Flows:
  Room DB ──► observeReadings() / observeAlerts() / observeDailyStats()
           ──► ViewModels (StateFlow) ──► Compose screens (collectAsStateWithLifecycle)
```

---

## 🧪 Test Coverage Summary

| Test class                    | Tests | What it covers                              |
|-------------------------------|-------|---------------------------------------------|
| `GetAISuggestionsUseCaseTest` | 5     | Suggestion logic, priority ordering         |
| `HealthReadingAlertLevelTest` | 8     | AlertLevel computed property, edge cases    |
| `HealthRepositoryImplTest`    | 8     | Alert creation thresholds, sync edge cases  |
| `DashboardViewModelTest`      | 7     | StateFlow updates, BLE emissions, toggles   |
| `BleManagerTest`              | 5     | Mock data ranges, step monotonicity         |
| `HealthDaoTest`               | 16    | All Room CRUD operations (in-memory DB)     |
| `AuthScreenTest`              | 6     | Compose UI state rendering                  |
| **Total**                     | **55**|                                             |
