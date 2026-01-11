package com.cocode.notifyx.core;

import android.util.Log;

import com.cocode.notifyx.api.ApiClient;
import com.cocode.notifyx.storage.SecureTokenStore;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Token lifecycle manager. Ensures there is a valid token and refreshes it when necessary.
 */
public final class TokenManager {
    private static final String TAG = "NotifyX";
    private static final Object LOCK = new Object();
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static volatile boolean initialized = false;
    private TokenManager() {
    }

    public static void ensureInitialized() {
        if (initialized) return;
        initialized = true;
        EXEC.submit(() -> {
            synchronized (LOCK) {
                if (!hasValidToken()) refreshToken();
                Log.d(TAG, "Token Manager Initialized Successfully");
            }
        });
    }

    private static boolean hasValidToken() {
        String token = SecureTokenStore.getToken();
        long exp = SecureTokenStore.getExpiry();
        return token != null && exp > System.currentTimeMillis();
    }

    private static void refreshToken() {
        try {
            JSONObject res = ApiClient.requestToken();
            Log.d(TAG, res.toString());
            String token = res.optString("token", null);
            long expiresAt = res.optLong("expiresAt", 0L);
            Log.d(TAG, String.valueOf(expiresAt - System.currentTimeMillis()));
            if (token != null && expiresAt > System.currentTimeMillis()) {
                Log.d(TAG, "Saving Token");
                SecureTokenStore.saveToken(token, expiresAt);
            }
            Log.d(TAG, "Token fetch from api : " + token);
        } catch (Exception ignored) {
            // will retry on next getToken() or scheduled poll
        }
    }

    public static String getToken() {
        synchronized (LOCK) {
            if (!hasValidToken()) {
                refreshToken(); // attempt synchronous refresh (light)
            }
            return SecureTokenStore.getToken();
        }
    }

    public static void shutdown() {
        EXEC.shutdown();
        try {
            if (!EXEC.awaitTermination(5, TimeUnit.SECONDS)) {
                EXEC.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXEC.shutdownNow();
        }
    }
}
