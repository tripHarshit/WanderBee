# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ===========================================
# WANDERBEE PROGUARD RULES - PROTECT ALL IMPORTANT CODE
# ===========================================

# Keep source file and line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all classes in the main package
-keep class com.example.wanderbee.** { *; }
-keepclassmembers class com.example.wanderbee.** { *; }

# ===========================================
# FIREBASE PROTECTION
# ===========================================

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.firestore.core.** { *; }
-keep class com.google.firebase.firestore.model.** { *; }

# Firebase Cloud Messaging
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }

# ===========================================
# JETPACK COMPOSE PROTECTION
# ===========================================

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.animation.** { *; }

# Compose Navigation
-keep class androidx.navigation.** { *; }
-keep class androidx.navigation.compose.** { *; }

# ===========================================
# HILT DEPENDENCY INJECTION PROTECTION
# ===========================================

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.managers.FragmentComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.managers.ServiceComponentManager { *; }

# Keep all @Inject constructors
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep all @Provides methods
-keepclassmembers class * {
    @dagger.Provides *;
}

# ===========================================
# ROOM DATABASE PROTECTION
# ===========================================

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep all Room entities
-keep class com.example.wanderbee.data.local.entity.** { *; }

# Keep all Room DAOs
-keep interface com.example.wanderbee.data.local.dao.** { *; }

# ===========================================
# RETROFIT & NETWORKING PROTECTION
# ===========================================

# Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep all API service interfaces
-keep interface com.example.wanderbee.data.remote.apiService.** { *; }

# Keep all model classes
-keep class com.example.wanderbee.data.remote.models.** { *; }

# ===========================================
# REPOSITORY PROTECTION
# ===========================================

# Keep all repository classes
-keep class com.example.wanderbee.data.repository.** { *; }

# ===========================================
# VIEWMODEL PROTECTION
# ===========================================

# Keep all ViewModels
-keep class com.example.wanderbee.screens.**.ViewModel { *; }
-keep class com.example.wanderbee.screens.**.*ViewModel { *; }

# ===========================================
# CACHE PROTECTION
# ===========================================

# Keep all cache classes
-keep class com.example.wanderbee.data.cache.** { *; }

# ===========================================
# UTILS PROTECTION
# ===========================================

# Keep all utility classes
-keep class com.example.wanderbee.utils.** { *; }

# ===========================================
# NAVIGATION PROTECTION
# ===========================================

# Keep navigation classes
-keep class com.example.wanderbee.navigation.** { *; }

# ===========================================
# SERVICES PROTECTION
# ===========================================

# Keep all service classes
-keep class com.example.wanderbee.services.** { *; }

# ===========================================
# COIL IMAGE LOADING PROTECTION
# ===========================================

# Coil
-keep class coil.** { *; }
-keep interface coil.** { *; }

# ===========================================
# COROUTINES PROTECTION
# ===========================================

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-keep interface kotlinx.coroutines.** { *; }

# ===========================================
# DATASTORE PROTECTION
# ===========================================

# DataStore
-keep class androidx.datastore.** { *; }

# ===========================================
# LOCATION SERVICES PROTECTION
# ===========================================

# Google Play Services Location
-keep class com.google.android.gms.location.** { *; }

# ===========================================
# JSON SERIALIZATION PROTECTION
# ===========================================

# Gson (if used)
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ===========================================
# KOTLIN REFLECTION PROTECTION
# ===========================================

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# ===========================================
# ENUM PROTECTION
# ===========================================

# Keep all enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===========================================
# PARCELABLE PROTECTION
# ===========================================

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===========================================
# SERIALIZABLE PROTECTION
# ===========================================

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===========================================
# ANNOTATION PROTECTION
# ===========================================

# Keep all annotations
-keep @interface * { *; }
-keep class * {
    @* <fields>;
}
-keep class * {
    @* <methods>;
}

# ===========================================
# NATIVE METHODS PROTECTION
# ===========================================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===========================================
# GENERIC SIGNATURES PROTECTION
# ===========================================

# Keep generic signatures
-keepattributes Signature

# ===========================================
# EXCEPTION HANDLING PROTECTION
# ===========================================

# Keep exception classes
-keep public class * extends java.lang.Exception

# ===========================================
# LOGGING PROTECTION (OPTIONAL - REMOVE FOR PRODUCTION)
# ===========================================

# Keep logging for debugging (remove in final production)
-keep class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# ===========================================
# WEBVIEW PROTECTION (IF USED)
# ===========================================

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ===========================================
# CUSTOM RULES FOR SPECIFIC ISSUES
# ===========================================

# Keep any classes that might be dynamically loaded
-keep class * {
    @androidx.annotation.Keep *;
}

# Keep classes with specific patterns that might be important
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Application { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.content.ContentProvider { *; }
-keep class * extends android.view.View { *; }

# ===========================================
# FINAL NOTES
# ===========================================

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all classes in the main application package
-keep class com.example.wanderbee.WanderBeeApp { *; }
-keep class com.example.wanderbee.MainActivity { *; }

# Keep all screen classes
-keep class com.example.wanderbee.screens.** { *; }

# Keep all data classes
-keep class com.example.wanderbee.data.** { *; }

# Keep all UI components
-keep class com.example.wanderbee.ui.** { *; }

# Keep all navigation components
-keep class com.example.wanderbee.navigation.** { *; }

# Keep all dependency injection modules
-keep class com.example.wanderbee.di.** { *; }
