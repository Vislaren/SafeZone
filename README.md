# SafeZone — Android Security App

> V2.4.0-STABLE · Kotlin + Jetpack Compose · Foreground Service Architecture

A professional personal-safety app with voice-trigger detection, GPS dispatch, encrypted media vault, biometric auth, and background recording capabilities.

---

## Architecture Overview

```
com.safezone/
├── SafeZoneApplication.kt        # Hilt app, notification channels
├── MainActivity.kt               # Navigation host, bottom bar
├── SecurityService.kt            # Foreground service (mic + camera + GPS)
│
├── data/
│   ├── db/                       # Room entities + DAOs
│   │   ├── SafeZoneDatabase.kt
│   │   ├── Contact.kt / ContactDao.kt
│   │   ├── TriggerPhrase.kt
│   │   └── VaultFile.kt
│   ├── preferences/
│   │   └── SessionManager.kt     # DataStore (encrypted prefs)
│   └── repository/
│       └── Repositories.kt       # Contact / Vault / Security repos
│
├── di/
│   └── AppModule.kt              # Hilt DI module
│
├── receivers/
│   └── BootReceiver.kt           # Auto-restart on boot
│
├── ui/
│   ├── theme/Theme.kt            # Dark orange design system
│   └── screens/
│       ├── OnboardingScreen.kt   # Phone + OTP + Biometric auth
│       ├── DashboardScreen.kt    # System status + sensors
│       ├── VaultScreen.kt        # Encrypted media list
│       ├── SosScreen.kt          # SOS button + delete overlay
│       ├── SettingsScreen.kt     # Contacts + phrases + sliders
│       └── PermissionScreen.kt   # Permission request UI
│
├── utils/
│   ├── BiometricHelper.kt        # BiometricPrompt wrapper
│   └── PermissionUtils.kt        # Permission labels + rationale
│
└── viewmodel/
    └── MainViewModel.kt          # Single ViewModel for all screens
```

---

## Features

| Feature | Implementation |
|---|---|
| Voice Trigger (1–3 phrases) | `SpeechRecognizer` continuous loop in `SecurityService` |
| Background Audio (45 min) | `MediaRecorder` → internal private storage |
| SMS Dispatch (5 contacts) | `SmsManager` + `FusedLocationProviderClient` |
| SOS Call | `Intent.ACTION_CALL` to first contact |
| Silent Video (30 min) | CameraX flag set in `SecurityService` |
| G-force Detection | `SensorManager` accelerometer |
| GPS Loop (5 min / 45 min) | `Handler.postDelayed` chain |
| Biometric Gate | `BiometricPrompt` for deletion + settings |
| Encrypted Storage | Internal private dir + `DataStore` |
| Room DB | Contacts, trigger phrases, vault file records |
| Boot persistence | `BootReceiver` restarts service on device restart |

---

## Prerequisites

