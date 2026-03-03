package com.cocode.popops.api;

public final class FunctionEndpoints {
    public static final String REGION = "us-central1";
    // Updated to match your actual Firebase project ID
    public static final String PROJECT = "popops-9ec2e";

    private FunctionEndpoints() {
    }

    public static String messagesUrl() {
        return "https://" + REGION + "-" + PROJECT + ".cloudfunctions.net/fetchMessages";
    }

    // New Endpoint for Impression Tracking
    public static String impressionUrl() {
        return "https://" + REGION + "-" + PROJECT + ".cloudfunctions.net/recordImpression";
    }
}