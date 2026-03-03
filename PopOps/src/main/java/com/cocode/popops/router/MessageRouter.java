package com.cocode.popops.router;

import android.util.Log;

import com.cocode.popops.core.PopOpsEnvironment;
import com.cocode.popops.model.Message;
import com.cocode.popops.model.MessageType;
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

    public static void route(JSONObject response) {
        if (response == null) return;
        List<Message> messages = MessageParser.parseMessages(response);
        for (Message m : messages) {
            if (shouldShow(m)) {
                Log.d(TAG, "Routing message " + m.id + " to renderer.");
                Renderer.render(m);
            }
        }
    }

    private static boolean shouldShow(Message m) {
        if (m == null || m.id == null) {
            Log.w(TAG, "Filtered: Message or Message ID is null.");
            return false;
        }

        // 1. Topic Filtering
        // If topic is absent, empty, "all", or literally "null", it bypasses the filter and shows to everyone.
        if (m.topic != null) {
            String safeTopic = m.topic.trim();
            if (!safeTopic.isEmpty() &&
                    !safeTopic.equalsIgnoreCase("all") &&
                    !safeTopic.equalsIgnoreCase("null")) {

                // A specific topic was requested. Check if this device is subscribed.
                if (!PopupStateStore.isSubscribed(safeTopic)) {
                    Log.d(TAG, "Filtered: Device not subscribed to required topic -> '" + safeTopic + "'");
                    return false;
                }
            }
        }

        // 2. Target Version Filtering (For non-app-update messages)
        if (m.type != MessageType.APP_UPDATE) {
            if (m.targetVersion != null) {
                String safeVersion = m.targetVersion.trim();
                if (!safeVersion.isEmpty() &&
                        !safeVersion.equalsIgnoreCase("all") &&
                        !safeVersion.equalsIgnoreCase("null")) {

                    if (!safeVersion.equals(PopOpsEnvironment.appVersion)) {
                        Log.d(TAG, "Filtered: Version mismatch. Target: '" + safeVersion + "', Current: '" + PopOpsEnvironment.appVersion + "'");
                        return false;
                    }
                }
            }
        }

        // 3. App Update Filtering (For app-update messages)
        if (m.type == MessageType.APP_UPDATE) {
            if (!VersionUtils.isUpdateRequired(PopOpsEnvironment.appVersion, m.newAppVersion)) {
                Log.d(TAG, "Filtered: App update not required. Current: '" + PopOpsEnvironment.appVersion + "', New: '" + m.newAppVersion + "'");
                return false;
            }
        }

        // 4. Dedupe check to ensure a message is only shown once.
        // NOTE: This is the most common reason messages hide during testing!
        if (PopupStateStore.isShown(m.messageId)) {
            Log.d(TAG, "Filtered: Message " + m.messageId + " has already been shown previously on this device.");
            return false;
        }

        return true;
    }
}