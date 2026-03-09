package com.cocode.popops.ui.renderers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.cocode.popops.R;
import com.cocode.popops.core.PopOps;
import com.cocode.popops.model.Message;
import com.cocode.popops.model.MessageType;
import com.cocode.popops.model.UpdateMode;
import com.cocode.popops.storage.PopupStateStore;
import com.cocode.popops.tracking.ImpressionTracker;
import com.cocode.popops.ui.factory.PresentationRenderer;

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
            PopupStateStore.markShown(message.messageId);
        }

        // 1. Lock the session for this specific message
        currentlyShowingMessageId = message.messageId;

        // 2. Track impression using the FIXED Firebase Node ID
        ImpressionTracker.track(message.id);

        activity.runOnUiThread(() -> {
            try {
                Dialog d = new Dialog(activity);

                // Strictly use the single unified dialog layout
                d.setContentView(R.layout.dialog_layout);

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

                // Target the exact new version text ID you provided
                TextView versionText = d.findViewById(R.id.new_version_text);

                Button btnPrimary = d.findViewById(R.id.btn_primary);
                Button btnSecondary = d.findViewById(R.id.btn_secondary);

                androidx.cardview.widget.CardView cardTop = d.findViewById(R.id.card_view_top);
                ImageView iconTop = null;

                if (cardTop != null && cardTop.getChildCount() > 0) {
                    View child = cardTop.getChildAt(0);
                    if (child instanceof ImageView) {
                        iconTop = (ImageView) child;
                    }
                }

                if (title != null) title.setText(message.title);

                String finalBodyText = message.body;

                // --- DYNAMIC STYLING VARIABLES ---

                // 1. Set explicit Drawables using @DrawableRes to silence lint errors
                @DrawableRes int primaryBtnBgRes = R.drawable.button_primary_blue;
                @DrawableRes int secondaryBtnBgRes = R.drawable.button_secondary_blue;
                @DrawableRes int iconRes;

                // 2. Set explicit Resolved Colors using @ColorInt to silence lint errors
                @ColorInt int primaryBtnTextColor = ContextCompat.getColor(activity, R.color.popops_md_theme_onPrimary);
                @ColorInt int secondaryBtnTextColor = ContextCompat.getColor(activity, R.color.popops_md_theme_onPrimaryContainer);
                @ColorInt int cardBgColor = ContextCompat.getColor(activity, R.color.popops_md_theme_primaryContainer);
                @ColorInt int iconTint = ContextCompat.getColor(activity, R.color.popops_md_theme_onPrimaryContainer);

                String primaryText;
                String secondaryText = "";
                boolean showSecondary = false;

                // --- APPLY LOGIC BASED ON MESSAGE TYPE ---
                if (message.type == MessageType.APP_UPDATE) {
                    primaryText = "Update Now";
                    secondaryText = "Later";
                    // Only show secondary button if the update is flexible
                    showSecondary = (message.updateMode == UpdateMode.FLEXIBLE);

                    @SuppressLint("DiscouragedApi")
                    int downloadRes = activity.getResources().getIdentifier("ic_download", "drawable", activity.getPackageName());
                    iconRes = (downloadRes != 0) ? downloadRes : R.drawable.ic_info;

                    // Handle Version Text Visibility
                    if (versionText != null) {
                        versionText.setVisibility(View.VISIBLE);
                        versionText.setText("v" + message.newAppVersion);
                    } else {
                        finalBodyText = finalBodyText + "\n\nNew Version: v" + message.newAppVersion;
                    }

                } else if (message.type == MessageType.WARNING) {
                    primaryText = "Dismiss";

                    primaryBtnBgRes = R.drawable.button_primary_red;
                    primaryBtnTextColor = Color.WHITE; // Color.WHITE intrinsically returns a @ColorInt

                    cardBgColor = ContextCompat.getColor(activity, R.color.popops_bg_red);
                    iconTint = ContextCompat.getColor(activity, R.color.popops_red);
                    iconRes = R.drawable.ic_warning;

                    if (versionText != null) versionText.setVisibility(View.GONE);

                } else {
                    // INFORMATIONAL (and fallback)
                    primaryText = "Dismiss";

                    primaryBtnBgRes = R.drawable.button_primary_green;
                    primaryBtnTextColor = Color.WHITE; // Color.WHITE intrinsically returns a @ColorInt

                    cardBgColor = ContextCompat.getColor(activity, R.color.popops_semantic_success_container);
                    iconTint = ContextCompat.getColor(activity, R.color.popops_semantic_on_success_container);
                    iconRes = R.drawable.ic_info;

                    if (versionText != null) versionText.setVisibility(View.GONE);
                }

                // --- APPLY RENDERED STYLES TO VIEWS ---
                if (body != null) {
                    body.setText(finalBodyText);
                }

                if (cardTop != null) {
                    cardTop.setCardBackgroundColor(cardBgColor);
                }

                if (iconTop != null) {
                    iconTop.setImageResource(iconRes);
                    iconTop.setColorFilter(iconTint, PorterDuff.Mode.SRC_IN);
                }

                if (btnPrimary != null) {
                    btnPrimary.setText(primaryText);
                    btnPrimary.setBackgroundResource(primaryBtnBgRes);
                    // Pass the color directly; the @ColorInt annotation proves to Studio it is already resolved
                    btnPrimary.setTextColor(primaryBtnTextColor);
                    btnPrimary.setOnClickListener(v -> {
                        if (message.actionUrl != null && !message.actionUrl.isEmpty()) {
                            openUrlSafe(activity, message.actionUrl);
                        }

                        // Immediate updates NEVER dismiss when the primary button is clicked
                        if (!isImmediateUpdate) {
                            d.dismiss();
                        }
                    });
                }

                if (btnSecondary != null) {
                    if (showSecondary) {
                        btnSecondary.setVisibility(View.VISIBLE);
                        btnSecondary.setText(secondaryText);
                        btnSecondary.setBackgroundResource(secondaryBtnBgRes);
                        btnSecondary.setTextColor(secondaryBtnTextColor);
                        btnSecondary.setOnClickListener(v -> d.dismiss());
                    } else {
                        btnSecondary.setVisibility(View.GONE);
                    }
                }

                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    d.show();
                } else {
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