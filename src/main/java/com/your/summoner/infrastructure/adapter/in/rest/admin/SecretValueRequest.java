package com.your.summoner.infrastructure.adapter.in.rest.admin;

public record SecretValueRequest(String value) {

    @Override
    public String toString() {
        return "SecretValueRequest[***]";
    }
}
