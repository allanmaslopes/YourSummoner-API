package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.domain.exception.ExternalAuthenticationException;
import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class ProblemDetailsHandlerTest {

    private static final String LEAKED_SECRET = "RGAPI-vazou-1234";
    private final ProblemDetailsHandler handler = new ProblemDetailsHandler();

    @Test
    void externalFailureLogsMetadataWithoutTheExceptionMessage(CapturedOutput output) {
        handler.externalService(new ExternalServiceUnavailableException("Riot API", LEAKED_SECRET));
        handler.externalService(new ExternalAuthenticationException("Riot API", LEAKED_SECRET));
        handler.externalService(new ExternalBadResponseException("Riot API", LEAKED_SECRET));

        assertThat(output.getAll())
                .contains("Riot API", "UNAVAILABLE", "ExternalServiceUnavailableException",
                        "AUTHENTICATION", "ExternalAuthenticationException",
                        "BAD_RESPONSE", "ExternalBadResponseException")
                .doesNotContain("RGAPI");
    }
}
