package com.cocode.notifyx.model;

public enum UpdateMode {
    FLEXIBLE,
    IMMEDIATE;

    public static UpdateMode fromString(String s) {
        if (s == null) return FLEXIBLE;
        try {
            return UpdateMode.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return FLEXIBLE;
        }
    }
}
