package com.your.summoner.infrastructure.adapter.in.rest.admin;

import com.your.summoner.application.port.in.ManageSettingsUseCase;
import com.your.summoner.domain.model.AppSettings;
import com.your.summoner.domain.model.Platform;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminSettingsController {
    private final ManageSettingsUseCase settings;

    public AdminSettingsController(ManageSettingsUseCase settings) {
        this.settings = settings;
    }

    @GetMapping("/admin/settings")
    SettingsDto get() {
        AppSettings s = settings.get();
        return new SettingsDto(s.locale(), s.defaultPlatform().name());
    }

    @PutMapping("/admin/settings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void put(@RequestBody SettingsDto body, Authentication auth, HttpServletRequest http) {
        settings.put(new AppSettings(body.locale(), Platform.from(body.defaultPlatform())), auth.getName(),
                http.getRemoteAddr());
    }
}
