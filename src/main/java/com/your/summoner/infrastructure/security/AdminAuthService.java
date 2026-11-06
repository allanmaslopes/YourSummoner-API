package com.your.summoner.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.your.summoner.application.port.out.AuditLog;
import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.domain.model.SecretName;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalLong;

/** Single admin user: BCrypt password + TOTP, answered with a 15-minute HS256 JWT. */
public class AdminAuthService {
    public static final String JWT_INFO = "yoursummoner-admin-jwt";
    static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final String ISSUER = "YourSummoner";

    private final AdminProperties properties;
    private final SecretStore secrets;
    private final AuditLog audit;
    private final LoginRateLimiter limiter;
    private final Clock clock;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    // Valid hash used when the configured one is unusable, so BCrypt work never disappears.
    private final String dummyHash = bcrypt.encode("not-the-password");
    private final JwtEncoder encoder;

    public AdminAuthService(AdminProperties properties, MasterKey masterKey, SecretStore secrets, AuditLog audit,
                            LoginRateLimiter limiter, Clock clock) {
        this.properties = properties;
        this.secrets = secrets;
        this.audit = audit;
        this.limiter = limiter;
        this.clock = clock;
        this.encoder = masterKey.present() ? new NimbusJwtEncoder(new ImmutableSecret<>(
                new SecretKeySpec(masterKey.derive(JWT_INFO), "HmacSHA256"))) : null;
    }

    public AdminSession login(String user, String password, String totp, String ip) {
        if (encoder == null) {
            throw new IllegalStateException("Administration is disabled");
        }
        OptionalLong blocked = limiter.retryAfterSeconds(ip);
        if (blocked.isPresent()) {
            throw new TooManyLoginAttemptsException(blocked.getAsLong());
        }
        // Both checks always run, so timing does not tell a wrong user from a wrong password.
        boolean userOk = constantTimeEquals(user, properties.user());
        boolean passwordOk = passwordMatches(password);
        if (!(userOk && passwordOk)) {
            throw fail(ip);
        }
        Optional<String> seed = secrets.find(SecretName.ADMIN_TOTP);
        String enrollmentUri = null;
        if (seed.isEmpty()) {
            String fresh = Totp.newSeed();
            secrets.put(SecretName.ADMIN_TOTP, fresh, properties.user());
            enrollmentUri = Totp.uri(fresh, ISSUER, properties.user());
        } else if (!Totp.verify(seed.get(), totp, clock.instant().getEpochSecond())) {
            throw fail(ip);
        }
        audit.record(properties.user(), enrollmentUri == null ? "login.success" : "login.enroll", "session", ip);
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expires = now.plus(TOKEN_TTL);
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(ISSUER).subject(properties.user())
                .issuedAt(now).expiresAt(expires).claim("scope", "ADMIN").build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new AdminSession(token, expires, enrollmentUri);
    }

    private InvalidCredentialsException fail(String ip) {
        limiter.recordFailure(ip);
        // Actor is not the typed user name: it may hold a password typed in the wrong field.
        audit.record("unknown", "login.failure", "session", ip);
        return new InvalidCredentialsException();
    }

    private boolean passwordMatches(String password) {
        String candidate = password == null ? "" : password;
        boolean tooLong = candidate.getBytes(StandardCharsets.UTF_8).length > 72; // BCrypt input limit
        String input = tooLong ? "x" : candidate;
        boolean ok;
        try {
            ok = bcrypt.matches(input, properties.passwordHash());
        } catch (IllegalArgumentException e) {
            ok = false;
        }
        if (!ok) {
            bcrypt.matches(input, dummyHash); // keeps timing close to the success path for malformed hashes
        }
        return ok && !tooLong;
    }

    private static boolean constantTimeEquals(String a, String b) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] x = sha.digest((a == null ? "" : a).getBytes(StandardCharsets.UTF_8));
            byte[] y = sha.digest((b == null ? "" : b).getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(x, y);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
