package com.cocode.notifyx.storage;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Stores token + expiry encrypted in a shared root structure.
 */
public final class SecureTokenStore {
    private static final Object LOCK = new Object();
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;

    private SecureTokenStore() {
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

    public static void saveToken(String token, long expiresAt) {
        if (token == null) return;
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                root.put("token", token);
                root.put("exp", expiresAt);
                saveRoot(root);
            } catch (Exception ignored) {
            }
        }
    }

    public static String getToken() {
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                return root.optString("token", null);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static long getExpiry() {
        synchronized (LOCK) {
            try {
                JSONObject root = loadRoot();
                return root.optLong("exp", 0L);
            } catch (Exception e) {
                return 0L;
            }
        }
    }
}