package com.cocode.popops.util;

import com.cocode.popops.core.PopOps;

/**
 * Checks if the TARGET activity (where PopOps.init was called) is foreground.
 * Messages will only be shown when the target activity is visible.
 */
public final class AppState {
    private AppState() {
    }

    /**
     * Returns true when the TARGET activity is foreground and available.
     * This is the activity where PopOps.init() was called.
     */
    public static boolean isForeground() {
        return PopOps.isTargetActivityForeground();
    }
}