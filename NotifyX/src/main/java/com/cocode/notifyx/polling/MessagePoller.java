package com.cocode.notifyx.polling;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.cocode.notifyx.api.ApiClient;
import com.cocode.notifyx.core.BackoffManager;
import com.cocode.notifyx.core.TokenManager;
import com.cocode.notifyx.router.MessageRouter;
import com.cocode.notifyx.util.AppState;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Optimized Message Poller with smart token management.
 * Only requests new token when needed, reducing API calls.
 */
public final class MessagePoller {
    private static final String TAG = "NotifyX";
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static Handler handler;
    private static HandlerThread handlerThread;
    private static volatile boolean running = false;

    private MessagePoller() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;

        handlerThread = new HandlerThread("notifyx-poller");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        // Schedule first run immediately
        handler.post(pollRunnable);
        Log.d(TAG, "Poller started");
    }

    private static long backoffBase() {
        return 30_000L;
    }

    private static final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            if (AppState.isForeground()) {
                EXEC.submit(() -> {
                    long nextDelay = BackoffManager.nextDelay();

                    try {
                        // OPTIMIZED: getToken() now uses cache, won't make API call unless needed
                        String token = TokenManager.getToken();

                        if (token == null) {
                            // No token available after refresh attempt
                            Log.w(TAG, "No token available, will retry on next poll");
                            BackoffManager.increase();
                            handler.postDelayed(pollRunnable, nextDelay);
                            return;
                        }

                        // Log token status (for debugging)
                        long timeUntilExpiry = TokenManager.getTimeUntilExpiry();
                        if (timeUntilExpiry > 0) {
                            Log.d(TAG, "Using cached token (expires in " + timeUntilExpiry + "s)");
                        }

                        // Fetch messages from API
                        JSONObject res = ApiClient.fetchMessages(token);

                        // Route the response (safe parsing inside)
                        MessageRouter.route(res);

                        // Success -> reset backoff and schedule next
                        BackoffManager.reset();
                        handler.postDelayed(pollRunnable, BackoffManager.nextDelay());

                    } catch (Exception e) {
                        String errorMsg = e.getMessage();

                        // OPTIMIZATION: Handle 401 Unauthorized (token invalid)
                        if (errorMsg != null && (errorMsg.contains("401") || errorMsg.contains("Unauthorized"))) {
                            Log.w(TAG, "Token rejected by server (401), forcing refresh");
                            TokenManager.forceRefresh();
                        } else {
                            Log.w(TAG, "Poll failed: " + errorMsg);
                        }

                        // Network or parsing failed -> increase backoff and reschedule
                        BackoffManager.increase();
                        handler.postDelayed(pollRunnable, nextDelay);
                    }
                });
            } else {
                // Not foreground: don't poll often. Reset and reschedule after base delay.
                BackoffManager.reset();
                handler.postDelayed(pollRunnable, backoffBase());
            }
        }
    };

    public static synchronized void stop() {
        if (!running) return;

        running = false;
        Log.d(TAG, "Stopping poller...");

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (handlerThread != null) {
            handlerThread.quitSafely();
            try {
                handlerThread.join(1000);
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while stopping handler thread");
            }
        }

        EXEC.shutdown();
        try {
            if (!EXEC.awaitTermination(5, TimeUnit.SECONDS)) {
                EXEC.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXEC.shutdownNow();
            Thread.currentThread().interrupt();
        }

        Log.d(TAG, "Poller stopped");
    }
}