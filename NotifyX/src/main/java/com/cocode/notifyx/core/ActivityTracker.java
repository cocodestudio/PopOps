package com.cocode.notifyx.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Tracks current foreground activity using Application callbacks.
 */
public final class ActivityTracker implements Application.ActivityLifecycleCallbacks {
    private static final Object LOCK = new Object();
    private static WeakReference<Activity> current = new WeakReference<>(null);
    private static volatile boolean registered = false;

    private ActivityTracker() {
    }

    public static void register(Context ctx) {
        if (registered) return;
        try {
            Application app = (Application) ctx.getApplicationContext();
            app.registerActivityLifecycleCallbacks(new ActivityTracker());
            registered = true;
        } catch (Exception ignored) {
            // If cast fails (very unlikely), we won't register; UI operations will check Activity presence.
        }
    }

    public static Activity getCurrentActivity() {
        synchronized (LOCK) {
            Activity a = current.get();
            if (a != null && !a.isFinishing()) return a;
            return null;
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle bundle) {
        synchronized (LOCK) {
            current = new WeakReference<>(activity);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        synchronized (LOCK) {
            current = new WeakReference<>(activity);
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        synchronized (LOCK) {
            current = new WeakReference<>(activity);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle out) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        synchronized (LOCK) {
            Activity a = current.get();
            if (a == activity) current = new WeakReference<>(null);
        }
    }
}
