# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============== Hilt ==============
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ============== Room ==============
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============== Kotlin Serialization ==============
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ============== Coroutines ==============
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============== OSMDroid ==============
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# ============== Google Play Services ==============
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ============== DataStore ==============
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# ============== Compose ==============
-dontwarn androidx.compose.**

# ============== App Widget ==============
-keep class com.compose.geoquest.widget.** { *; }

# ============== Keep data classes ==============
-keep class com.compose.geoquest.data.model.** { *; }
-keep class com.compose.geoquest.data.local.** { *; }

# ============== Optimize aggressively ==============
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
