package com.your.summoner.support;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Dev helper (docs/admin.md): reads a password from stdin and prints its BCrypt hash (cost 12). */
public final class HashPassword {
    private HashPassword() {
    }

    public static void main(String[] args) throws Exception {
        var in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String password = in.readLine();
        if (password == null || password.isBlank()) {
            System.err.println("Type the password and press Enter.");
            System.exit(1);
        }
        System.out.println(new BCryptPasswordEncoder(12).encode(password));
    }
}
