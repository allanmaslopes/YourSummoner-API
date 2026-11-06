package com.your.summoner.application.service;

import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Champion;

/** Nome exibido ou id do Data Dragon, sem diferenciar caixa → id do campeão no patch. */
final class ChampionNames {

    private ChampionNames() {
    }

    static String resolveId(ChampionCatalog catalog, String patch, String championName) {
        if (championName == null || championName.isBlank()) {
            throw new InvalidInputException("Champion name is empty or blank");
        }
        String wanted = championName.trim();
        return catalog.champions(patch).values().stream()
                .filter(c -> c.id().equalsIgnoreCase(wanted) || c.name().equalsIgnoreCase(wanted))
                .map(Champion::id)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Champion", wanted));
    }
}
