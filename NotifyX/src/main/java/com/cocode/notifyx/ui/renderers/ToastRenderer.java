package com.cocode.notifyx.ui.renderers;

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

import androidx.core.content.ContextCompat;

import com.cocode.notifyx.R;
import com.cocode.notifyx.core.NotifyX;
import com.cocode.notifyx.model.Message;
import com.cocode.notifyx.storage.PopupStateStore;
import com.cocode.notifyx.ui.factory.PresentationRenderer;

/**
 * Shows toasts ONLY on the target activity specified in NotifyX.init()
 */
public final class ToastRenderer implements PresentationRenderer {
    private static final String TAG = "ToastRenderer";
    private static volatile boolean isToastVisible = false;

    @Override
    public void render(Message message) {
        // Get target activity instead of any foreground activity
        Activity activity = NotifyX.getTargetActivity();

        if (activity == null) {
            Log.w(TAG, "Target activity not available, cannot show toast");
            return;
        }

        if (isToastVisible) {
            Log.d(TAG, "Toast already visible");
            return;
        }

        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        if (rootView == null) {
            Log.w(TAG, "No root view available");
            return;
        }

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
                FrameLayout container = toastView.findViewById(R.id.toastContainer);

                // Configure based on type
                switch (message.type) {
                    case INFORMATIONAL:
                        container.setBackgroundResource(R.drawable.toast_bg_info);
                        iconView.setImageResource(R.drawable.ic_bell);
                        iconView.setColorFilter(ContextCompat.getColor(activity, R.color.notifyx_blue));
                        break;
                    case SUCCESS:
                        container.setBackgroundResource(R.drawable.toast_bg_success);
                        iconView.setImageResource(R.drawable.ic_check);
                        iconView.setColorFilter(ContextCompat.getColor(activity, R.color.success_icon));
                        break;
                    case WARNING:
                        container.setBackgroundResource(R.drawable.toast_bg_warning);
                        iconView.setImageResource(R.drawable.ic_warning);
                        iconView.setColorFilter(ContextCompat.getColor(activity, R.color.warning_icon));
                        break;
                    case ERROR:
                        container.setBackgroundResource(R.drawable.toast_bg_error);
                        iconView.setImageResource(R.drawable.ic_error);
                        iconView.setColorFilter(ContextCompat.getColor(activity, R.color.error_icon));
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

                // Mark as shown immediately
                PopupStateStore.markShown(message.id);

            } catch (Exception e) {
                Log.e(TAG, "Failed to show toast", e);
                isToastVisible = false;
            }
        });
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