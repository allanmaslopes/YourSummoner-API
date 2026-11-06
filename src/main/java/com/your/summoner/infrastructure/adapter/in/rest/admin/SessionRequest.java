package com.your.summoner.infrastructure.adapter.in.rest.admin;

import jakarta.validation.constraints.Size;

public record SessionRequest(@Size(max = 256) String user, @Size(max = 256) String password, @Size(max = 16) String totp) {

    @Override
    public String toString() {
        return "SessionRequest[***]";
    }
}
