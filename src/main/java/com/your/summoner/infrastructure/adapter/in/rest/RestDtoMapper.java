package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.LeagueEntry;
import com.your.summoner.domain.model.MainChampion;
import com.your.summoner.domain.model.MasteredChampion;
import com.your.summoner.domain.model.Match;
import com.your.summoner.domain.model.Patch;
import com.your.summoner.domain.model.PlayerMatch;
import com.your.summoner.domain.model.SummonerProfile;
import com.your.summoner.infrastructure.adapter.in.rest.dto.ChampionDetailResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.ChampionMasteryResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.ChampionSkinResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.MatchHistoryEntryResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.MatchResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.PatchResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.SummonerProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** Domínio → DTOs de resposta. Os nomes de campo do JSON seguem a API Quarkus. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RestDtoMapper {

    @Mapping(target = "urlIcon", source = "iconUrl")
    ChampionDetailResponse toResponse(ChampionDetail detail);

    @Mapping(target = "id", source = "champion.id")
    @Mapping(target = "key", source = "champion.key")
    @Mapping(target = "championName", source = "champion.name")
    @Mapping(target = "masteryPoints", source = "mastery.championPoints")
    @Mapping(target = "masteryLevel", source = "mastery.championLevel")
    @Mapping(target = "lastPlayTime", source = "mastery.lastPlayTime")
    @Mapping(target = "tags", source = "champion.tags")
    ChampionMasteryResponse toResponse(MasteredChampion mastered);

    List<ChampionMasteryResponse> toMasteryResponses(List<MasteredChampion> mastered);

    ChampionSkinResponse toResponse(ChampionSkin skin);

    List<ChampionSkinResponse> toSkinResponses(List<ChampionSkin> skins);

    PatchResponse toResponse(Patch patch);

    List<PatchResponse> toPatchResponses(List<Patch> patches);

    @Mapping(target = "matchId", source = "match.matchId")
    @Mapping(target = "gameCreation", source = "match.gameCreation")
    @Mapping(target = "gameDuration", source = "match.gameDuration")
    @Mapping(target = "gameMode", source = "match.gameMode")
    @Mapping(target = "queueId", source = "match.queueId")
    @Mapping(target = "championId", source = "player.championId")
    @Mapping(target = "championName", source = "player.championName")
    @Mapping(target = "teamPosition", source = "player.teamPosition")
    @Mapping(target = "champLevel", source = "player.champLevel")
    @Mapping(target = "kills", source = "player.kills")
    @Mapping(target = "deaths", source = "player.deaths")
    @Mapping(target = "assists", source = "player.assists")
    @Mapping(target = "kda", expression = "java(playerMatch.player().kda())")
    @Mapping(target = "totalMinionsKilled", source = "player.totalMinionsKilled")
    @Mapping(target = "goldEarned", source = "player.goldEarned")
    @Mapping(target = "visionScore", source = "player.visionScore")
    @Mapping(target = "win", source = "player.win")
    @Mapping(target = "items", source = "player.items")
    MatchHistoryEntryResponse toResponse(PlayerMatch playerMatch);

    List<MatchHistoryEntryResponse> toHistoryResponses(List<PlayerMatch> history);

    MatchResponse toResponse(Match match);

    @Mapping(target = "kda", expression = "java(participant.kda())")
    MatchResponse.Participant toResponse(Match.Participant participant);

    @Mapping(target = "summonerName", source = "account.gameName")
    @Mapping(target = "tagLine", source = "account.tagLine")
    @Mapping(target = "puuid", source = "account.puuid")
    @Mapping(target = "urlIcon", source = "profileIconUrl")
    @Mapping(target = "level", source = "summoner.summonerLevel")
    @Mapping(target = "rankingList", source = "rankings")
    @Mapping(target = "mainChampionList", source = "mainChampions")
    SummonerProfileResponse toResponse(SummonerProfile profile);

    @Mapping(target = "tier", expression = "java(entry.displayTier())")
    @Mapping(target = "quantMatchs", expression = "java(entry.totalMatches())")
    @Mapping(target = "winRate", expression = "java(entry.winRate())")
    SummonerProfileResponse.Ranking toRanking(LeagueEntry entry);

    @Mapping(target = "id", source = "champion.champion.id")
    @Mapping(target = "key", source = "champion.champion.key")
    @Mapping(target = "championName", source = "champion.champion.name")
    @Mapping(target = "masteryPoints", source = "champion.mastery.championPoints")
    @Mapping(target = "masteryLevel", source = "champion.mastery.championLevel")
    @Mapping(target = "urlIcon", source = "iconUrl")
    @Mapping(target = "tags", source = "champion.champion.tags")
    SummonerProfileResponse.MainChampionResponse toMainChampion(MainChampion main);
}
