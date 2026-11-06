package com.your.summoner.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI yourSummonerOpenApi(@Value("${springdoc.info.title:YourSummoner-API}") String title,
                                @Value("${springdoc.info.version:3.0.0}") String version) {
        // URL relativa: a especificação exportada não depende da porta do teste.
        return new OpenAPI().servers(List.of(new Server().url("/your-summoner")))
                .info(new Info().title(title).version(version)
                        .description("API que consome a Riot Games API, o Data Dragon e o Community Dragon."));
    }
}
