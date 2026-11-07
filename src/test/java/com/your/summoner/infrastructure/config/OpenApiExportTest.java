package com.your.summoner.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Exporta o contrato OpenAPI para {@code docs/openapi/}; rode os testes e versione o resultado. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class OpenApiExportTest {

    private static final Path DIR = Path.of("docs", "openapi");

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate rest;

    @Test
    void exportsTheSpecAsJsonAndYaml() throws Exception {
        String base = "http://localhost:" + port + "/your-summoner";
        String json = rest.getForObject(base + "/v3/api-docs", String.class);
        String yaml = rest.getForObject(base + "/v3/api-docs.yaml", String.class);

        assertThat(json).contains("/champions/{championName}/skins", "/patches", "/champions/findVersion");
        assertThat(yaml).contains("/patches:");

        var mapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
        Files.createDirectories(DIR);
        Files.writeString(DIR.resolve("openapi.json"), mapper.writeValueAsString(mapper.readTree(json)) + "\n");
        Files.writeString(DIR.resolve("openapi.yaml"), yaml);
    }
}
