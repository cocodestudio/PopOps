package com.cocode.popops.core;

import android.annotation.SuppressLint;
import android.content.Context;

import com.cocode.popops.util.AppUtils;

public final class PopOpsEnvironment {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static String projectId;
    public static String packageName;
    public static String appVersion;

    private PopOpsEnvironment() {
    }

    public static void init(Context ctx, String pid) {
        context = ctx;
        projectId = pid;
        packageName = ctx.getPackageName();
        appVersion = AppUtils.getVersionName(ctx);
    }
}