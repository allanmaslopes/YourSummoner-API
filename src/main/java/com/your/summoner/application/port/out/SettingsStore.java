package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.AppSettings;

public interface SettingsStore {
    AppSettings get();

    void put(AppSettings settings, String actor);
}
