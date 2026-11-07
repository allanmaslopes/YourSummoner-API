package com.your.summoner.infrastructure.security;

/** Deliberately carries no hint about which credential failed. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials", null, false, false);
    }
}
