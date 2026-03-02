# PopOps SDK - ProGuard Rules

##---------------Begin: PopOps SDK Core Rules ---------------

# Keep all public API classes and methods
-keep public class com.cocode.popops.core.PopOps {
    public *;
}

-keep public class com.cocode.popops.topics.TopicManager {
    public *;
}

##---------------Begin: Model Classes ---------------

# Keep all model classes and their fields (used for JSON parsing)
-keep class com.cocode.popops.model.** { *; }
-keepclassmembers class com.cocode.popops.model.** {
    <fields>;
    <init>(...);
}

# Keep enum classes
-keepclassmembers enum com.cocode.popops.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

##---------------Begin: Storage & Crypto ---------------

# Keep crypto classes (Android Keystore)
-keep class com.cocode.popops.storage.CryptoManager { *; }
-keep class com.cocode.popops.storage.SecureFileStore { *; }
-keep class com.cocode.popops.storage.PopupStateStore { *; }

# Keep Android Keystore classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class android.security.keystore.** { *; }

-dontwarn javax.crypto.**
-dontwarn java.security.**
-dontwarn android.security.keystore.**

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

##---------------End: PopOps SDK Rules ---------------