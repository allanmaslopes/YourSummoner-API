package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.Patch;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CatalogDocumentMapper {
    Patch toPatch(PatchDocument document);

    @Mapping(target = "championId", source = "champion.id")
    ChampionDocument toChampionDocument(String patch, String locale, Champion champion);

    @Mapping(target = "id", source = "championId")
    Champion toChampion(ChampionDocument document);

    default ChampionDetailDocument toDetailDocument(String patch, String locale, ChampionDetail detail) {
        return new ChampionDetailDocument(patch, locale, detail.id(), detail);
    }

    default ChampionDetail toDetail(ChampionDetailDocument document) {
        return document.detail();
    }

    SkinDocument toSkinDocument(String patch, String locale, ChampionSkin skin);

    ChampionSkin toSkin(SkinDocument document);

    ChampionStatisticsDocument toStatisticsDocument(String patch, Lane lane, Map<Long, Double> pickRates);
}