| Tool | Minimum Version | Download |
|---|---|---|
| Android Studio | Hedgehog (2023.1.1+) | [developer.android.com](https://developer.android.com/studio) |
| JDK | 17 | Bundled with Android Studio |
| Android SDK | API 26 (Oreo) minimum | Via SDK Manager |
| Android SDK Build Tools | 35.0.0 | Via SDK Manager |
| Physical Android device | API 26+ recommended | — |

> **VS Code works for editing but Android builds require Android Studio's Gradle toolchain or a standalone SDK installation.**

---

## Step-by-Step: Build & Test from VS Code

### Step 1 — Install Android Studio + SDK (One-time)

Even if you use VS Code as your editor, you need the Android SDK and build tools.

1. Download and install **Android Studio** from https://developer.android.com/studio
2. Open Android Studio → **More Actions → SDK Manager**
3. In **SDK Platforms** tab: check **Android 14 (API 35)** and **Android 8.0 (API 26)**
4. In **SDK Tools** tab: check:
   - Android SDK Build-Tools 35
   - Android Emulator
   - Android SDK Platform-Tools
5. Click **Apply → OK**

Note your SDK path (usually `~/Library/Android/sdk` on Mac, `C:\Users\<you>\AppData\Local\Android\Sdk` on Windows).

---

### Step 2 — Set ANDROID_HOME Environment Variable

**macOS/Linux** — add to `~/.zshrc` or `~/.bashrc`:
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk          # macOS
export ANDROID_HOME=$HOME/Android/Sdk                   # Linux
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
```
Then run: `source ~/.zshrc`

**Windows PowerShell:**
```powershell
$env:ANDROID_HOME = "C:\Users\<YourName>\AppData\Local\Android\Sdk"
$env:PATH += ";$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\tools"
```

Verify: `adb version` should print a version number.

---

### Step 3 — Install VS Code Extensions

Open VS Code and install:
1. **Extension Pack for Java** (Microsoft) — `ms-java`
2. **Kotlin** (fwcd) — syntax highlighting
3. **Android iOS Emulator** (optional, for emulator control)
4. **Gradle for Java** — Gradle task runner

---

### Step 4 — Open the Project

```bash
# Clone or copy the project folder, then:
cd SafeZone
code .
```

VS Code will detect the Gradle project. Accept any prompts to import it.

---

### Step 5 — Add Gradle Wrapper (if missing)

```bash
# Run inside the SafeZone directory:
gradle wrapper --gradle-version 8.7
```

Or download manually:
```bash
mkdir -p gradle/wrapper
curl -o gradle/wrapper/gradle-wrapper.jar \
  https://services.gradle.org/distributions/gradle-wrapper-8.7.jar
```

---

### Step 6 — Connect Your Android Device

1. On your Android phone: **Settings → Developer Options → USB Debugging → ON**
   - If no Developer Options: **Settings → About Phone → tap Build Number 7 times**
2. Connect via USB cable
3. Accept the "Allow USB Debugging" prompt on your phone
4. Verify connection:
   ```bash
   adb devices
   ```
   You should see your device listed (e.g., `emulator-5554 device` or `R3CT103ABCD device`).

---

### Step 7 — Build the APK

**From VS Code terminal:**
```bash
# Debug build (for testing)
./gradlew assembleDebug

# On Windows:
gradlew.bat assembleDebug
```

The APK will be output to:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

### Step 8 — Install and Run

```bash
# Install to connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or build + install in one command:
./gradlew installDebug
```

Then open the **SafeZone** app on your device.

---

### Step 9 — Grant Permissions at First Launch

The Permission screen will appear. Tap **GRANT PERMISSIONS** and allow:
- Camera
- Microphone
- Location (Precise)
- Send SMS
- Read Contacts
- Phone/Calls
- Notifications (Android 13+)

> Some permissions (SMS, Calls) may show system dialogs — allow all for full functionality.

---

### Step 10 — Authentication Flow

1. Enter your phone number on the **Enter Mobile ID** screen
2. Tap **INITIATE LINK** — you'll advance to OTP entry
   - In dev/test mode, entering any 6 digits will work (server validation is a stub)
3. Complete biometric enrollment
4. Tap **ACCESS DASHBOARD**

---

### Step 11 — Configure Contacts & Trigger Phrase

1. Navigate to **SETTINGS** tab
2. Add emergency contacts (up to 5 slots)
3. The default trigger phrase is `"Protocol Omega Delta"` — tap **RECORD VOICE PRINT** to set a new one
4. Adjust **KINETIC SENSORS** sliders to your environment

---

### Step 12 — Test Core Features

**Test voice trigger:**
```bash
# Watch logcat for trigger detection
adb logcat -s "SafeZone::Service"
```
Say your trigger phrase aloud — you should see:
```
Trigger phrase detected: 'Protocol Omega Delta'
EMERGENCY SEQUENCE INITIATED
```

**Test SOS dispatch:**
- Go to **SOS** tab → tap the red SOS button
- Check logcat for SMS dispatch confirmation

**Test accelerometer:**
- Shake the device firmly — watch for high G-force logs

---

### Step 13 — View Logs in Real Time

```bash
# All SafeZone logs:
adb logcat -s "SafeZone::Service" "SafeZone::Boot"

# Full app logs:
adb logcat | grep -i safezone
```

---

## Troubleshooting

| Issue | Fix |
|---|---|
| `INSTALL_FAILED_USER_RESTRICTED` | Enable USB debugging + allow installation from ADB in Developer Options |
| `Manifest merger failed` | Check that `compileSdk = 35` in `build.gradle.kts` |
| `SpeechRecognizer not available` | Requires Google app (Speech services) — install on device |
| `MediaRecorder: start failed` | RECORD_AUDIO permission not granted — recheck permissions |
| `SMS not sending` | SEND_SMS permission required; carrier may block programmatic SMS |
| Gradle build fails | Run `./gradlew --stop` then retry |
| `adb: device not found` | Reconnect USB, accept debug prompt, check `adb kill-server && adb start-server` |
| Room schema error | `fallbackToDestructiveMigration()` handles this; uninstall/reinstall app if needed |

---

## Dependency Version Matrix (Verified Compatible)

| Library | Version |
|---|---|
| AGP (Android Gradle Plugin) | 8.6.1 |
| Kotlin | 1.9.25 |
| Compose BOM | 2024.10.01 |
| Hilt | 2.51.1 |
| KSP | 1.9.25-1.0.20 |
| Room | 2.6.1 |
| CameraX | 1.3.4 |
| Play Services Location | 21.3.0 |
| DataStore | 1.1.1 |
| Biometric | 1.2.0-alpha05 |
| Coroutines | 1.8.1 |
| Accompanist Permissions | 0.36.0 |
| Gradle Wrapper | 8.7 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |

---

## Privacy & Security Notes

- All recordings are stored in **app-private internal storage** — inaccessible to other apps
- File records tracked in Room DB — deletion requires **biometric authentication**
- Session tokens stored in **DataStore Preferences** (not SharedPreferences)
- App disables cloud backup entirely via `data_extraction_rules.xml`

---

*SafeZone Heavy Industries — V2.4.0-STABLE*
