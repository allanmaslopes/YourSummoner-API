package com.your.summoner.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Partida (match-v5), com os campos relevantes para a aplicação. */
public record Match(String matchId, String platformId, long gameCreation, long gameDuration,
                    String gameMode, String gameType, String gameVersion, int queueId, int mapId,
                    List<Participant> participants, List<Team> teams) {

    public Match {
        participants = participants == null ? List.of() : List.copyOf(participants);
        teams = teams == null ? List.of() : List.copyOf(teams);
    }

    public Optional<Participant> participant(String puuid) {
        return participants.stream().filter(p -> Objects.equals(p.puuid(), puuid)).findFirst();
    }

    public record Participant(String puuid, String riotIdGameName, String riotIdTagline, int teamId,
                              int championId, String championName, String teamPosition, int champLevel,
                              int kills, int deaths, int assists, int totalMinionsKilled,
                              int neutralMinionsKilled, int goldEarned, int totalDamageDealtToChampions,
                              int visionScore, boolean win, List<Integer> items,
                              int summoner1Id, int summoner2Id, int playerSubteamId, int subteamPlacement) {

        public Participant {
            items = items == null ? List.of() : List.copyOf(items);
        }

        /** KDA clássico: (kills + assists) / max(1, deaths), com duas casas. */
        public double kda() {
            return Math.round((kills + assists) * 100.0 / Math.max(1, deaths)) / 100.0;
        }
    }

    public record Team(int teamId, boolean win, List<Integer> bannedChampionIds,
                       int baronKills, int dragonKills, int towerKills, int championKills) {

        public Team {
            bannedChampionIds = bannedChampionIds == null ? List.of() : List.copyOf(bannedChampionIds);
        }
    }
}
