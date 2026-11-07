package com.your.summoner.infrastructure.config;

import com.your.summoner.application.port.out.RiotCredentials;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/** Fallback credential source; a supplied RiotCredentials bean takes precedence. */
@Configuration(proxyBeanMethods = false)
public class EnvRiotCredentials {
    @Bean
    @ConditionalOnMissingBean(RiotCredentials.class)
    RiotCredentials riotCredentials(RiotApiProperties properties) {
        return () -> Optional.ofNullable(properties.token()).filter(token -> !token.isBlank());
    }
}
