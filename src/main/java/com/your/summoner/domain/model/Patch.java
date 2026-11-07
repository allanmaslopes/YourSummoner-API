package com.your.summoner.domain.model;

import java.util.Map;
import java.util.regex.Pattern;

public record Patch(String version, boolean active, Map<String, PatchStatus> statusByLocale) {

    private static final Pattern VERSION = Pattern.compile("\\d+\\.\\d+\\.\\d+");

    public Patch {
        statusByLocale = statusByLocale == null ? Map.of() : Map.copyOf(statusByLocale);
    }

    public PatchStatus statusFor(String locale) {
        return statusByLocale.getOrDefault(locale, PatchStatus.NOT_INGESTED);
    }

    public boolean isReady(String locale) {
        return statusFor(locale) == PatchStatus.READY;
    }

    public static boolean isValidVersion(String version) {
        if (version == null) {
            return false;
        }
        return VERSION.matcher(version).matches();
    }
}
