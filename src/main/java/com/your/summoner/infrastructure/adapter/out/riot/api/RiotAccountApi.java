package com.your.summoner.infrastructure.adapter.out.riot.api;

import com.your.summoner.infrastructure.adapter.out.riot.RiotFeignConfig;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotAccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

@FeignClient(name = "riot-account", url = "https://api.riotgames.com", configuration = RiotFeignConfig.class)
public interface RiotAccountApi {
    @GetMapping("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
    RiotAccountDto byRiotId(URI baseUri, @PathVariable("gameName") String gameName, @PathVariable("tagLine") String tagLine);
}
