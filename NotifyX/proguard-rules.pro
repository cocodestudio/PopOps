# NotifyX SDK - ProGuard Rules
# Add these rules to your SDK's proguard-rules.pro file

##---------------Begin: NotifyX SDK Core Rules ---------------

# Keep all public API classes and methods
-keep public class com.cocode.notifyx.core.NotifyX {
    public *;
}

-keep public class com.cocode.notifyx.topics.TopicManager {
    public *;
}

##---------------Begin: Model Classes ---------------

# Keep all model classes and their fields (used for JSON parsing)
-keep class com.cocode.notifyx.model.** { *; }
-keepclassmembers class com.cocode.notifyx.model.** {
    <fields>;
    <init>(...);
}

# Keep enum classes
-keepclassmembers enum com.cocode.notifyx.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

##---------------Begin: Storage & Crypto ---------------

# Keep crypto classes (Android Keystore)
-keep class com.cocode.notifyx.storage.CryptoManager { *; }
-keep class com.cocode.notifyx.storage.SecureTokenStore { *; }
-keep class com.cocode.notifyx.storage.SecureFileStore { *; }
-keep class com.cocode.notifyx.storage.PopupStateStore { *; }

# Keep Android Keystore classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class android.security.keystore.** { *; }

-dontwarn javax.crypto.**
-dontwarn java.security.**

##---------------Begin: Activity Tracking ---------------

# Keep Activity Lifecycle Callbacks
-keep class com.cocode.notifyx.core.ActivityTracker implements android.app.Application$ActivityLifecycleCallbacks {
    public *;
}

-keepclassmembers class com.cocode.notifyx.core.ActivityTracker {
    public void onActivity*(...);
}

##---------------Begin: UI Components ---------------

# Keep renderer classes
-keep class com.cocode.notifyx.ui.** { *; }
-keep class com.cocode.notifyx.ui.renderers.** { *; }
-keep class com.cocode.notifyx.ui.factory.** { *; }
-keep class com.cocode.notifyx.ui.layoutrenderers.** { *; }

# Keep PresentationRenderer interface
-keep interface com.cocode.notifyx.ui.factory.PresentationRenderer {
    public *;
}

##---------------Begin: Networking ---------------

# Keep API client classes
-keep class com.cocode.notifyx.api.** { *; }

# Keep networking classes
-keepclassmembers class com.cocode.notifyx.api.NetworkUtils {
    public static *;
}

##---------------Begin: JSON Parsing ---------------

# Keep org.json classes
-keep class org.json.** { *; }
-keepclassmembers class org.json.** {
    public *;
}

-dontwarn org.json.**

# Keep JSON constructors
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

##---------------Begin: Reflection & Serialization ---------------

# Keep classes that might be accessed via reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep source file names and line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

##---------------Begin: Threading & Concurrency ---------------

# Keep ExecutorService and Handler classes
-keep class java.util.concurrent.** { *; }
-keep class android.os.Handler { *; }
-keep class android.os.HandlerThread { *; }

-dontwarn java.util.concurrent.**

##---------------Begin: WeakReference ---------------

# Keep WeakReference (used for Activity tracking)
-keep class java.lang.ref.WeakReference { *; }

##---------------Begin: AndroidX & Support Libraries ---------------

# Keep AndroidX classes
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

-dontwarn androidx.**

# Keep AppCompat
-keep class androidx.appcompat.** { *; }
-keep interface androidx.appcompat.** { *; }

# Keep Core
-keep class androidx.core.** { *; }
-keep interface androidx.core.** { *; }

##---------------Begin: Android System Classes ---------------

# Keep Application class
-keep class android.app.Application { *; }
-keep class * extends android.app.Application

# Keep Activity classes
-keep class android.app.Activity { *; }
-keep class * extends android.app.Activity

# Keep Dialog classes
-keep class android.app.Dialog { *; }
-keep class * extends android.app.Dialog

# Keep View classes
-keep class android.view.View { *; }
-keep class * extends android.view.View

-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep onClick methods
-keepclassmembers class * extends android.view.View {
    public void set*(...);
    public ** get*();
}

##---------------Begin: R Classes ---------------

# Keep R classes (resources)
-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class com.cocode.notifyx.R$* {
    *;
}

##---------------Begin: Native Methods ---------------

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

##---------------Begin: Parcelable ---------------

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

##---------------Begin: Serializable ---------------

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

##---------------Begin: Remove Logging (Optional) ---------------

# Remove Log calls in release builds (Optional - uncomment if you want)
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }

##---------------Begin: Optimization Settings ---------------

# Optimization settings
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose

# Optimization filters
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

##---------------Begin: Warnings ---------------

# Ignore warnings for common issues
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.google.android.gms.**

##---------------End: NotifyX SDK Rules ---------------