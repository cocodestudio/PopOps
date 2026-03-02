package com.cocode.popops.topics;

import com.cocode.popops.storage.PopupStateStore;

/**
 * Simple topic subscribe/unsubscribe manager.
 */
public final class TopicManager {
    private TopicManager() {
    }

    public static void subscribe(String topic) {
        if (topic == null || topic.isEmpty()) return;
        PopupStateStore.addTopic(topic);
        // optionally call backend to register subscription
    }

    public static void unsubscribe(String topic) {
        if (topic == null || topic.isEmpty()) return;
        PopupStateStore.removeTopic(topic);
    }
}
