package com.cocode.popops.router;

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
                Renderer.render(m);
            }
        }
    }

    private static boolean shouldShow(Message m) {
        if (m == null || m.id == null) return false;

        // Topic filtering
        if (m.topic != null && !PopupStateStore.isSubscribed(m.topic)) return false;

        // Dedupe check for all messages
        if (PopupStateStore.isShown(m.id)) return false;

        // Type-specific checks
        if (m.type == MessageType.APP_UPDATE) {
            return VersionUtils.isUpdateRequired(PopOpsEnvironment.appVersion, m.newAppVersion);
        } else {
            if (m.targetVersion != null && !m.targetVersion.isEmpty() && !m.targetVersion.equalsIgnoreCase("All")) {
                return m.targetVersion.equals(PopOpsEnvironment.appVersion);
            }
        }

        return true;
    }
}