package com.your.summoner.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties("yoursummoner.admin")
public record AdminProperties(
        @DefaultValue("") String user,
        @DefaultValue("") String passwordHash,
        @DefaultValue({"127.0.0.1/32", "::1/128"}) List<String> allowedCidrs) {

    @Override
    public String toString() {
        return "AdminProperties[***]";
    }
}
