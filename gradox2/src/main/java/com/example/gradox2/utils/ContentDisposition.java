package com.example.gradox2.utils;

public final class ContentDisposition {

    private static final String FALLBACK_FILENAME = "download";

    private ContentDisposition() {
    }

    public static String attachmentOf(String filename) {
        String safe = sanitize(filename);
        return "attachment; filename=\"" + safe + "\"";
    }

    public static String inlineOf(String filename) {
        String safe = sanitize(filename);
        return "inline; filename=\"" + safe + "\"";
    }

    private static String sanitize(String filename) {
        if (filename == null) {
            return FALLBACK_FILENAME;
        }
        String safe = filename
                .replace("\"", "")
                .replace("\\", "")
                .replace("\r", "")
                .replace("\n", "")
                .trim();
        return safe.isBlank() ? FALLBACK_FILENAME : safe;
    }
}