package com.cocode.popops.api;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Optimized HTTP helpers with better error handling.
 */
public final class NetworkUtils {
    private NetworkUtils() {
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
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    conn.setRequestProperty(e.getKey(), e.getValue());
                }
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                String errorMsg = getErrorMsg(conn, code);

                throw new Exception(errorMsg);
            }

            InputStream is = conn.getInputStream();
            if (is == null) {
                throw new IllegalStateException("Empty response");
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                String responseBody = sb.toString();
                if (responseBody.isEmpty()) {
                    throw new Exception("Empty response body");
                }

                return new JSONObject(responseBody);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
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
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    conn.setRequestProperty(e.getKey(), e.getValue());
                }
            }

            byte[] payload = body != null ? body.toString().getBytes(StandardCharsets.UTF_8) : new byte[0];
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                String errorMsg = getErrorMsg(conn, code);

                throw new Exception(errorMsg);
            }

            InputStream is = conn.getInputStream();
            if (is == null) {
                throw new IllegalStateException("Empty response");
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                String responseBody = sb.toString();
                if (responseBody.isEmpty()) {
                    throw new Exception("Empty response body");
                }

                return new JSONObject(responseBody);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @NonNull
    private static String getErrorMsg(HttpURLConnection conn, int code) throws IOException {
        String errorBody = "";
        InputStream errStream = conn.getErrorStream();
        if (errStream != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(errStream))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                errorBody = sb.toString();
            }
        }

        String errorMsg = "HTTP " + code;
        if (!errorBody.isEmpty()) {
            errorMsg += ": " + errorBody;
        }
        return errorMsg;
    }
}