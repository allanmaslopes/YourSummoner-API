package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.domain.exception.ExternalAuthenticationException;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RiotErrorDecoderTest {

    private static final String LEAKED_SECRET = "RGAPI-vazou-1234";
    private final RiotErrorDecoder decoder = new RiotErrorDecoder(new ObjectMapper());

    @Test
    void authenticationFailureDoesNotIncludeTheUpstreamBody() {
        var exception = decoder.decode("RiotClient#summoner", response(401));

        assertThat(exception)
                .isInstanceOf(ExternalAuthenticationException.class)
                .hasMessage("Riot API answered 401");
        assertThat(exception.getMessage()).doesNotContain("RGAPI");
    }

    @Test
    void serverFailureDoesNotIncludeTheUpstreamBody() {
        var exception = decoder.decode("RiotClient#summoner", response(500));

        assertThat(exception)
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessage("Riot API answered 500");
        assertThat(exception.getMessage()).doesNotContain("RGAPI");
    }

    private static Response response(int status) {
        var request = Request.create(Request.HttpMethod.GET, "https://riot.invalid/test", Map.of(), null,
                StandardCharsets.UTF_8, null);
        return Response.builder()
                .status(status)
                .reason("upstream failure")
                .request(request)
                .body("{\"status\":{\"message\":\"" + LEAKED_SECRET + "\"}}", StandardCharsets.UTF_8)
                .build();
    }
}
