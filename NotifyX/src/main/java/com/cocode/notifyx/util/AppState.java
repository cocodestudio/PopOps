package com.cocode.notifyx.util;

import com.cocode.notifyx.core.NotifyX;

/**
 * Checks if the TARGET activity (where NotifyX.init was called) is foreground.
 * Messages will only be shown when the target activity is visible.
 */
public final class AppState {
    private AppState() {
    }

    /**
     * Returns true when the TARGET activity is foreground and available.
     * This is the activity where NotifyX.init() was called.
     */
    public static boolean isForeground() {
        return NotifyX.isTargetActivityForeground();
    }
}