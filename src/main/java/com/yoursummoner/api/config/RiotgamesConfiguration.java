package com.yoursummoner.api.config;

import com.yoursummoner.api.controller.ChampionController;
import com.yoursummoner.api.model.error.ApiError;
import com.yoursummoner.api.service.StaticWS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
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
        StaticWS.findPatch();
    }

    @Bean
    public void loadChampionsList() {
        StaticWS.findChampionsList();
    }


}
