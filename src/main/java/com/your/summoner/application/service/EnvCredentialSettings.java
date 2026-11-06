package com.your.summoner.application.service;

/** Credentials coming from the environment, used when the vault has no value. */
public record EnvCredentialSettings(String riotApiToken) {
    @Override
    public String toString() {
        return "EnvCredentialSettings[***]";
    }
}
