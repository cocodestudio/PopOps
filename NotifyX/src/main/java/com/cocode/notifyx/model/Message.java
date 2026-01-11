package com.cocode.notifyx.model;

public final class Message {
    public String id;
    public MessageType type;
    public PresentationType presentation;
    public String title;
    public String body;
    public String actionUrl;

    // For app update messages
    public String newAppVersion;
    public UpdateMode updateMode;

    // For non-update messages
    public String targetVersion; // null => all

    // Topic
    public String topic;
    public boolean isCancelable;
    public String primaryBtnText;
    public String secondaryBtnText;
}
