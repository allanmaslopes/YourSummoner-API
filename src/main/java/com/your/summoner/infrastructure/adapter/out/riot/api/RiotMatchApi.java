package com.your.summoner.infrastructure.adapter.out.riot.api;

import com.your.summoner.infrastructure.adapter.out.riot.RiotFeignConfig;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotMatchDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.List;

@FeignClient(name = "riot-match", url = "https://api.riotgames.com", configuration = RiotFeignConfig.class)
public interface RiotMatchApi {
    @GetMapping("/lol/match/v5/matches/by-puuid/{puuid}/ids")
    List<String> idsByPuuid(URI baseUri, @PathVariable("puuid") String puuid,
                          @RequestParam(value = "startTime", required = false) Long startTime,
                          @RequestParam(value = "endTime", required = false) Long endTime,
                          @RequestParam(value = "queue", required = false) Integer queue,
                          @RequestParam(value = "type", required = false) String type,
                          @RequestParam(value = "start", required = false) Integer start,
                          @RequestParam(value = "count", required = false) Integer count);

    @GetMapping("/lol/match/v5/matches/{matchId}")
    RiotMatchDto byId(URI baseUri, @PathVariable("matchId") String matchId);
}
