# HealthMonitor

An Android app that connects to BLE health sensors and tracks your heart rate, steps, and blood oxygen. Built with Jetpack Compose and Firebase.

---

## What it does

Reads health data from a BLE device (or fake data if you don't have one), saves it locally, syncs to Firebase, and shows some basic health suggestions based on your readings.

---

## Setup

**Clone it**
```bash
git clone https://github.com/Jyotishmaan-Deka/HealthMonitor
cd HealthMonitor
```

**Firebase** (you need this to log in)
1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package `com.healthmonitor`
3. Download `google-services.json` and put it in the `app/` folder
4. Enable Email/Password auth and create a Firestore database

> If login gives you an "api key not valid" error, you're still on the placeholder file — replace it with yours. Or just tap **Continue as Guest** to try the app without any Firebase setup.

**Set your SDK path in `local.properties`**
```
sdk.dir=/Users/yourname/Library/Android/sdk
```

**Build**
```bash
./gradlew assembleDebug
```

---

## Testing without a real sensor

Mock mode is on by default. It generates fake heart rate, steps, and SpO2 data every 3 seconds so you can test everything. To use a real device, go to Profile → turn off "Simulate BLE data".

---

## Running tests

```bash
./gradlew test                  # unit tests
./gradlew connectedAndroidTest  # needs a device or emulator
```

---

## Tech used

Jetpack Compose, Hilt, Room, DataStore, Firebase Auth, Firestore, WorkManager, Coroutines/Flow, Android BLE API.