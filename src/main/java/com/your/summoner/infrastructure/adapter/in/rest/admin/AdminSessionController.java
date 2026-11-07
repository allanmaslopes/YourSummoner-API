package com.your.summoner.infrastructure.adapter.in.rest.admin;

import com.your.summoner.infrastructure.security.AdminAuthService;
import com.your.summoner.infrastructure.security.AdminSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminSessionController {
    private final AdminAuthService auth;

    public AdminSessionController(AdminAuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/admin/session")
    SessionResponse login(@Valid @RequestBody SessionRequest request, HttpServletRequest http) {
        AdminSession s = auth.login(request.user(), request.password(), request.totp(), http.getRemoteAddr());
        return new SessionResponse(s.token(), s.expiresAt(), s.totpEnrollmentUri());
    }
}
