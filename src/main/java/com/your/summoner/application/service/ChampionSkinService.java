package com.your.summoner.application.service;

import com.your.summoner.application.port.in.ListChampionSkinsUseCase;
import com.your.summoner.domain.model.ChampionSkin;
import org.springframework.stereotype.Service;

import java.util.List;

/** Serviço à parte: {@code execute(String, String)} já é de {@link ChampionService} (detalhe), com outro retorno. */
@Service
public class ChampionSkinService implements ListChampionSkinsUseCase {

    private final ChampionCatalog catalog;

    public ChampionSkinService(ChampionCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<ChampionSkin> execute(String championName, String patch) {
        String resolved = catalog.resolve(patch);
        return catalog.skins(resolved, ChampionNames.resolveId(catalog, resolved, championName));
    }
}
