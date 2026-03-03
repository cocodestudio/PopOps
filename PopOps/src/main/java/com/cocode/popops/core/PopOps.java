package com.cocode.popops.core;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.cocode.popops.polling.MessagePoller;
import com.cocode.popops.storage.PopupStateStore;
import com.cocode.popops.storage.SecureFileStore;
import com.cocode.popops.topics.TopicManager;

import java.lang.ref.WeakReference;

/**
 * Public SDK entry. Call in your Activity's onCreate():
 * PopOps.init(this, "projectId");
 * Messages will ONLY show on this specific activity.
 */
public final class PopOps {
    private static final String TAG = "PopOps";
    private static volatile boolean initialized = false;
    private static WeakReference<Activity> targetActivity = new WeakReference<>(null);

    private PopOps() {
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

        Log.d(TAG, "Initializing PopOps SDK for activity: " + activity.getClass().getSimpleName());

        targetActivity = new WeakReference<>(activity);

        if (initialized) {
            Log.d(TAG, "PopOps SDK already initialized. Updating target activity.");
            return;
        }

        try {
            Context ctx = activity.getApplicationContext();

            PopOpsEnvironment.init(ctx, projectId);
            SecureFileStore.init(ctx);
            PopupStateStore.init(ctx);

            // Pass the literal Activity to lock the foreground state instantly
            ActivityTracker.register(activity);

            // Start listening for messages immediately
            MessagePoller.start();

            initialized = true;
            Log.d(TAG, "PopOps SDK initialization complete");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize PopOps SDK", e);
        }
    }

    public static Activity getTargetActivity() {
        Activity activity = targetActivity.get();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            return activity;
        }
        return null;
    }

    public static boolean isTargetActivityForeground() {
        Activity target = getTargetActivity();
        Activity current = ActivityTracker.getCurrentActivity();
        return target != null && target == current;
    }

    public static synchronized void shutdown() {
        if (!initialized) {
            Log.w(TAG, "Not initialized, nothing to shutdown");
            return;
        }

        Log.d(TAG, "Shutting down PopOps SDK...");

        MessagePoller.stop();
        targetActivity = new WeakReference<>(null);

        initialized = false;
        Log.d(TAG, "PopOps SDK shutdown complete");
    }

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