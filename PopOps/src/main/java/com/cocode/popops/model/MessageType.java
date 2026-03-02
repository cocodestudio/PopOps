package com.cocode.popops.model;

public enum MessageType {
    APP_UPDATE, // For DIALOGS
    INFORMATIONAL, // For DIALOGS & TOAST
    SUCCESS, // For TOAST
    ERROR, // For TOAST
    WARNING; // For DIALOGS & TOAST

    public static MessageType fromString(String s) {
        if (s == null) return INFORMATIONAL;
        try {
            return MessageType.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return INFORMATIONAL;
        }
    }
}
