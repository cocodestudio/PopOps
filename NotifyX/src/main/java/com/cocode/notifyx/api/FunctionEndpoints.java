package com.cocode.notifyx.api;

/**
 * Update REGION and PROJECT to match your Firebase Cloud Functions deployment.
 */
public final class FunctionEndpoints {
    public static final String REGION = "us-central1";
    public static final String PROJECT = "notifyx-b9832";
    private FunctionEndpoints() {
    }

    public static String tokenUrl() {
        return "https://" + REGION + "-" + PROJECT + ".cloudfunctions.net/getSdkToken";
    }

    public static String messagesUrl() {
        return "https://" + REGION + "-" + PROJECT + ".cloudfunctions.net/fetchMessages";
    }
}
