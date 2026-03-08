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
 * High-level state: shown IDs, topics, and offline scheduled payloads stored securely.
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
                    saveRoot(root);
                }
            } catch (Exception ignored) {
            }
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
            } catch (Exception ignored) {
            }
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
            } catch (Exception ignored) {
            }
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

                // Save it keyed by the messageId to automatically overwrite/update edits
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