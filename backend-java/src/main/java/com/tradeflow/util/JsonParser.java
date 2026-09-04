package com.tradeflow.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight JSON utility for extracting fields from flat JSON payloads
 * without requiring external JSON libraries.
 */
public class JsonParser {

    public static String getString(String json, String key) {
        if (json == null) return null;
        // Match: "key"\s*:\s*"([^"]*)"
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public static Integer getInt(String json, String key) {
        if (json == null) return null;
        // Match: "key"\s*:\s*(-?\d+)
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1).trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public static Double getDouble(String json, String key) {
        if (json == null) return null;
        // Match: "key"\s*:\s*(-?\d+(?:\.\d+)?)
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1).trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public static String escape(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
