package com.cocode.notifyx.core;

import android.annotation.SuppressLint;
import android.content.Context;

import com.cocode.notifyx.util.AppUtils;

public final class NotifyXEnvironment {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static String projectId;
    public static String packageName;
    public static String appVersion;

    private NotifyXEnvironment() {
    }

    public static void init(Context ctx, String pid) {
        context = ctx;
        projectId = pid;
        packageName = ctx.getPackageName();
        appVersion = AppUtils.getVersionName(ctx);
    }
}
