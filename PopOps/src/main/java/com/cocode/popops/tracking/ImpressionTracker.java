package com.cocode.popops.tracking;

import android.util.Log;

import com.cocode.popops.api.ApiClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles impression tracking off the main thread so it never blocks UI rendering.
 */
public final class ImpressionTracker {
    private static final String TAG = "PopOps";
    // Lightweight single thread executor specifically for analytics
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    private ImpressionTracker() {
    }

    public static void track(String messageId) {
        if (messageId == null || messageId.isEmpty()) return;

        EXEC.execute(() -> {
            try {
                ApiClient.recordImpression(messageId);
                Log.d(TAG, "Impression successfully recorded for message: " + messageId);
            } catch (Exception e) {
                Log.w(TAG, "Failed to record impression: " + e.getMessage());
            }
        });
    }
}