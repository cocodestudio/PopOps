package com.cocode.popops.router;

import android.util.Log;

import com.cocode.popops.core.PopOpsEnvironment;
import com.cocode.popops.core.RateLimiter;
import com.cocode.popops.model.Message;
import com.cocode.popops.model.MessageType;
import com.cocode.popops.model.UpdateMode;
import com.cocode.popops.storage.PopupStateStore;
import com.cocode.popops.ui.Renderer;
import com.cocode.popops.util.MessageParser;
import com.cocode.popops.util.VersionUtils;

import org.json.JSONObject;

import java.util.List;

/**
 * Routes parsed messages to UI renderers based on rules.
 */
public final class MessageRouter {
    private static final String TAG = "PopOps";

    private MessageRouter() {
    }

    public static void checkScheduledMessages() {
        try {
            List<JSONObject> scheduled = PopupStateStore.getScheduledMessages();
            for (JSONObject obj : scheduled) {
                Message m = MessageParser.parseSingle(obj);
                if (m != null && shouldShow(m)) {
                    Log.d(TAG, "Routing saved scheduled message " + m.messageId + " to renderer.");
                    Renderer.render(m);

                    // Exit loop! Only show ONE message per cycle to prevent UI stacking.
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to check scheduled messages", e);
        }
    }

    public static void route(JSONObject response) {
        if (response == null) return;
        List<Message> messages = MessageParser.parseMessages(response);
        for (Message m : messages) {
            if (shouldShow(m)) {
                Log.d(TAG, "Routing message " + m.messageId + " to renderer.");
                Renderer.render(m);

                // Exit loop! Only show ONE message per cycle to prevent UI stacking.
                return;
            }
        }
    }

    private static boolean shouldShow(Message m) {
        if (m == null || m.messageId == null) {
            Log.w(TAG, "Filtered: Message or Message ID is null.");
            return false;
        }

        boolean isImmediateUpdate = (m.type == MessageType.APP_UPDATE && m.updateMode == UpdateMode.IMMEDIATE);

        // 0. Display Rate Limit Check (UX Protection)
        // Skip display cool-down ONLY for Immediate Updates (because they are critical)
        if (!isImmediateUpdate && !RateLimiter.canShow()) {
            return false;
        }

        // 1. Scheduling Check
        if (m.startAt != null && m.endAt != null && m.startAt > 0 && m.endAt > 0) {
            long now = System.currentTimeMillis();

            if (now > m.endAt) {
                Log.d(TAG, "Filtered: Message expired. Dropping locally.");
                PopupStateStore.removeScheduledMessage(m.messageId);
                return false;
            } else if (now < m.startAt) {
                Log.d(TAG, "Filtered: Message scheduled for future. Saving to local storage to show later.");
                PopupStateStore.saveScheduledMessage(m.messageId, m.rawJson);
                return false;
            } else {
                PopupStateStore.removeScheduledMessage(m.messageId);
            }
        }

        // 2. Topic Filtering
        if (m.topic != null) {
            String safeTopic = m.topic.trim();
            if (!safeTopic.isEmpty() &&
                    !safeTopic.equalsIgnoreCase("all") &&
                    !safeTopic.equalsIgnoreCase("null")) {

                if (!PopupStateStore.isSubscribed(safeTopic)) {
                    Log.d(TAG, "Filtered: Device not subscribed to topic -> '" + safeTopic + "'");
                    return false;
                }
            }
        }

        // 3. Target Version Filtering (For non-app-update messages)
        if (m.type != MessageType.APP_UPDATE) {
            if (m.targetVersion != null) {
                String safeVersion = m.targetVersion.trim();
                if (!safeVersion.isEmpty() &&
                        !safeVersion.equalsIgnoreCase("all") &&
                        !safeVersion.equalsIgnoreCase("null")) {

                    if (!safeVersion.equals(PopOpsEnvironment.appVersion)) {
                        Log.d(TAG, "Filtered: Version mismatch.");
                        return false;
                    }
                }
            }
        }

        // 4. App Update Filtering
        if (m.type == MessageType.APP_UPDATE) {
            if (!VersionUtils.isUpdateRequired(PopOpsEnvironment.appVersion, m.newAppVersion)) {
                Log.d(TAG, "Filtered: App update not required.");
                return false;
            }
        }

        // 5. Dedupe Check
        if (!isImmediateUpdate) {
            if (PopupStateStore.isShown(m.messageId)) {
                Log.d(TAG, "Filtered: Message " + m.messageId + " has already been shown.");
                return false;
            }
        }

        return true;
    }
}