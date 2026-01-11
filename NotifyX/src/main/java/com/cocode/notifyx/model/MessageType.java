package com.cocode.notifyx.model;

public enum MessageType {
    APP_UPDATE,
    INFORMATIONAL,
    SUCCESS,
    ERROR,
    WARNING;

    public static MessageType fromString(String s) {
        if (s == null) return INFORMATIONAL;
        try {
            return MessageType.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return INFORMATIONAL;
        }
    }
}
