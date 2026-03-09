package com.cocode.popops.ui.renderers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.cocode.popops.R;
import com.cocode.popops.core.PopOps;
import com.cocode.popops.model.Message;
import com.cocode.popops.storage.PopupStateStore;
import com.cocode.popops.tracking.ImpressionTracker;
import com.cocode.popops.ui.factory.PresentationRenderer;

/**
 * Shows toasts ONLY on the target activity specified in PopOps.init()
 */
public final class ToastRenderer implements PresentationRenderer {
    private static final String TAG = "ToastRenderer";
    private static volatile boolean isToastVisible = false;

    @Override
    public void render(Message message) {
        // Get target activity instead of any foreground activity
        Activity activity = PopOps.getTargetActivity();

        if (activity == null) {
            Log.w(TAG, "Target activity not available, cannot show toast");
            return;
        }

        if (isToastVisible) {
            Log.d(TAG, "Toast already visible");
            return;
        }

        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();

        if (!PopupStateStore.isShown(message.messageId)) {

            activity.runOnUiThread(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;

                try {
                    isToastVisible = true;

                    LayoutInflater inflater = LayoutInflater.from(activity);
                    View toastView = inflater.inflate(R.layout.toast_layout, null);

                    // Setup views
                    ImageView iconView = toastView.findViewById(R.id.toastIcon);
                    TextView titleView = toastView.findViewById(R.id.toastTitle);
                    TextView subtitleView = toastView.findViewById(R.id.toastSubtitle);
                    ImageView closeButton = toastView.findViewById(R.id.toastClose);
                    ConstraintLayout container = toastView.findViewById(R.id.toastContainer);

                    // Configure based on type
                    switch (message.type) {
                        case INFORMATIONAL:
                            container.setBackgroundResource(R.drawable.toast_background_informational);
                            iconView.setImageResource(R.drawable.ic_bell);
                            iconView.setColorFilter(ContextCompat.getColor(activity, R.color.popops_md_theme_primary));
                            break;
                        case SUCCESS:
                            container.setBackgroundResource(R.drawable.toast_background_success);
                            iconView.setImageResource(R.drawable.ic_check);
                            iconView.setColorFilter(ContextCompat.getColor(activity, R.color.popops_semantic_success));
                            break;
                        case WARNING:
                            container.setBackgroundResource(R.drawable.toast_background_warning);
                            iconView.setImageResource(R.drawable.ic_warning);
                            iconView.setColorFilter(ContextCompat.getColor(activity, R.color.popops_semantic_warning));
                            break;
                        case ERROR:
                            container.setBackgroundResource(R.drawable.toast_background_error);
                            iconView.setImageResource(R.drawable.ic_error);
                            iconView.setColorFilter(ContextCompat.getColor(activity, R.color.popops_md_theme_error));
                            break;
                    }

                    titleView.setText(message.title);
                    if (message.body != null && !message.body.isEmpty()) {
                        subtitleView.setText(message.body);
                        subtitleView.setVisibility(View.VISIBLE);
                    } else {
                        subtitleView.setVisibility(View.GONE);
                    }

                    // Setup layout params to position at top
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.gravity = Gravity.TOP;

                    int statusBarHeight = getStatusBarHeight(activity);
                    params.topMargin = statusBarHeight + dpToPx(16, activity);
                    params.leftMargin = dpToPx(16, activity);
                    params.rightMargin = dpToPx(16, activity);

                    // Add to root view
                    rootView.addView(toastView, params);

                    // Slide in animation
                    AnimationSet slideIn = new AnimationSet(true);
                    TranslateAnimation translateIn = new TranslateAnimation(0, 0, -300, 0);
                    translateIn.setDuration(300);
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                    fadeIn.setDuration(300);
                    slideIn.addAnimation(translateIn);
                    slideIn.addAnimation(fadeIn);
                    toastView.startAnimation(slideIn);

                    // Close button functionality
                    View.OnClickListener dismissListener = v -> dismissToast(toastView, rootView);
                    closeButton.setOnClickListener(dismissListener);

                    // Auto dismiss after duration
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (toastView.getParent() != null) {
                            dismissToast(toastView, rootView);
                        }
                    }, 3000);

                    // 1. Mark the dynamic ID as shown so it doesn't loop
                    PopupStateStore.markShown(message.messageId);

                    // 2. Track impression using the FIXED Firebase Node ID
                    ImpressionTracker.track(message.id);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to show toast", e);
                    isToastVisible = false;
                }
            });
        }
    }

    private void dismissToast(View toastView, ViewGroup rootView) {
        AnimationSet slideOut = new AnimationSet(true);
        TranslateAnimation translateOut = new TranslateAnimation(0, 0, 0, -300);
        translateOut.setDuration(250);
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(250);
        slideOut.addAnimation(translateOut);
        slideOut.addAnimation(fadeOut);

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    rootView.removeView(toastView);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to remove toast view", e);
                }
                isToastVisible = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        toastView.startAnimation(slideOut);
    }

    private int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getStatusBarHeight(Context context) {
        @SuppressLint("InternalInsetResource")
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}