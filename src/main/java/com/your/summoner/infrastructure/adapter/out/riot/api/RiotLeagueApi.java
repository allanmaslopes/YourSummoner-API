package com.your.summoner.infrastructure.adapter.out.riot.api;

import com.your.summoner.infrastructure.adapter.out.riot.RiotFeignConfig;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotLeagueEntryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.List;

@FeignClient(name = "riot-league", url = "https://api.riotgames.com", configuration = RiotFeignConfig.class)
public interface RiotLeagueApi {
    @GetMapping("/lol/league/v4/entries/by-puuid/{puuid}")
    List<RiotLeagueEntryDto> byPuuid(URI baseUri, @PathVariable("puuid") String puuid);
}
