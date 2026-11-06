package com.your.summoner.infrastructure.config;

import com.your.summoner.application.service.CatalogSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class CatalogConfig {

    @Bean
    CatalogSettings catalogSettings(@Value("${yoursummoner.locale}") String locale,
                                    @Value("${yoursummoner.patch-sync.lock-stale-after}") String lockStaleAfter) {
        return new CatalogSettings(locale, DurationStyle.detectAndParse(lockStaleAfter));
    }
}
