# SafeZone ProGuard Rules

# ─── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ─── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ─── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ─── Security Crypto ──────────────────────────────────────────────────────────
-keep class androidx.security.crypto.** { *; }

# ─── Google Play Services ─────────────────────────────────────────────────────
-keep class com.google.android.gms.** { *; }

# ─── CameraX ──────────────────────────────────────────────────────────────────
-keep class androidx.camera.** { *; }

# ─── SafeZone Models ─────────────────────────────────────────────────────
-keep class com.safezone.data.** { *; }
-keepclassmembers class com.safezone.** { *; }

# ─── Compose ──────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
