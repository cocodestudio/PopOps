# PopOps SDK - Consumer ProGuard Rules
# These rules are automatically applied to apps that use the PopOps SDK
# consumerProguardFiles 'consumer-rules.pro'

##---------------Begin: Public API ---------------

# Keep the public SDK API that app developers use
-keep public class com.cocode.popops.core.PopOps {
    public static void init(android.app.Activity, java.lang.String);
    public static void shutdown();
    public static void subscribeToTopic(java.lang.String);
    public static void unsubscribeFromTopic(java.lang.String);
    public static android.app.Activity getTargetActivity();
    public static boolean isTargetActivityForeground();
}

-keep public class com.cocode.popops.topics.TopicManager {
    public static void subscribe(java.lang.String);
    public static void unsubscribe(java.lang.String);
}

##---------------Begin: Model Classes ---------------

# Keep all model classes for JSON serialization/deserialization
-keep class com.cocode.popops.model.** { *; }

# Keep enum methods
-keepclassmembers enum com.cocode.popops.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

##---------------Begin: Android Components ---------------

# Keep Activity Lifecycle Callbacks
-keep class com.cocode.popops.core.ActivityTracker implements android.app.Application$ActivityLifecycleCallbacks { *; }

-dontwarn javax.crypto.**
-dontwarn java.security.**
-dontwarn android.security.keystore.**

##---------------Begin: JSON Processing ---------------

# Keep JSON classes
-keep class org.json.** { *; }
-dontwarn org.json.**

##---------------Begin: Resources ---------------

# Keep R classes for the SDK
-keep class com.cocode.popops.R
-keep class com.cocode.popops.R$* {
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
-keep class com.cocode.popops.ui.factory.RendererFactory { *; }
-keep class com.cocode.popops.ui.factory.PresentationRenderer { *; }
-keep class com.cocode.popops.ui.renderers.** { *; }

##---------------Begin: Threading ---------------

# Keep threading classes
-keep class java.util.concurrent.** { *; }
-dontwarn java.util.concurrent.**

##---------------Begin: AndroidX ---------------

# Keep AndroidX classes used by the SDK
-keep class androidx.** { *; }
-dontwarn androidx.**