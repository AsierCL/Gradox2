package com.example.gradox2.utils;

import java.util.Collection;

public final class SortUtils {
    private SortUtils() {
    }

    public static String resolveSortBy(String requestedSortBy, String fallbackSortBy, Collection<String> allowedSortFields) {
        if (requestedSortBy == null || requestedSortBy.isBlank()) {
            return fallbackSortBy;
        }

        if (allowedSortFields != null && allowedSortFields.contains(requestedSortBy)) {
            return requestedSortBy;
        }

        return fallbackSortBy;
    }
}
