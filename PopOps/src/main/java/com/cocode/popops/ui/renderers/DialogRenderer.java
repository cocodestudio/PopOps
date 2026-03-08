package com.cocode.popops.ui.renderers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cocode.popops.R;
import com.cocode.popops.core.PopOps;
import com.cocode.popops.model.Message;
import com.cocode.popops.model.MessageType;
import com.cocode.popops.model.UpdateMode;
import com.cocode.popops.storage.PopupStateStore;
import com.cocode.popops.tracking.ImpressionTracker;
import com.cocode.popops.ui.factory.PresentationRenderer;
import com.cocode.popops.ui.layoutrenderers.DialogLayoutRenderer;

public final class DialogRenderer implements PresentationRenderer {
    private static final String TAG = "DialogRenderer";

    // In-memory session lock to prevent 10-second polling duplicates for Immediate Updates
    private static String currentlyShowingMessageId = null;

    @Override
    public void render(Message message) {
        Activity activity = PopOps.getTargetActivity();

        if (activity == null) {
            Log.w(TAG, "Target activity not available, cannot show dialog");
            return;
        }

        boolean isImmediateUpdate = (message.type == MessageType.APP_UPDATE && message.updateMode == UpdateMode.IMMEDIATE);

        if (isImmediateUpdate) {
            // For immediate updates, block it ONLY if it is currently visible on the screen
            if (message.messageId.equals(currentlyShowingMessageId)) {
                return;
            }
        } else {
            // For normal messages, check persistent storage
            if (PopupStateStore.isShown(message.messageId)) {
                return;
            }
            // Mark normal messages as permanently shown
            // (Note: use addShown or markShown depending on what your PopupStateStore.java uses)
            PopupStateStore.markShown(message.messageId);
        }

        // 1. Lock the session for this specific message
        currentlyShowingMessageId = message.messageId;

        // 2. Track impression using the FIXED Firebase Node ID
        ImpressionTracker.track(message.id);

        activity.runOnUiThread(() -> {
            try {
                DialogLayoutRenderer layoutRenderer = new DialogLayoutRenderer();
                int layoutResId = layoutRenderer.getLayout(message);

                Dialog d = new Dialog(activity);
                d.setContentView(layoutResId);

                if (d.getWindow() != null) {
                    d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                // Release the session lock if the dialog is naturally dismissed
                d.setOnDismissListener(dialog -> {
                    if (message.messageId.equals(currentlyShowingMessageId)) {
                        currentlyShowingMessageId = null;
                    }
                });

                // Apply strict non-cancelable behavior for IMMEDIATE app updates
                if (isImmediateUpdate) {
                    d.setCancelable(false);
                    d.setCanceledOnTouchOutside(false);
                } else {
                    d.setCancelable(true);
                    d.setCanceledOnTouchOutside(true);
                }

                TextView title = d.findViewById(R.id.dialog_title);
                TextView body = d.findViewById(R.id.dialog_message);
                TextView versionLabel = d.findViewById(R.id.new_version_text);
                Button btnPrimary = d.findViewById(R.id.btn_primary);
                Button btnSecondary = d.findViewById(R.id.btn_secondary);

                if (title != null) title.setText(message.title);
                if (body != null) body.setText(message.body);

                if (versionLabel != null) {
                    versionLabel.setText(message.newAppVersion);
                }

                if (btnPrimary != null) {
                    btnPrimary.setText("Update");
                    btnPrimary.setOnClickListener(v -> {
                        if (message.actionUrl != null && !message.actionUrl.isEmpty()) {
                            openUrlSafe(activity, message.actionUrl);
                        }

                        // Immediate updates NEVER dismiss when primary button is clicked
                        if (!isImmediateUpdate) {
                            d.dismiss();
                        }
                    });
                }

                if (btnSecondary != null) {
                    if (isImmediateUpdate) {
                        btnSecondary.setVisibility(View.GONE);
                    } else {
                        btnSecondary.setVisibility(View.VISIBLE);
                        btnSecondary.setText("Later");
                        btnSecondary.setOnClickListener(v -> d.dismiss());
                    }
                }

                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    d.show();
                } else {
                    // Release the lock if the activity died before we could show it
                    currentlyShowingMessageId = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to show dialog", e);
                currentlyShowingMessageId = null;
            }
        });
    }

    private void openUrlSafe(Activity activity, String url) {
        try {
            if (url == null || url.isEmpty()) return;

            Log.d(TAG, url);

            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) {
                Log.w(TAG, "Invalid URL: missing scheme or host");
                return;
            }

            if (!scheme.equals("http") && !scheme.equals("https")) {
                Log.w(TAG, "Blocked non-HTTP(S) URL: " + scheme);
                return;
            }

            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open URL", e);
        }
    }
}