package com.cocode.popops.api;

import com.cocode.popops.core.PopOpsEnvironment;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Optimized API Client. Directly fetches messages without token overhead,
 * using a high-speed SHA-256 signature to verify the source securely.
 */
public final class ApiClient {
    // Secret salt to verify requests are originating from the official SDK
    private static final String SDK_SECRET = "_PopOpsSecure2026";

    private ApiClient() {
    }

    /**
     * Fetch messages directly from server.
     *
     * @return JSON response with messages
     * @throws Exception if request fails
     */
    public static JSONObject fetchMessages() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Package", PopOpsEnvironment.packageName);

        // Generate High-Speed Security Signature
        String payload = PopOpsEnvironment.projectId + "_" + PopOpsEnvironment.packageName + SDK_SECRET;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(payload.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        headers.put("X-Signature", hexString.toString());

        String url = FunctionEndpoints.messagesUrl() + "?projectId=" + PopOpsEnvironment.projectId;

        return NetworkUtils.getJson(url, headers);

    }

    /**
     * Securely sends an impression record to the backend.
     */
    public static void recordImpression(String messageId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Package", PopOpsEnvironment.packageName);

        // Include the messageId in the hashing payload to prevent replay attacks
        String payload = PopOpsEnvironment.projectId + "_" + PopOpsEnvironment.packageName + "_" + messageId + SDK_SECRET;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(payload.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        headers.put("X-Signature", hexString.toString());

        JSONObject body = new JSONObject();
        body.put("projectId", PopOpsEnvironment.projectId);
        body.put("messageId", messageId);

        NetworkUtils.postJson(FunctionEndpoints.impressionUrl(), body, headers);
    }
}