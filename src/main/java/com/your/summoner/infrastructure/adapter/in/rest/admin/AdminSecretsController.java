package com.your.summoner.infrastructure.adapter.in.rest.admin;

import com.your.summoner.application.port.in.ManageSecretsUseCase;
import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.model.SecretMetadata;
import com.your.summoner.domain.model.SecretName;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminSecretsController {
    private final ManageSecretsUseCase secrets;

    public AdminSecretsController(ManageSecretsUseCase secrets) {
        this.secrets = secrets;
    }

    @GetMapping("/admin/secrets")
    List<SecretMetadata> list() {
        return secrets.list();
    }

    @PutMapping("/admin/secrets/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void put(@PathVariable String name, @RequestBody SecretValueRequest body, Authentication auth,
             HttpServletRequest http) {
        SecretName secret;
        try {
            secret = SecretName.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Unknown secret");
        }
        secrets.put(secret, body.value(), auth.getName(), http.getRemoteAddr());
    }
}
