package com.cocode.popops.model;

public final class Message {
    public String id;
    public String messageId;
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
}
