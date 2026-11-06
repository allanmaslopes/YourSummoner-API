package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.ChampionMastery;
import com.your.summoner.domain.model.LeagueEntry;
import com.your.summoner.domain.model.Match;
import com.your.summoner.domain.model.SummonerDetails;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotAccountDto;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotChampionMasteryDto;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotLeagueEntryDto;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotMatchDto;
import com.your.summoner.infrastructure.adapter.out.riot.dto.RiotSummonerDto;

import java.util.List;

/** DTOs da Riot → domínio. */
@org.mapstruct.Mapper(componentModel = "spring")
public abstract class RiotMapper {

    public abstract Account toDomain(RiotAccountDto dto);

    public abstract SummonerDetails toDomain(RiotSummonerDto dto);

    public abstract LeagueEntry toDomain(RiotLeagueEntryDto dto);

    public abstract ChampionMastery toDomain(RiotChampionMasteryDto dto);

    public Match toDomain(RiotMatchDto dto) {
        RiotMatchDto.Info info = dto.info();
        return new Match(
                dto.metadata() == null ? null : dto.metadata().matchId(),
                info.platformId(), info.gameCreation(), info.gameDuration(), info.gameMode(),
                info.gameType(), info.gameVersion(), info.queueId(), info.mapId(),
                info.participants() == null ? List.of() : info.participants().stream().map(RiotMapper::toDomain).toList(),
                info.teams() == null ? List.of() : info.teams().stream().map(RiotMapper::toDomain).toList());
    }

    private static Match.Participant toDomain(RiotMatchDto.Participant p) {
        return new Match.Participant(p.puuid(), p.riotIdGameName(), p.riotIdTagline(), p.teamId(),
                p.championId(), p.championName(), p.teamPosition(), p.champLevel(), p.kills(), p.deaths(),
                p.assists(), p.totalMinionsKilled(), p.neutralMinionsKilled(), p.goldEarned(),
                p.totalDamageDealtToChampions(), p.visionScore(), p.win(),
                List.of(p.item0(), p.item1(), p.item2(), p.item3(), p.item4(), p.item5(), p.item6()),
                p.summoner1Id(), p.summoner2Id(),
                p.playerSubteamId() == null ? 0 : p.playerSubteamId(),
                p.subteamPlacement() == null ? 0 : p.subteamPlacement());
    }

    private static Match.Team toDomain(RiotMatchDto.Team t) {
        RiotMatchDto.Objectives o = t.objectives();
        return new Match.Team(t.teamId(), t.win(),
                t.bans() == null ? List.of() : t.bans().stream().map(RiotMatchDto.Ban::championId).toList(),
                kills(o == null ? null : o.baron()), kills(o == null ? null : o.dragon()),
                kills(o == null ? null : o.tower()), kills(o == null ? null : o.champion()));
    }

    private static int kills(RiotMatchDto.Objective objective) {
        return objective == null ? 0 : objective.kills();
    }
}
