package com.your.summoner.infrastructure.security;

import com.your.summoner.application.port.out.RiotCredentials;
import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.infrastructure.adapter.in.rest.admin.SecretValueRequest;
import com.your.summoner.infrastructure.adapter.in.rest.admin.SessionRequest;
import com.your.summoner.support.AdminFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static com.your.summoner.support.AdminFixture.PASSWORD;
import static com.your.summoner.support.AdminFixture.USER;
import static com.your.summoner.support.AdminFixture.freshIp;
import static com.your.summoner.support.AdminFixture.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "logging.level.com.your.summoner=DEBUG",
        "logging.level.org.springframework.security=DEBUG",
        "logging.level.org.springframework.web=DEBUG"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = AdminFixture.Enabled.class)
@ExtendWith(OutputCaptureExtension.class)
class SecretLeakTest {
    private static final String SECRET = "RGAPI-segredo-de-teste-1234";

    @Autowired MockMvc mvc;
    @Autowired SecretStore secrets;
    @Autowired MongoTemplate mongo;
    @Autowired RiotCredentials credentials;

    @BeforeEach
    void clean() {
        mongo.dropCollection("secrets");
        mongo.dropCollection("admin_audit");
    }

    @Test
    void secretNeverLeaks(CapturedOutput output) throws Exception {
        String ip = freshIp();
        String auth = AdminFixture.bearer(mvc, secrets, ip);
        List<String> responses = new ArrayList<>();

        MvcResult put = mvc.perform(put("/admin/secrets/RIOT_API_KEY").with(from(ip)).header("Authorization", auth)
                .contentType("application/json").content("{\"value\":\"" + SECRET + "\"}"))
                .andExpect(status().isNoContent()).andReturn();
        responses.add(body(put));
        // a rejected value must not be echoed back either
        MvcResult bad = mvc.perform(put("/admin/secrets/RIOT_API_KEY").with(from(ip)).header("Authorization", auth)
                .contentType("application/json").content("{\"value\":\"segredo com espaco 5678\"}"))
                .andExpect(status().isBadRequest()).andReturn();
        responses.add(body(bad));
        MvcResult list = mvc.perform(get("/admin/secrets").with(from(ip)).header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].last4").value("1234")).andReturn();
        responses.add(body(list));
        responses.add(body(mvc.perform(get("/admin/audit").with(from(ip)).header("Authorization", auth)).andReturn()));
        responses.add(body(mvc.perform(get("/admin/settings").with(from(ip)).header("Authorization", auth)).andReturn()));

        assertThat(credentials.riotApiKey()).contains(SECRET); // the vault does serve it internally
        for (String response : responses) {
            assertThat(response).doesNotContain(SECRET).doesNotContain("segredo-de-teste").doesNotContain("espaco");
        }

        String stored = dump("secrets");
        assertThat(stored).doesNotContain(SECRET).doesNotContain("segredo-de-teste");
        String audit = dump("admin_audit");
        assertThat(audit).doesNotContain(SECRET).doesNotContain("segredo-de-teste")
                .doesNotContain(PASSWORD).doesNotContain(auth.substring("Bearer ".length()));
        assertThat(audit).contains("RIOT_API_KEY");

        String logs = output.getAll();
        assertThat(logs).doesNotContain(SECRET).doesNotContain("segredo-de-teste").doesNotContain(PASSWORD)
                .doesNotContain(AdminFixture.MASTER_KEY_B64).doesNotContain(auth.substring("Bearer ".length()));
    }

    @Test
    void toStringsHideValues() {
        assertThat(new SecretValueRequest(SECRET).toString()).doesNotContain(SECRET);
        assertThat(new SessionRequest(USER, PASSWORD, "123456").toString()).doesNotContain(PASSWORD).doesNotContain("123456");
        assertThat(MasterKey.parse(AdminFixture.MASTER_KEY_B64).toString()).doesNotContain(AdminFixture.MASTER_KEY_B64);
        assertThat(new AdminProperties(USER, AdminFixture.HASH, List.of()).toString()).doesNotContain(AdminFixture.HASH);
    }

    private String dump(String collection) {
        StringBuilder out = new StringBuilder();
        mongo.getCollection(collection).find().forEach(d -> out.append(d.toJson()));
        return out.toString();
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString() + result.getResponse().getHeaderNames().stream()
                .map(h -> result.getResponse().getHeader(h)).toList();
    }
}
