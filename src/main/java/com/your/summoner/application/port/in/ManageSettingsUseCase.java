package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.AppSettings;

public interface ManageSettingsUseCase {
    AppSettings get();

    void put(AppSettings settings, String actor, String ip);
}
