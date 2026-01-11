package com.cocode.notifyx.core;

import android.util.Log;

import com.cocode.notifyx.api.ApiClient;
import com.cocode.notifyx.storage.SecureTokenStore;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Optimized Token Manager - Only requests new token when:
 * 1. No token exists (first time)
 * 2. Token is expired or about to expire (within 5 minutes)
 *
 * This reduces unnecessary API calls significantly.
 */
public final class TokenManager {
    private static final String TAG = "NotifyX";
    private static final Object LOCK = new Object();
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    // Token refresh margin: Refresh 5 minutes before actual expiry
    private static final long REFRESH_MARGIN_MS = 5 * 60 * 1000L; // 5 minutes

    // In-memory cache to avoid repeated storage reads
    private static String cachedToken = null;
    private static long cachedExpiry = 0L;

    // Track if refresh is in progress to avoid duplicate calls
    private static volatile boolean refreshInProgress = false;

    private static volatile boolean initialized = false;

    private TokenManager() {
    }

    public static void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        EXEC.submit(() -> {
            synchronized (LOCK) {
                // Load cached token from storage on initialization
                loadTokenFromStorage();

                // Only refresh if no valid token exists
                if (!hasValidToken()) {
                    Log.d(TAG, "No valid token found, requesting new token");
                    refreshToken();
                } else {
                    Log.d(TAG, "Valid token found in cache, skipping API call");
                }
            }
        });
    }

    /**
     * Load token from storage into memory cache.
     * This avoids repeated storage reads.
     */
    private static void loadTokenFromStorage() {
        try {
            cachedToken = SecureTokenStore.getToken();
            cachedExpiry = SecureTokenStore.getExpiry();

            if (cachedToken != null) {
                long remainingTime = cachedExpiry - System.currentTimeMillis();
                Log.d(TAG, "Token loaded from storage. Valid for: " + (remainingTime / 1000) + " seconds");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load token from storage", e);
            cachedToken = null;
            cachedExpiry = 0L;
        }
    }

    /**
     * Check if current token is valid.
     * Uses REFRESH_MARGIN to refresh before actual expiry.
     *
     * @return true if token exists and is valid (not expired or about to expire)
     */
    private static boolean hasValidToken() {
        if (cachedToken == null || cachedToken.isEmpty()) {
            Log.d(TAG, "No token in cache");
            return false;
        }

        long now = System.currentTimeMillis();
        long timeUntilExpiry = cachedExpiry - now;

        // Token is valid if it won't expire within the next 5 minutes
        boolean isValid = timeUntilExpiry > REFRESH_MARGIN_MS;

        if (!isValid) {
            if (cachedExpiry <= now) {
                Log.d(TAG, "Token expired " + ((now - cachedExpiry) / 1000) + " seconds ago");
            } else {
                Log.d(TAG, "Token expires soon (" + (timeUntilExpiry / 1000) + " seconds), will refresh");
            }
        }

        return isValid;
    }

    /**
     * Refresh token from API and save to storage + cache.
     * Prevents duplicate refresh calls if one is already in progress.
     */
    private static void refreshToken() {
        // Prevent duplicate refresh calls
        if (refreshInProgress) {
            Log.d(TAG, "Token refresh already in progress, skipping duplicate call");
            return;
        }

        refreshInProgress = true;

        try {
            Log.d(TAG, "Requesting new token from API...");
            JSONObject res = ApiClient.requestToken();

            String token = res.optString("token", null);
            long expiresAt = res.optLong("expiresAt", 0L);

            if (token != null && expiresAt > System.currentTimeMillis()) {
                // Update both storage and cache
                SecureTokenStore.saveToken(token, expiresAt);
                cachedToken = token;
                cachedExpiry = expiresAt;

                long validFor = (expiresAt - System.currentTimeMillis()) / 1000;
                Log.d(TAG, "Token refreshed successfully. Valid for: " + validFor + " seconds");
            } else {
                Log.w(TAG, "Invalid token response from API");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to refresh token: " + e.getMessage());
            // Token will be retried on next getToken() call
        } finally {
            refreshInProgress = false;
        }
    }

    /**
     * Get current token. Only refreshes if necessary.
     *
     * Flow:
     * 1. Check in-memory cache first (fast)
     * 2. If cache empty, load from storage
     * 3. If no valid token, refresh from API
     * 4. Return cached token
     *
     * @return Current valid token, or null if refresh failed
     */
    public static String getToken() {
        synchronized (LOCK) {
            // First check: Do we have a valid cached token?
            if (hasValidToken()) {
                // Fast path: Return cached token without any I/O
                return cachedToken;
            }

            // Second check: Maybe storage has a newer token?
            loadTokenFromStorage();

            if (hasValidToken()) {
                Log.d(TAG, "Found valid token in storage");
                return cachedToken;
            }

            // Third check: Need to refresh from API
            Log.d(TAG, "No valid token available, requesting new one");
            refreshToken();

            // Return whatever we have (might be null if refresh failed)
            return cachedToken;
        }
    }

    /**
     * Force token refresh (e.g., when API returns 401 Unauthorized).
     * Useful for handling token invalidation from server side.
     */
    public static void forceRefresh() {
        synchronized (LOCK) {
            Log.d(TAG, "Force refresh requested");
            cachedToken = null;
            cachedExpiry = 0L;
            refreshToken();
        }
    }

    /**
     * Check if a valid token is available without triggering refresh.
     * Useful for checking status without side effects.
     *
     * @return true if valid token exists
     */
    public static boolean isTokenAvailable() {
        synchronized (LOCK) {
            return hasValidToken();
        }
    }

    /**
     * Get time until token expires (in seconds).
     *
     * @return seconds until expiry, or -1 if no token or already expired
     */
    public static long getTimeUntilExpiry() {
        synchronized (LOCK) {
            if (cachedToken == null) return -1;

            long remaining = cachedExpiry - System.currentTimeMillis();
            return remaining > 0 ? remaining / 1000 : -1;
        }
    }

    /**
     * Clear cached token (e.g., on logout).
     */
    public static void clearToken() {
        synchronized (LOCK) {
            Log.d(TAG, "Clearing token cache");
            cachedToken = null;
            cachedExpiry = 0L;
            SecureTokenStore.saveToken(null, 0L);
        }
    }

    /**
     * Shutdown token manager and release resources.
     */
    public static void shutdown() {
        Log.d(TAG, "Shutting down TokenManager...");
        EXEC.shutdown();
        try {
            if (!EXEC.awaitTermination(5, TimeUnit.SECONDS)) {
                EXEC.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXEC.shutdownNow();
            Thread.currentThread().interrupt();
        }
        initialized = false;
        Log.d(TAG, "TokenManager shutdown complete");
    }
}