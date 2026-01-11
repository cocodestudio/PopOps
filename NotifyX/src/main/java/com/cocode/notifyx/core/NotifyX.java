package com.cocode.notifyx.core;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.cocode.notifyx.polling.MessagePoller;
import com.cocode.notifyx.storage.PopupStateStore;
import com.cocode.notifyx.storage.SecureFileStore;
import com.cocode.notifyx.storage.SecureTokenStore;
import com.cocode.notifyx.topics.TopicManager;

import java.lang.ref.WeakReference;

/**
 * Public SDK entry. Call in your Activity's onCreate():
 * NotifyX.init(this, "projectId");
 * Messages will ONLY show on this specific activity.
 */
public final class NotifyX {
    private static final String TAG = "NotifyX";
    private static volatile boolean initialized = false;
    private static WeakReference<Activity> targetActivity = new WeakReference<>(null);

    private NotifyX() {
    }

    /**
     * Initialize SDK with a specific activity.
     * Messages will ONLY show on this activity.
     *
     * @param activity The activity where messages should appear
     * @param projectId Your project ID
     */
    public static synchronized void init(Activity activity, String projectId) {
        if (activity == null) throw new IllegalArgumentException("activity required");
        if (projectId == null || projectId.isEmpty())
            throw new IllegalArgumentException("projectId required");

        Log.d(TAG, "Initializing NotifyX SDK for activity: " + activity.getClass().getSimpleName());

        // Store the target activity
        targetActivity = new WeakReference<>(activity);

        if (!initialized) {
            // Initialize global environment (only once)
            NotifyXEnvironment.init(activity.getApplicationContext(), projectId);

            // Initialize secure stores and trackers
            SecureFileStore.init(NotifyXEnvironment.context);
            SecureTokenStore.init(NotifyXEnvironment.context);
            PopupStateStore.init(NotifyXEnvironment.context);

            // Register activity tracker for lifecycle
            ActivityTracker.register(NotifyXEnvironment.context);

            // Ensure token manager started
            TokenManager.ensureInitialized();

            // Start polling
            MessagePoller.start();

            initialized = true;
        }

        Log.d(TAG, "NotifyX SDK initialized successfully");
    }

    /**
     * Get the target activity where messages should be shown.
     * Returns null if activity is destroyed or not set.
     */
    public static Activity getTargetActivity() {
        Activity activity = targetActivity.get();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            return activity;
        }
        return null;
    }

    /**
     * Check if the current foreground activity is the target activity
     */
    public static boolean isTargetActivityForeground() {
        Activity target = getTargetActivity();
        Activity current = ActivityTracker.getCurrentActivity();
        return target != null && target == current;
    }

    /**
     * Properly shutdown SDK and release resources.
     * Call this in your activity's onDestroy() if needed.
     */
    public static synchronized void shutdown() {
        if (!initialized) {
            Log.w(TAG, "Not initialized, nothing to shutdown");
            return;
        }

        Log.d(TAG, "Shutting down NotifyX SDK...");

        MessagePoller.stop();
        TokenManager.shutdown();
        targetActivity = new WeakReference<>(null);

        initialized = false;
        Log.d(TAG, "NotifyX SDK shutdown complete");
    }

    // Topic convenience methods
    public static void subscribeToTopic(String topic) {
        if (!initialized) {
            Log.w(TAG, "SDK not initialized");
            return;
        }
        TopicManager.subscribe(topic);
    }

    public static void unsubscribeFromTopic(String topic) {
        if (!initialized) {
            Log.w(TAG, "SDK not initialized");
            return;
        }
        TopicManager.unsubscribe(topic);
    }
}