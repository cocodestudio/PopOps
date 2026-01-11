# NotifyX SDK - Consumer ProGuard Rules
# These rules are automatically applied to apps that use the NotifyX SDK
# Place this file in your SDK module and reference it in build.gradle:
# consumerProguardFiles 'consumer-proguard-rules.pro'

##---------------Begin: Public API ---------------

# Keep the public SDK API that app developers use
-keep public class com.cocode.notifyx.core.NotifyX {
    public static void init(android.app.Activity, java.lang.String);
    public static void shutdown();
    public static void subscribeToTopic(java.lang.String);
    public static void unsubscribeFromTopic(java.lang.String);
    public static android.app.Activity getTargetActivity();
    public static boolean isTargetActivityForeground();
}

-keep public class com.cocode.notifyx.topics.TopicManager {
    public static void subscribe(java.lang.String);
    public static void unsubscribe(java.lang.String);
}

##---------------Begin: Model Classes ---------------

# Keep all model classes for JSON serialization/deserialization
-keep class com.cocode.notifyx.model.** { *; }

# Keep enum methods
-keepclassmembers enum com.cocode.notifyx.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

##---------------Begin: Android Components ---------------

# Keep Activity Lifecycle Callbacks
-keep class com.cocode.notifyx.core.ActivityTracker implements android.app.Application$ActivityLifecycleCallbacks {
    <init>();
    public void onActivity*(...);
}

##---------------Begin: Security & Crypto ---------------

# Keep crypto and security classes
-keep class com.cocode.notifyx.storage.CryptoManager { *; }
-keep class android.security.keystore.** { *; }
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

-dontwarn javax.crypto.**
-dontwarn java.security.**
-dontwarn android.security.keystore.**

##---------------Begin: JSON Processing ---------------

# Keep JSON classes
-keep class org.json.** { *; }
-dontwarn org.json.**

##---------------Begin: Resources ---------------

# Keep R classes for the SDK
-keep class com.cocode.notifyx.R
-keep class com.cocode.notifyx.R$* {
    <fields>;
}

##---------------Begin: Annotations ---------------

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# For debugging
-keepattributes SourceFile,LineNumberTable

##---------------Begin: SDK Internal Classes ---------------

# Keep internal classes that use reflection or are instantiated dynamically
-keep class com.cocode.notifyx.ui.factory.RendererFactory { *; }
-keep class com.cocode.notifyx.ui.factory.PresentationRenderer { *; }
-keep class com.cocode.notifyx.ui.renderers.** { *; }

##---------------Begin: Threading ---------------

# Keep threading classes
-keep class java.util.concurrent.** { *; }
-dontwarn java.util.concurrent.**

##---------------Begin: AndroidX ---------------

# Keep AndroidX classes used by the SDK
-keep class androidx.core.content.ContextCompat { *; }
-keep class androidx.appcompat.app.AppCompatActivity { *; }

-dontwarn androidx.**

##---------------End: NotifyX Consumer Rules ---------------