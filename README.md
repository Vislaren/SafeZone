# SafeZone — Tactical Safety Android App

A production-grade emergency-response Android app built with Jetpack Compose, Kotlin, MVVM + Clean Architecture, and a Supabase / Firebase backend.

**What it does**

- Manual hold-to-trigger SOS with live location + audio recording
- Voice security phrases that silently trigger SOS
- Real-time alerting of nearby SafeZone users within 50 m (PostGIS)
- Bluetooth LE offline fallback (broadcast + scan)
- Biometric login
- FCM push for incoming alerts
- SOS history + map of responders

## 1. Scope of this codebase

This is a **fully scaffolded project**. Every screen, ViewModel, repository, service, and DI module is written. What remains for you to do before shipping is:

- Fill in real Supabase / Firebase / Google Maps credentials.
- Wire the profile avatar picker and phrase recording UI to the actual file/audio APIs (the hooks are in place — `ProfileViewModel.uploadAvatar`, `ChunkedAudioRecorder`).
- Swap Android's on-device `SpeechRecognizer` in `ListeningService` for **Porcupine** or **Vosk** if you need reliable always-on wake-word detection. The current implementation works but is battery-heavy.
- Implement an FCM sender (Supabase Edge Function or Cloud Function) that listens on `alert_deliveries` inserts and pushes to the `recipient_id`'s FCM token stored on `profiles`.

## 2. Project structure

```
safezone/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/safezone/app/
│       │   ├── SafeZoneApp.kt            # @HiltAndroidApp + notification channels
│       │   ├── MainActivity.kt           # Compose host + deep-link handler
│       │   ├── data/
│       │   │   ├── local/                # Room DB, DataStore, EncryptedSharedPreferences
│       │   │   ├── remote/supabase/      # Supabase client + schema constants
│       │   │   └── repository/           # Repository implementations
│       │   ├── domain/
│       │   │   ├── models/               # UserProfile, SosEvent, AppResult, ...
│       │   │   ├── repository/           # Repository interfaces
│       │   │   └── usecases/             # SignIn, TriggerSos, EndSos, ...
│       │   ├── di/                       # Hilt modules (AppModule, RepositoryModule)
│       │   ├── services/
│       │   │   ├── SOSService.kt         # Foreground: chunked audio + location + events
│       │   │   ├── ListeningService.kt   # Foreground: wake-word detection
│       │   │   ├── BLEService.kt         # Foreground: BLE advertise/scan
│       │   │   ├── SafeZoneMessagingService.kt   # FCM receiver
│       │   │   └── BootReceiver.kt
│       │   ├── ui/
│       │   │   ├── navigation/           # Route.kt, SafeZoneNavGraph.kt
│       │   │   ├── theme/                # Color/Type/Theme
│       │   │   ├── components/           # Buttons, Fields, Cards, BottomBar, TopBar, ShieldLogo
│       │   │   ├── viewmodels/           # HiltViewModels for each screen
│       │   │   └── screens/              # splash, auth, profile, home, map, sos, alert, history, settings
│       │   └── utils/                    # BiometricGate, PermissionSets, PhraseMatcher, Formatters, ChunkedAudioRecorder
│       └── res/                          # strings, colors, themes, manifest xml
├── gradle/libs.versions.toml             # Version catalog (one source of truth)
├── supabase/schema.sql                   # Full DB + RLS + RPCs + buckets
├── local.properties.template             # Copy to local.properties and fill in
└── README.md
```

## 3. Prerequisites

- Android Studio **Ladybug** (2024.2) or newer
- JDK **17**
- Android SDK with API **35**
- Min device: Android **12** (API 31). The app uses the API-31+ BLE permission model.
- A device with a fingerprint sensor (for the biometric login flow)

## 4. Setup

### 4.1 Clone + open

```bash
git clone <your-fork>
cd safezone
cp local.properties.template local.properties
```

### 4.2 Supabase

1. Create a project at <https://supabase.com>.
2. In **SQL Editor → New query**, paste the contents of `supabase/schema.sql` and run it. This creates all tables, RPCs, storage buckets, and RLS policies.
3. Go to **Settings → API** and copy:
   - `Project URL` → paste into `local.properties` as `SUPABASE_URL`
   - `anon public` key → `SUPABASE_ANON_KEY`
4. In **Authentication → Providers**, enable **Email** (disable "Confirm email" during development if you don't want to wire the email flow yet).
5. In **Storage**, confirm the four buckets were created: `avatars`, `sos-audio`, `sos-images`, `voice-phrases`. The script creates them; if your project pre-dates that schema you may need to create them from the UI.
6. In **Database → Replication**, confirm `alert_deliveries` is published on `supabase_realtime`.

### 4.3 Firebase

1. Create a Firebase project at <https://console.firebase.google.com>.
2. Add an Android app with package name `com.safezone.app`.
3. Download `google-services.json` and place it at `app/google-services.json`.
4. Enable **Cloud Messaging** in the project.
5. **Bridge Supabase → FCM.** The scaffold assumes a Supabase Edge Function or Cloud Function that:
   - triggers on `insert` into `alert_deliveries`,
   - reads the `recipient_id`'s `fcm_token` from `profiles`,
   - sends an FCM data message with keys `type=sos_alert`, `event_id`, `user_name`, `distance_meters`.
   Example edge function template to write yourself:

   ```ts
   // supabase/functions/notify-fcm/index.ts  (Deno edge function)
   import { createClient } from "jsr:@supabase/supabase-js";
   // ... subscribe to alert_deliveries via webhook, fetch token, call FCM v1 API.
   ```

### 4.4 Google Maps

1. At <https://console.cloud.google.com> create / select a project.
2. Enable the **Maps SDK for Android**.
3. Create an API key, restrict it to your debug + release SHA-1s and to package `com.safezone.app`.
4. Put the key in `local.properties` as `MAPS_API_KEY`.

### 4.5 Build

```bash
./gradlew assembleDebug
```

or open in Android Studio → Run `app`.

## 5. Runtime permission flow

On first launch the app asks for location (foreground), microphone, and notifications. Background location and Bluetooth are requested lazily when the relevant feature is enabled in settings. The helper `com.safezone.app.utils.PermissionSets` centralises these.

## 6. Known limitations

- **Always-on keyword detection** uses `SpeechRecognizer.createOnDeviceSpeechRecognizer`. It works but will drain battery faster than a proper wake-word engine. Swap in Porcupine or Vosk for production.
- **BLE peripheral mode** isn't supported on every Android device. `BLEService` falls back to scan-only on unsupported hardware.
- **Nearby radius** is fixed at 50 m in the RPC call site (`SosRepositoryImpl.createEvent`) — widen if you want broader fan-out.
- **Audio chunk upload** is sequential at 15 s chunks. For very poor networks, consider moving uploads onto a `WorkManager` retry queue (the `PendingAudioDao` is already there as a hook).

## 7. Contributing

Everything is wired through `HiltViewModel` + repository interfaces, so swapping any implementation (Supabase → custom backend, SpeechRecognizer → Porcupine) is a one-module change.

## 8. License

This scaffold is provided as-is. Review all security and permission flows against your own threat model before shipping to real users.
