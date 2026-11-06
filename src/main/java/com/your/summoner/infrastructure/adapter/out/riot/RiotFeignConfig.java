package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.application.port.out.RiotCredentials;
import com.your.summoner.domain.exception.ExternalAuthenticationException;
import com.your.summoner.infrastructure.config.RiotApiProperties;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

/** Deliberately not component scanned: applies only to the five Riot clients. */
public class RiotFeignConfig {
    @Bean
    RequestInterceptor riotToken(RiotCredentials credentials) {
        return template -> {
            String token = credentials.riotApiKey().filter(value -> !value.isBlank())
                    .orElseThrow(() -> new ExternalAuthenticationException(
                            RiotErrorDecoder.SERVICE, "Riot API token is missing"));
            template.removeHeader("X-Riot-Token");
            template.header("X-Riot-Token", token);
        };
    }

    @Bean
    RiotErrorDecoder riotErrorDecoder(ObjectMapper mapper) {
        return new RiotErrorDecoder(mapper);
    }

    @Bean
    Request.Options riotTimeouts(RiotApiProperties properties) {
        return new Request.Options(properties.connectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                properties.readTimeout().toMillis(), TimeUnit.MILLISECONDS, false);
    }

    @Bean
    Retryer riotFeignRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
