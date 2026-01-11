package com.cocode.notifyx.router;

import android.util.Log;

import com.cocode.notifyx.core.NotifyXEnvironment;
import com.cocode.notifyx.model.Message;
import com.cocode.notifyx.model.MessageType;
import com.cocode.notifyx.storage.PopupStateStore;
import com.cocode.notifyx.ui.Renderer;
import com.cocode.notifyx.util.MessageParser;
import com.cocode.notifyx.util.VersionUtils;

import org.json.JSONObject;

import java.util.List;

/**
 * Routes parsed messages to UI renderers based on rules.
 */
public final class MessageRouter {
    private static final String TAG = "NotifyX";
    private MessageRouter() {
    }

    public static void route(JSONObject response) {
        Log.d(TAG, "Routing messages");
        Log.d(TAG, response.toString());
        if (response == null) return;
        List<Message> messages = MessageParser.parseMessages(response);
        for (Message m : messages) {
            Log.d(TAG, m.toString());
            if (shouldShow(m)) {
                Log.d(TAG, "Should show");
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
            return VersionUtils.isUpdateRequired(NotifyXEnvironment.appVersion, m.newAppVersion);
        }

        // Version match for non-update messages
        if (m.targetVersion != null && !m.targetVersion.equals(NotifyXEnvironment.appVersion)) return false;

        return true;
    }
}
