package com.cocode.notifyx.api;

import com.cocode.notifyx.core.NotifyXEnvironment;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Calls Firebase Cloud Functions endpoints using NetworkUtils.
 */
public final class ApiClient {
    private ApiClient() {
    }

    public static JSONObject requestToken() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("projectId", NotifyXEnvironment.projectId);
        payload.put("packageName", NotifyXEnvironment.packageName);
        payload.put("appVersion", NotifyXEnvironment.appVersion);
        payload.put("currentTime", System.currentTimeMillis());
        return NetworkUtils.postJson(FunctionEndpoints.tokenUrl(), payload, null);
    }

    public static JSONObject fetchMessages(String token) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("X-Package", NotifyXEnvironment.packageName);
        String url = FunctionEndpoints.messagesUrl() + "?projectId=" + NotifyXEnvironment.projectId;
        return NetworkUtils.getJson(url, headers);
    }
}
