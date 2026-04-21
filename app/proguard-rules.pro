# Keep Supabase serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.safezone.app.**$$serializer { *; }
-keepclassmembers class com.safezone.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.safezone.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keep class com.safezone.app.domain.models.** { *; }
-keep class com.safezone.app.data.remote.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
