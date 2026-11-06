package com.your.summoner.infrastructure.adapter.out.cdragon;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/** Bundle público do Community Dragon com estatísticas globais por lane. */
@FeignClient(name = "cdragon", url = "${cdragon.url:https://raw.communitydragon.org}",
        configuration = CommunityDragonFeignConfig.class)
public interface CommunityDragonApi {

    String SERVICE = "Community Dragon";

    @GetMapping("/latest/plugins/rcp-fe-lol-champion-statistics/global/default/rcp-fe-lol-champion-statistics.js")
    String championStatisticsScript();
}
