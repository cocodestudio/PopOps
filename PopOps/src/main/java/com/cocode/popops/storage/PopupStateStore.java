package com.cocode.popops.storage;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * High-level state: rate limits, shown IDs, topics, and offline scheduled payloads.
 */
public final class PopupStateStore {
    private static final Object LOCK = new Object();
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;

    private PopupStateStore() {
    }

    public static void init(Context context) {
        ctx = context;
        SecureFileStore.init(ctx);
    }

    private static JSONObject loadRoot() throws Exception {
        byte[] enc = SecureFileStore.read();
        if (enc == null) return new JSONObject();
        byte[] dec = CryptoManager.decrypt(enc);
        return new JSONObject(new String(dec, StandardCharsets.UTF_8));
    }

    private static void saveRoot(JSONObject root) throws Exception {
        byte[] enc = CryptoManager.encrypt(root.toString().getBytes(StandardCharsets.UTF_8));
        SecureFileStore.write(enc);
    }

    // --- Rate Limiting Timestamps ---
    public static long getLastPollTime() {
        synchronized (LOCK) {
            try { return loadRoot().optLong("last_poll", 0L); } catch (Exception e) { return 0L; }
        }
    }

    public static void setLastPollTime(long timestamp) {
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                root.put("last_poll", timestamp);
                saveRoot(root);
            } catch (Exception ignored) {}
        }
    }

    public static long getLastShowTime() {
        synchronized (LOCK) {
            try { return loadRoot().optLong("last_show", 0L); } catch (Exception e) { return 0L; }
        }
    }

    public static void setLastShowTime(long timestamp) {
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                root.put("last_show", timestamp);
                saveRoot(root);
            } catch (Exception ignored) {}
        }
    }

    // --- Shown Deduplication Configs ---
    public static Set<String> getShown() {
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                JSONArray arr = root.optJSONArray("shown");
                Set<String> set = new HashSet<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                }
                return set;
            } catch (Exception e) {
                return new HashSet<>();
            }
        }
    }

    public static boolean isShown(String id) {
        if (id == null) return false;
        return getShown().contains(id);
    }

    public static void markShown(String id) {
        if (id == null) return;
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                JSONArray arr = root.optJSONArray("shown");
                Set<String> set = new HashSet<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                }
                if (!set.contains(id)) {
                    set.add(id);
                    JSONArray out = new JSONArray();
                    for (String s : set) out.put(s);
                    root.put("shown", out);

                    // Automatically update display rate limit whenever a message is successfully marked as shown
                    root.put("last_show", System.currentTimeMillis());

                    saveRoot(root);
                }
            } catch (Exception ignored) {}
        }
    }

    // --- Topic Subscription Configs ---
    public static Set<String> getTopics() {
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                JSONArray arr = root.optJSONArray("topics");
                Set<String> set = new HashSet<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                }
                return set;
            } catch (Exception e) {
                return new HashSet<>();
            }
        }
    }

    public static void addTopic(String topic) {
        if (topic == null) return;
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                JSONArray arr = root.optJSONArray("topics");
                Set<String> set = new HashSet<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                }
                if (!set.contains(topic)) {
                    set.add(topic);
                    JSONArray out = new JSONArray();
                    for (String t : set) out.put(t);
                    root.put("topics", out);
                    saveRoot(root);
                }
            } catch (Exception ignored) {}
        }
    }

    public static void removeTopic(String topic) {
        if (topic == null) return;
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                JSONArray arr = root.optJSONArray("topics");
                Set<String> set = new HashSet<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                }
                if (set.remove(topic)) {
                    JSONArray out = new JSONArray();
                    for (String t : set) out.put(t);
                    root.put("topics", out);
                    saveRoot(root);
                }
            } catch (Exception ignored) {}
        }
    }

    public static boolean isSubscribed(String topic) {
        if (topic == null) return false;
        return getTopics().contains(topic);
    }

    // --- Offline Scheduling Cache Commands ---
    public static void saveScheduledMessage(String messageId, JSONObject msgJson) {
        if (messageId == null || msgJson == null) return;
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                JSONObject scheduled = root.optJSONObject("scheduled");
                if (scheduled == null) scheduled = new JSONObject();

                scheduled.put(messageId, msgJson);
                root.put("scheduled", scheduled);
                saveRoot(root);
            } catch (Exception ignored) {}
        }
    }

    public static void removeScheduledMessage(String messageId) {
        if (messageId == null) return;
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                JSONObject scheduled = root.optJSONObject("scheduled");
                if (scheduled != null && scheduled.has(messageId)) {
                    scheduled.remove(messageId);
                    root.put("scheduled", scheduled);
                    saveRoot(root);
                }
            } catch (Exception ignored) {}
        }
    }

    public static List<JSONObject> getScheduledMessages() {
        List<JSONObject> list = new ArrayList<>();
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                JSONObject scheduled = root.optJSONObject("scheduled");
                if (scheduled != null) {
                    Iterator<String> keys = scheduled.keys();
                    while (keys.hasNext()) {
                        list.add(scheduled.optJSONObject(keys.next()));
                    }
                }
            } catch (Exception ignored) {}
        }
        return list;
    }
}