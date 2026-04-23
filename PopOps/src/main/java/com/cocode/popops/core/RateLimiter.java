package com.cocode.popops.core;

import android.util.Log;

import com.cocode.popops.storage.PopupStateStore;

/**
 * Ensures the SDK respects minimum intervals between network requests
 * and UI displays to prevent resource abuse and user fatigue.
 */
public final class RateLimiter {
    private static final String TAG = "PopOps";

    // Minimum 10 seconds between server polls
    private static final long MIN_POLL_INTERVAL = 10_000L;

    // Minimum 30 seconds between showing any two different popups
    private static final long MIN_SHOW_INTERVAL = 30_000L;

    private RateLimiter() {}

    /**
     * Checks if enough time has passed to allow a new network request.
     */
    public static boolean canPoll() {
        long lastPoll = PopupStateStore.getLastPollTime();
        long now = System.currentTimeMillis();

        if (now - lastPoll < MIN_POLL_INTERVAL) {
            Log.d(TAG, "Rate Limit: Polling blocked. Please wait " +
                    ((MIN_POLL_INTERVAL - (now - lastPoll)) / 1000) + "s");
            return false;
        }
        return true;
    }

    /**
     * Checks if enough time has passed since the last message was shown.
     */
    public static boolean canShow() {
        long lastShow = PopupStateStore.getLastShowTime();
        long now = System.currentTimeMillis();

        if (now - lastShow < MIN_SHOW_INTERVAL) {
            Log.d(TAG, "Rate Limit: Display blocked. Cool-down in progress: " +
                    ((MIN_SHOW_INTERVAL - (now - lastShow)) / 1000) + "s");
            return false;
        }
        return true;
    }
}