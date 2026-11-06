package com.your.summoner.infrastructure.adapter.out.ddragon;

import com.your.summoner.infrastructure.adapter.out.ddragon.dto.DdChampionDetailListDto;
import com.your.summoner.infrastructure.adapter.out.ddragon.dto.DdChampionListDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/** Data Dragon: CDN pública de dados estáticos, sem API key. */
@FeignClient(name = "ddragon", url = "${ddragon.url:https://ddragon.leagueoflegends.com}",
        configuration = DataDragonFeignConfig.class)
public interface DataDragonApi {

    String SERVICE = "Data Dragon";

    @GetMapping("/api/versions.json")
    List<String> versions();

    @GetMapping("/cdn/{patch}/data/{locale}/champion.json")
    DdChampionListDto champions(@PathVariable("patch") String patch, @PathVariable("locale") String locale);

    @GetMapping("/cdn/{patch}/data/{locale}/champion/{championId}.json")
    DdChampionDetailListDto championDetail(@PathVariable("patch") String patch,
                                           @PathVariable("locale") String locale,
                                           @PathVariable("championId") String championId);
}
