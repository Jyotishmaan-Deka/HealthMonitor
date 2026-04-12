# Keep Room entity and DAO classes
-keep class com.healthmonitor.data.local.** { *; }

# Keep domain models (used by Firestore serialization)
-keep class com.healthmonitor.domain.model.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.healthmonitor.**$$serializer { *; }
-keepclassmembers class com.healthmonitor.** {
    *** Companion;
}
-keepclasseswithmembers class com.healthmonitor.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
