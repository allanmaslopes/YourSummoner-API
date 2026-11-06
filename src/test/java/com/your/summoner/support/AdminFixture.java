package com.your.summoner.support;

import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.domain.model.SecretName;
import com.your.summoner.infrastructure.security.AdminAuthService;
import com.your.summoner.infrastructure.security.MasterKey;
import com.your.summoner.infrastructure.security.Totp;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Test-only credentials, generated per JVM. Nothing here is a real secret. */
public final class AdminFixture {
    public static final String USER = "admin-test";
    public static final String PASSWORD = "correct-horse-battery";
    public static final String MASTER_KEY_B64;
    public static final String HASH;
    private static final AtomicInteger NEXT_IP = new AtomicInteger(1);

    static {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        MASTER_KEY_B64 = Base64.getEncoder().encodeToString(key);
        HASH = new BCryptPasswordEncoder(4).encode(PASSWORD);
    }

    private AdminFixture() {
    }

    public static class Enabled implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "yoursummoner.master-key=" + MASTER_KEY_B64,
                    "yoursummoner.admin.user=" + USER,
                    "yoursummoner.admin.password-hash=" + HASH,
                    "yoursummoner.admin.allowed-cidrs=127.0.0.1/32,::1/128,192.168.0.0/16");
        }
    }

    public static class Disabled implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "yoursummoner.master-key=", "yoursummoner.admin.user=", "yoursummoner.admin.password-hash=");
        }
    }

    /** A fresh allowed address per call, so tests do not share rate-limit state. */
    public static String freshIp() {
        return "192.168.0." + NEXT_IP.getAndIncrement();
    }

    public static RequestPostProcessor from(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    public static String loginBody(String user, String password, String totp) {
        return "{\"user\":\"%s\",\"password\":\"%s\",\"totp\":\"%s\"}".formatted(user, password, totp);
    }

    public static MvcResult login(MockMvc mvc, String ip, String user, String password, String totp) throws Exception {
        return mvc.perform(post("/admin/session").with(from(ip)).contentType("application/json")
                .content(loginBody(user, password, totp))).andReturn();
    }

    public static String currentCode(SecretStore secrets) {
        String seed = secrets.find(SecretName.ADMIN_TOTP).orElseThrow();
        return Totp.code(seed, Instant.now().getEpochSecond() / 30);
    }

    /** Logs in (enrolling the TOTP on first use) and returns the Authorization header value. */
    public static String bearer(MockMvc mvc, SecretStore secrets, String ip) throws Exception {
        String totp = secrets.find(SecretName.ADMIN_TOTP).isPresent() ? currentCode(secrets) : "";
        MvcResult result = login(mvc, ip, USER, PASSWORD, totp);
        String body = result.getResponse().getContentAsString();
        if (result.getResponse().getStatus() != 200) {
            throw new AssertionError("login failed: " + result.getResponse().getStatus());
        }
        return "Bearer " + com.jayway.jsonpath.JsonPath.<String>read(body, "$.token");
    }

    public static String jwtKeyInfo() {
        return AdminAuthService.JWT_INFO;
    }

    public static MasterKey masterKey() {
        return MasterKey.parse(MASTER_KEY_B64);
    }
}
