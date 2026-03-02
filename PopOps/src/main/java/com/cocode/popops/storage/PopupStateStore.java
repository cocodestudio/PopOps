package com.cocode.popops.storage;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * High-level state: shown IDs + topics stored encrypted.
 */
public final class PopupStateStore {
    private static final Object LOCK = new Object();
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;
    private PopupStateStore() {
    }

    public static void init(Context context) {
        ctx = context;
        SecureFileStore.init(ctx); // same file used for both token & state; that's fine because each uses structured JSON keys
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

    public static boolean isShown(String id) {
        if (id == null) return false;
        return getShown().contains(id);
    }

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
}
