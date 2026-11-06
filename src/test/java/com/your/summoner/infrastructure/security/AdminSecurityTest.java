package com.your.summoner.infrastructure.security;

import com.jayway.jsonpath.JsonPath;
import java.util.concurrent.ExecutorService;
import com.your.summoner.application.port.out.RiotCredentials;
import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.application.port.out.SettingsStore;
import com.your.summoner.application.service.CredentialService;
import com.your.summoner.domain.model.AppSettings;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.support.AdminFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

import static com.your.summoner.support.AdminFixture.PASSWORD;
import static com.your.summoner.support.AdminFixture.USER;
import static com.your.summoner.support.AdminFixture.freshIp;
import static com.your.summoner.support.AdminFixture.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = AdminFixture.Enabled.class)
class AdminSecurityTest {
    private static final String PROBLEM = "application/problem+json";

    @Autowired MockMvc mvc;
    @Autowired SecretStore secrets;
    @Autowired MongoTemplate mongo;
    @Autowired RiotCredentials credentials;
    @Autowired SettingsStore settings;
    // Drops the post-locale-change sync: the test must not ingest a patch from the network.
    @MockitoBean(name = "adminSettingsExecutor") ExecutorService adminSettingsExecutor;

    @BeforeEach
    void cleanVault() {
        mongo.dropCollection("secrets");
        mongo.dropCollection("admin_audit");
        mongo.dropCollection("app_settings");
        settings.put(new AppSettings("pt_BR", Platform.BR1), USER);
    }

