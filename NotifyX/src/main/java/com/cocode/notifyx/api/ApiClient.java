package com.cocode.notifyx.api;

import com.cocode.notifyx.core.NotifyXEnvironment;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Optimized API Client with better error handling.
 */
public final class ApiClient {
    private ApiClient() {
    }

    /**
     * Request new token from server.
     * This should only be called when:
     * 1. No token exists
     * 2. Token is expired or about to expire
     *
     * @return JSON response with token and expiry
     * @throws Exception if request fails
     */
    public static JSONObject requestToken() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("projectId", NotifyXEnvironment.projectId);
        payload.put("packageName", NotifyXEnvironment.packageName);
        payload.put("appVersion", NotifyXEnvironment.appVersion);
        payload.put("currentTime",System.currentTimeMillis());

        JSONObject response = NetworkUtils.postJson(FunctionEndpoints.tokenUrl(), payload, null);

        // Validate response
        if (response == null) {
            throw new Exception("Empty response from token endpoint");
        }

        if (!response.has("token") || !response.has("expiresAt")) {
            throw new Exception("Invalid token response: missing required fields");
        }

        return response;
    }

    /**
     * Fetch messages from server using authenticated token.
     *
     * @param token Valid authentication token
     * @return JSON response with messages
     * @throws Exception if request fails (including 401 Unauthorized)
     */
    public static JSONObject fetchMessages(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("X-Package", NotifyXEnvironment.packageName);

        String url = FunctionEndpoints.messagesUrl() + "?projectId=" + NotifyXEnvironment.projectId;

        try {
            JSONObject response = NetworkUtils.getJson(url, headers);

            // Validate response
            if (response == null) {
                throw new Exception("Empty response from messages endpoint");
            }

            return response;

        } catch (Exception e) {
            // Re-throw with clearer error message
            String message = e.getMessage();
            if (message != null && message.contains("401")) {
                throw new Exception("401 Unauthorized - Token invalid or expired", e);
            }
            throw e;
        }
    }
}