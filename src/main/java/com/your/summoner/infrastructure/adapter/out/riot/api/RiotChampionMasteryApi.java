package com.your.summoner.infrastructure.adapter.out.riot.api;

import com.your.summoner.infrastructure.adapter.out.riot.RiotFeignConfig;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotChampionMasteryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.List;

@FeignClient(name = "riot-mastery", url = "https://api.riotgames.com", configuration = RiotFeignConfig.class)
public interface RiotChampionMasteryApi {
    @GetMapping("/lol/champion-mastery/v4/champion-masteries/by-puuid/{puuid}")
    List<RiotChampionMasteryDto> byPuuid(URI baseUri, @PathVariable("puuid") String puuid);
}
