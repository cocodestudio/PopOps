package com.cocode.notifyx.ui.renderers;

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

import com.cocode.notifyx.core.NotifyX;
import com.cocode.notifyx.model.Message;
import com.cocode.notifyx.model.MessageType;
import com.cocode.notifyx.model.UpdateMode;
import com.cocode.notifyx.storage.PopupStateStore;
import com.cocode.notifyx.ui.factory.PresentationRenderer;
import com.cocode.notifyx.ui.layoutrenderers.DialogLayoutRenderer;

/**
 * Shows dialogs ONLY on the target activity specified in NotifyX.init()
 */
public final class DialogRenderer implements PresentationRenderer {
    private static final String TAG = "DialogRenderer";

    @Override
    public void render(Message message) {
        // Get target activity instead of any foreground activity
        Activity activity = NotifyX.getTargetActivity();

        if (activity == null) {
            Log.w(TAG, "Target activity not available, cannot show dialog");
            return;
        }

        // Mark as shown IMMEDIATELY to prevent re-showing
        if (message.id != null) {
            PopupStateStore.markShown(message.id);
        }

        activity.runOnUiThread(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;

            try {
                Dialog d = new Dialog(activity);
                d.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

                int layoutId = new DialogLayoutRenderer().getLayout(message);
                d.setContentView(layoutId);

                boolean isCancelable = message.isCancelable &&
                        !(message.type == MessageType.APP_UPDATE &&
                                message.updateMode == UpdateMode.IMMEDIATE);
                d.setCancelable(isCancelable);

                if (d.getWindow() != null) {
                    d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    d.getWindow().setLayout(
                            (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.90),
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }

                TextView dialogTitle = d.findViewById(com.cocode.notifyx.R.id.dialog_title);
                TextView dialogBody = d.findViewById(com.cocode.notifyx.R.id.dialog_message);
                Button btnPrimary = d.findViewById(com.cocode.notifyx.R.id.btn_primary);
                Button btnSecondary = d.findViewById(com.cocode.notifyx.R.id.btn_secondary);

                if (dialogTitle != null) {
                    dialogTitle.setText(message.title != null ? message.title : "");
                }
                if (dialogBody != null) {
                    dialogBody.setText(message.body != null ? message.body : "");
                }

                if (message.type == MessageType.APP_UPDATE) {
                    if (message.updateMode == UpdateMode.IMMEDIATE) {
                        if (btnSecondary != null) btnSecondary.setVisibility(View.GONE);
                        if (btnPrimary != null) {
                            btnPrimary.setText(message.primaryBtnText != null ?
                                    message.primaryBtnText : "Update");
                            btnPrimary.setOnClickListener(v -> {
                                openUrlSafe(activity, message.actionUrl);
                                d.dismiss();
                            });
                        }
                    } else {
                        if (btnPrimary != null) {
                            btnPrimary.setText(message.primaryBtnText != null ?
                                    message.primaryBtnText : "Update");
                            btnPrimary.setOnClickListener(v -> {
                                openUrlSafe(activity, message.actionUrl);
                                d.dismiss();
                            });
                        }
                        if (btnSecondary != null) {
                            btnSecondary.setText(message.secondaryBtnText != null ?
                                    message.secondaryBtnText : "Later");
                            btnSecondary.setOnClickListener(v -> d.dismiss());
                        }
                    }
                } else {
                    if (btnPrimary != null) {
                        btnPrimary.setText(message.primaryBtnText != null ?
                                message.primaryBtnText : "OK");
                        btnPrimary.setOnClickListener(v -> {
                            if (message.actionUrl != null && !message.actionUrl.isEmpty()) {
                                openUrlSafe(activity, message.actionUrl);
                            }
                            d.dismiss();
                        });
                    }
                    if (btnSecondary != null) {
                        btnSecondary.setVisibility(View.GONE);
                    }
                }

                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    d.show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to show dialog", e);
            }
        });
    }

    private void openUrlSafe(Activity activity, String url) {
        try {
            if (url == null || url.isEmpty()) return;

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