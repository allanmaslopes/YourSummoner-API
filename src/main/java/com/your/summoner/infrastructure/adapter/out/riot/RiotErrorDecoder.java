package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.domain.exception.ExternalAuthenticationException;
import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.exception.RateLimitExceededException;
import com.your.summoner.infrastructure.adapter.out.UpstreamNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

public final class RiotErrorDecoder implements ErrorDecoder {
    public static final String SERVICE = "Riot API";
    private final ObjectMapper mapper;

    public RiotErrorDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String message = "%s answered %d: %s".formatted(SERVICE, status, message(response));
        return switch (status) {
            case 401, 403 -> new ExternalAuthenticationException(SERVICE, message);
            case 404 -> new UpstreamNotFoundException(message);
            case 429 -> new RateLimitExceededException(retryAfter(response));
            default -> status >= 500
                    ? new ExternalServiceUnavailableException(SERVICE, message)
                    : new ExternalBadResponseException(SERVICE, message);
        };
    }

    private String message(Response response) {
        if (response.body() != null) {
            try (var input = response.body().asInputStream()) {
                var body = mapper.readTree(input);
                var message = body == null ? null : body.path("status").path("message");
                if (message != null && message.isTextual() && !message.asText().isBlank()) {
                    return message.asText();
                }
            } catch (Exception ignored) {
                // Fall back to the HTTP reason; never expose raw upstream payloads.
            }
        }
        if (response.reason() != null && !response.reason().isBlank()) {
            return response.reason();
        }
        HttpStatus status = HttpStatus.resolve(response.status());
        return status == null ? "Unexpected response" : status.getReasonPhrase();
    }

    private Duration retryAfter(Response response) {
        var values = response.headers().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase("Retry-After"))
                .flatMap(entry -> entry.getValue().stream()).findFirst();
        try {
            long seconds = Long.parseLong(values.orElse("").trim());
            return seconds < 0 ? null : Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
