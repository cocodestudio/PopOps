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

/**
 * Polls the backend for messages while the app is foregrounded.
 * Uses a HandlerThread + single-thread executor for network calls.
 * Implements exponential backoff (with jitter) via BackoffManager.
 */
public final class MessagePoller {
    private static final String TAG = "NotifyX";
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static Handler handler;
    private static volatile boolean running = false;

    private MessagePoller() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;

        HandlerThread ht = new HandlerThread("popup-sdk-poller");
        ht.start();
        handler = new Handler(ht.getLooper());

        // schedule first run immediately
        handler.post(pollRunnable);
    }

    private static long backoffBase() {
        // A short base when background; it will be jittered by BackoffManager when nextDelay called
        return 30_000L;
    }

    public static synchronized void stop() {
        running = false;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        EXEC.shutdownNow();
    }

    private static final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;


            if (AppState.isForeground()) {
                EXEC.submit(() -> {
                    Log.d(TAG, "Polling has started");
                    long delay = BackoffManager.nextDelay();
                    try {
                        // Attempt to obtain token (synchronously if necessary)
                        String token = TokenManager.getToken();
                        if (token == null) {
                            // no token available — increase backoff and reschedule
                            Log.d(TAG, "No token available");
                            BackoffManager.increase();
                            handler.postDelayed(pollRunnable, delay);
                            return;
                        }

                        JSONObject res = ApiClient.fetchMessages(token);
                        // route the response (safe parsing inside)
                        MessageRouter.route(res);

                        // success -> reset backoff and schedule next
                        BackoffManager.reset();
                        handler.postDelayed(pollRunnable, BackoffManager.nextDelay());
                    } catch (Exception e) {
                        // network or parsing failed -> increase backoff and reschedule
                        BackoffManager.increase();
                        handler.postDelayed(pollRunnable, BackoffManager.nextDelay());
                    }
                });
            } else {
                // Not foreground: don't poll often. Reset and reschedule after base delay.
                BackoffManager.reset();
                handler.postDelayed(pollRunnable, backoffBase());
            }
        }
    };


}
