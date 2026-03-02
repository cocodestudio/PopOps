package com.cocode.popops.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public final class AppUtils {
    private AppUtils() {
    }

    public static String getVersionName(Context ctx) {
        try {
            PackageInfo pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return pi.versionName != null ? pi.versionName : "0";
        } catch (PackageManager.NameNotFoundException e) {
            return "0";
        }
    }
}
