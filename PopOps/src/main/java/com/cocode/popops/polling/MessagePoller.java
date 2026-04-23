package com.cocode.popops.polling;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.cocode.popops.api.ApiClient;
import com.cocode.popops.core.BackoffManager;
import com.cocode.popops.core.RateLimiter;
import com.cocode.popops.router.MessageRouter;
import com.cocode.popops.storage.PopupStateStore;
import com.cocode.popops.util.AppState;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Optimized Message Poller.
 * Fetches messages directly, eliminating token generation latency entirely.
 * Survives configuration changes and implements request Rate Limiting.
 */
public final class MessagePoller {
    private static final String TAG = "PopOps";

    private static ExecutorService exec;
    private static Handler handler;
    private static HandlerThread handlerThread;
    private static volatile boolean running = false;

    private MessagePoller() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;

        if (exec == null || exec.isShutdown() || exec.isTerminated()) {
            exec = Executors.newSingleThreadExecutor();
        }

        // INSTANT OFFLINE CHECK: Review saved scheduled messages immediately on app open.
        if (exec != null && !exec.isShutdown()) {
            exec.execute(MessageRouter::checkScheduledMessages);
        }

        handlerThread = new HandlerThread("popops-poller");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        handler.post(pollRunnable);
        Log.d(TAG, "Poller started");
    }

    private static final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            // SECURITY: Rate limit check before hitting the network
            if (AppState.isForeground() && RateLimiter.canPoll()) {
                if (exec != null && !exec.isShutdown()) {
                    exec.execute(() -> {
                        try {
                            // Update last poll timestamp in storage
                            PopupStateStore.setLastPollTime(System.currentTimeMillis());

                            JSONObject response = ApiClient.fetchMessages();
                            MessageRouter.route(response);

                            BackoffManager.reset();
                            reschedule(10_000L);
                        } catch (Exception e) {
                            Log.w(TAG, "Poll failed: " + e.getMessage());

                            BackoffManager.increase();
                            reschedule(BackoffManager.nextDelay());
                        }
                    });
                }
            } else {
                // Not foreground or blocked by rate limiter: Check again in base delay
                BackoffManager.reset();
                reschedule(10_000L);
            }
        }
    };

    private static void reschedule(long delay) {
        if (running && handler != null) {
            handler.postDelayed(pollRunnable, delay);
        }
    }

    public static synchronized void stop() {
        if (!running) return;

        running = false;
        Log.d(TAG, "Stopping poller...");

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        if (handlerThread != null) {
            handlerThread.quitSafely();
            try {
                handlerThread.join(1000);
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while stopping handler thread");
            }
            handlerThread = null;
        }

        if (exec != null) {
            exec.shutdown();
            try {
                if (!exec.awaitTermination(5, TimeUnit.SECONDS)) {
                    exec.shutdownNow();
                }
            } catch (InterruptedException e) {
                exec.shutdownNow();
                Thread.currentThread().interrupt();
            }
            exec = null;
        }

        Log.d(TAG, "Poller stopped");
    }
}