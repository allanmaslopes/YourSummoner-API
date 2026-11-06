package com.your.summoner.infrastructure.adapter.in.rest.admin;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SessionResponse(String token, Instant expiresAt, String totpEnrollmentUri) {

    @Override
    public String toString() {
        return "SessionResponse[***]";
    }
}
