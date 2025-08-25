package com.your.summoner.api.config;

import com.your.summoner.api.service.StaticService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RiotgamesConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(240000);
        return new RestTemplate(factory);
    }

    @Bean
    public void loadFindVersion()  {
        StaticService.findPatch();
    }

    @Bean
    public void loadChampionsList() {
        StaticService.findChampionsList();
    }

    @Bean
    public void loadProfileIconsList() {
        StaticService.findProfileIconsList();
    }


}
