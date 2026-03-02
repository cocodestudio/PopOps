package com.cocode.popops.util;

public final class VersionUtils {
    private VersionUtils() {
    }

    public static boolean isUpdateRequired(String current, String latest) {
        if (latest == null || latest.isEmpty()) return false;
        if (current == null || current.isEmpty()) return true;
        String[] a = current.split("\\.");
        String[] b = latest.split("\\.");
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int ai = i < a.length ? parseIntSafe(a[i]) : 0;
            int bi = i < b.length ? parseIntSafe(b[i]) : 0;
            if (ai < bi) return true;
            if (ai > bi) return false;
        }
        return false;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
