package com.cocode.notifyx.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Minimal safe HTTP helpers for GET/POST returning JSON.
 */
public final class NetworkUtils {
    private NetworkUtils() {
    }

    public static JSONObject postJson(String urlStr, JSONObject body, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet())
                    conn.setRequestProperty(e.getKey(), e.getValue());
            }
            byte[] payload = body != null ? body.toString().getBytes(StandardCharsets.UTF_8) : new byte[0];
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
                os.flush();
            }
            return readJson(conn);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static JSONObject getJson(String urlStr, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet())
                    conn.setRequestProperty(e.getKey(), e.getValue());
            }
            return readJson(conn);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static JSONObject readJson(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) throw new IllegalStateException("Empty response");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return new JSONObject(sb.toString());
        }
    }
}
