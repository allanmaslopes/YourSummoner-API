package com.your.summoner.infrastructure.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** RFC 6238 TOTP: HMAC-SHA1, 6 digits, 30 s step. The stored form of a seed is Base64. */
public final class Totp {
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Totp() {
    }

    public static String newSeed() {
        byte[] seed = new byte[20];
        RANDOM.nextBytes(seed);
        return Base64.getEncoder().encodeToString(seed);
    }

    public static String uri(String seed, String issuer, String account) {
        return "otpauth://totp/" + enc(issuer) + ":" + enc(account) + "?secret="
                + base32(Base64.getDecoder().decode(seed)) + "&issuer=" + enc(issuer)
                + "&algorithm=SHA1&digits=6&period=30";
    }

    public static String code(String seed, long counter) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(seed), "HmacSHA1"));
            byte[] h = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int o = h[h.length - 1] & 0x0f;
            int bin = ((h[o] & 0x7f) << 24) | ((h[o + 1] & 0xff) << 16) | ((h[o + 2] & 0xff) << 8) | (h[o + 3] & 0xff);
            return "%06d".formatted(bin % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Accepts the previous, current and next step; compares all three in constant time. */
    public static boolean verify(String seed, String candidate, long epochSeconds) {
        if (candidate == null || !candidate.matches("\\d{6}")) {
            return false;
        }
        byte[] given = candidate.getBytes(StandardCharsets.US_ASCII);
        long step = epochSeconds / 30;
        boolean ok = false;
        for (long s = step - 1; s <= step + 1; s++) {
            ok |= MessageDigest.isEqual(given, code(seed, s).getBytes(StandardCharsets.US_ASCII));
        }
        return ok;
    }

    private static String base32(byte[] data) {
        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                out.append(BASE32.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
            buffer &= (1 << bits) - 1;
        }
        if (bits > 0) {
            out.append(BASE32.charAt((buffer << (5 - bits)) & 31));
        }
        return out.toString();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
