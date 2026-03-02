package com.cocode.popops.model;

public enum PresentationType {
    DIALOG,
    TOAST;

    public static PresentationType fromString(String s) {
        if (s == null) return DIALOG;
        try {
            return PresentationType.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return DIALOG;
        }
    }
}