    @Test
    void publicRoutesStayOpen() throws Exception {
        mvc.perform(get("/actuator/health").with(from("203.0.113.9"))).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs.yaml").with(from("203.0.113.9"))).andExpect(status().isOk());
        // a public prefix is open to reads only
        mvc.perform(post("/champions/anything").with(from("203.0.113.9"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/something-else").with(from("203.0.113.9"))).andExpect(status().isUnauthorized());
    }

    @Test
    void adminWithoutTokenIs401() throws Exception {
        mvc.perform(get("/admin/secrets").with(from(freshIp())))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM));
    }

    @Test
    void loginWithWrongPasswordIs401Generic() throws Exception {
        MvcResult wrongPassword = AdminFixture.login(mvc, freshIp(), USER, "wrong-password", "");
        MvcResult wrongUser = AdminFixture.login(mvc, freshIp(), "someone-else", PASSWORD, "");
        assertThat(wrongPassword.getResponse().getStatus()).isEqualTo(401);
        assertThat(wrongUser.getResponse().getStatus()).isEqualTo(401);
        assertThat(wrongPassword.getResponse().getContentType()).startsWith(PROBLEM);
        assertThat(wrongUser.getResponse().getContentAsString()).isEqualTo(wrongPassword.getResponse().getContentAsString());
        assertThat(wrongPassword.getResponse().getContentAsString()).doesNotContain("wrong-password");
    }

    @Test
    void sixthFailedLoginIn15MinIs429() throws Exception {
        String ip = freshIp();
        for (int i = 0; i < 5; i++) {
            assertThat(AdminFixture.login(mvc, ip, USER, "wrong", "").getResponse().getStatus()).isEqualTo(401);
        }
        // even the right password is refused once blocked
        MvcResult blocked = AdminFixture.login(mvc, ip, USER, PASSWORD, "");
        assertThat(blocked.getResponse().getStatus()).isEqualTo(429);
        assertThat(blocked.getResponse().getContentType()).startsWith(PROBLEM);
        assertThat(Long.parseLong(blocked.getResponse().getHeader("Retry-After"))).isBetween(1L, 901L);
        // another address is not affected
        assertThat(AdminFixture.login(mvc, freshIp(), USER, "wrong", "").getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void firstLoginReturnsEnrollmentUriOnce() throws Exception {
        MvcResult first = AdminFixture.login(mvc, freshIp(), USER, PASSWORD, "");
        assertThat(first.getResponse().getStatus()).isEqualTo(200);
        String body = first.getResponse().getContentAsString();
        assertThat(JsonPath.<String>read(body, "$.totpEnrollmentUri")).startsWith("otpauth://totp/");
        assertThat(JsonPath.<String>read(body, "$.token")).isNotBlank();
        Instant.parse(JsonPath.<String>read(body, "$.expiresAt"));

        MvcResult second = AdminFixture.login(mvc, freshIp(), USER, PASSWORD, AdminFixture.currentCode(secrets));
        assertThat(second.getResponse().getStatus()).isEqualTo(200);
        assertThat(second.getResponse().getContentAsString()).doesNotContain("totpEnrollmentUri");
    }

    @Test
    void loginWithoutTotpAfterEnrollmentIs401() throws Exception {
        AdminFixture.login(mvc, freshIp(), USER, PASSWORD, "");
        assertThat(AdminFixture.login(mvc, freshIp(), USER, PASSWORD, "").getResponse().getStatus()).isEqualTo(401);
        assertThat(AdminFixture.login(mvc, freshIp(), USER, PASSWORD, "abc").getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void expiredTokenIs401() throws Exception {
        String ip = freshIp();
        mvc.perform(get("/admin/secrets").with(from(ip)).header("Authorization", "Bearer " + token(
                        AdminFixture.masterKey().derive(AdminAuthService.JWT_INFO), Instant.now().minusSeconds(600), "ADMIN")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM));
    }

    @Test
    void forgedOrUnprivilegedTokenIsRejected() throws Exception {
        String ip = freshIp();
        Instant future = Instant.now().plusSeconds(600);
        mvc.perform(get("/admin/secrets").with(from(ip)).header("Authorization",
                "Bearer " + token(new byte[32], future, "ADMIN"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/admin/secrets").with(from(ip)).header("Authorization",
                "Bearer " + token(AdminFixture.masterKey().derive(AdminAuthService.JWT_INFO), future, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void remoteIpIsForbidden() throws Exception {
        mvc.perform(get("/admin/secrets").with(from("10.0.0.5")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM));
        mvc.perform(post("/admin/session").with(from("10.0.0.5")).contentType("application/json")
                        .header("X-Forwarded-For", "127.0.0.1")
                        .content(AdminFixture.loginBody(USER, PASSWORD, "")))
                .andExpect(status().isForbidden());
    }

    @Test
    void secretsSettingsAndAuditWorkWithAToken() throws Exception {
        String ip = freshIp();
        String auth = AdminFixture.bearer(mvc, secrets, ip);
        mvc.perform(put("/admin/secrets/RIOT_API_KEY").with(from(ip)).header("Authorization", auth)
                .contentType("application/json").content("{\"value\":\"RGAPI-aaaa-bbbb-9876\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/admin/secrets").with(from(ip)).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // ADMIN_TOTP stays hidden
                .andExpect(jsonPath("$[0].name").value("RIOT_API_KEY"))
                .andExpect(jsonPath("$[0].last4").value("9876"))
                .andExpect(jsonPath("$[0].updatedBy").value(USER));
        assertThat(credentials.riotApiKey()).contains("RGAPI-aaaa-bbbb-9876");

        mvc.perform(put("/admin/secrets/ADMIN_TOTP").with(from(ip)).header("Authorization", auth)
                .contentType("application/json").content("{\"value\":\"x\"}")).andExpect(status().isBadRequest());
        mvc.perform(put("/admin/secrets/NOPE").with(from(ip)).header("Authorization", auth)
                .contentType("application/json").content("{\"value\":\"x\"}")).andExpect(status().isBadRequest());

        mvc.perform(get("/admin/settings").with(from(ip)).header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.locale").value("pt_BR"))
                .andExpect(jsonPath("$.defaultPlatform").value("BR1"));
        mvc.perform(put("/admin/settings").with(from(ip)).header("Authorization", auth)
                .contentType("application/json").content("{\"locale\":\"en_US\",\"defaultPlatform\":\"NA1\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/admin/settings").with(from(ip)).header("Authorization", auth))
                .andExpect(jsonPath("$.locale").value("en_US")).andExpect(jsonPath("$.defaultPlatform").value("NA1"));
        mvc.perform(put("/admin/settings").with(from(ip)).header("Authorization", auth)
                .contentType("application/json").content("{\"locale\":\"nope\",\"defaultPlatform\":\"NA1\"}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(PROBLEM));
        mvc.perform(put("/admin/settings").with(from(ip)).header("Authorization", auth)
                .contentType("application/json").content("{\"locale\":\"pt_BR\",\"defaultPlatform\":\"XX9\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/admin/audit?limit=50").with(from(ip)).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action == 'secret.update')].target").value("RIOT_API_KEY"))
                .andExpect(jsonPath("$[?(@.action == 'settings.update')]").isNotEmpty())
                .andExpect(header().exists("Content-Type"));
    }

    @Test
    void credentialServiceReplacesEnvFallback() {
        assertThat(credentials).isInstanceOf(CredentialService.class);
    }

    private static String token(byte[] key, Instant expires, String scope) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(key, "HmacSHA256")));
        JwtClaimsSet claims = JwtClaimsSet.builder().subject(USER).issuedAt(expires.minusSeconds(900))
                .expiresAt(expires).claim("scope", scope).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @Nested
    @ContextConfiguration(initializers = AdminFixture.Disabled.class, inheritInitializers = false)
    class WithoutMasterKey {
        @Autowired MockMvc disabledMvc;

        @Test
        void adminDisabledWithoutMasterKey() throws Exception {
            disabledMvc.perform(get("/admin/secrets").with(from("127.0.0.1")))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(content().contentTypeCompatibleWith(PROBLEM));
            disabledMvc.perform(post("/admin/session").with(from("127.0.0.1")).contentType("application/json")
                            .content(AdminFixture.loginBody(USER, PASSWORD, "")))
                    .andExpect(status().isServiceUnavailable());
            disabledMvc.perform(get("/actuator/health").with(from("127.0.0.1"))).andExpect(status().isOk());
        }
    }
}
