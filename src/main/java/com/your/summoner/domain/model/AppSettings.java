package com.your.summoner.domain.model;

import com.your.summoner.domain.exception.InvalidInputException;

public record AppSettings(String locale, Platform defaultPlatform) {
    public AppSettings {
        if (locale == null || !locale.matches("[a-z]{2}_[A-Z]{2}")) {
            throw new InvalidInputException("locale must be in format [a-z]{2}_[A-Z]{2}");
        }
        if (defaultPlatform == null) {
            throw new InvalidInputException("defaultPlatform cannot be null");
        }
    }
}
