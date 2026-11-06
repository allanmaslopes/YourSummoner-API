package com.your.summoner.infrastructure.adapter.out.riot.api;

import com.your.summoner.infrastructure.adapter.out.riot.RiotFeignConfig;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotSummonerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

@FeignClient(name = "riot-summoner", url = "https://api.riotgames.com", configuration = RiotFeignConfig.class)
public interface RiotSummonerApi {
    @GetMapping("/lol/summoner/v4/summoners/by-puuid/{puuid}")
    RiotSummonerDto byPuuid(URI baseUri, @PathVariable("puuid") String puuid);
}
