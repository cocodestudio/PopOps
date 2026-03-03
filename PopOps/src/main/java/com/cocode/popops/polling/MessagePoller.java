package com.cocode.popops.polling;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.cocode.popops.api.ApiClient;
import com.cocode.popops.core.BackoffManager;
import com.cocode.popops.router.MessageRouter;
import com.cocode.popops.util.AppState;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Optimized Message Poller.
 * Fetches messages directly, eliminating token generation latency entirely.
 */
public final class MessagePoller {
    private static final String TAG = "PopOps";
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static Handler handler;
    private static HandlerThread handlerThread;
    private static volatile boolean running = false;

    private MessagePoller() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;

        handlerThread = new HandlerThread("popops-poller");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        // Schedule first run immediately
        handler.post(pollRunnable);
        Log.d(TAG, "Poller started");
    }

    private static long backoffBase() {
        return 10_000L; // Lowered to 10 seconds for highly reactive dashboard testing
    }

    private static final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            if (AppState.isForeground()) {
                EXEC.execute(() -> {
                    try {
                        // Directly fetch messages without token delays
                        JSONObject response = ApiClient.fetchMessages();

                        // Pass to router
                        MessageRouter.route(response);

                        // Success -> reset backoff and reschedule
                        BackoffManager.reset();
                        if (running) {
                            handler.postDelayed(pollRunnable, backoffBase());
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Poll failed: " + e.getMessage());

                        // Network or parsing failed -> increase backoff and reschedule
                        BackoffManager.increase();
                        if (running) {
                            handler.postDelayed(pollRunnable, BackoffManager.nextDelay());
                        }
                    }
                });
            } else {
                // Not foreground: don't poll often. Reset and reschedule after base delay.
                BackoffManager.reset();
                if (running) {
                    handler.postDelayed(pollRunnable, backoffBase());
                }
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