package com.yoursummoner.api.model.match;

import java.util.List;

public class InfoMatch {

    private Long gameCreation;

    private Long gameDuration;

    private String gameMode;

    private String gameName;

    private String gameStartTimestamp;

    private String gameType;

    private String gameVersion;

    private Integer mapId;

    private List<Participants> participants;

    private String tournamentCode;

    public InfoMatch(Long gameCreation, Long gameDuration, String gameMode, String gameName, String gameStartTimestamp, String gameType, String gameVersion, Integer mapId, List<Participants> participants, String tournamentCode) {
        this.gameCreation = gameCreation;
        this.gameDuration = gameDuration;
        this.gameMode = gameMode;
        this.gameName = gameName;
        this.gameStartTimestamp = gameStartTimestamp;
        this.gameType = gameType;
        this.gameVersion = gameVersion;
        this.mapId = mapId;
        this.participants = participants;
        this.tournamentCode = tournamentCode;
    }

    public Long getGameCreation() {
        return gameCreation;
    }

    public void setGameCreation(Long gameCreation) {
        this.gameCreation = gameCreation;
    }

    public Long getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(Long gameDuration) {
        this.gameDuration = gameDuration;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameStartTimestamp() {
        return gameStartTimestamp;
    }

    public void setGameStartTimestamp(String gameStartTimestamp) {
        this.gameStartTimestamp = gameStartTimestamp;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public Integer getMapId() {
        return mapId;
    }

    public void setMapId(Integer mapId) {
        this.mapId = mapId;
    }

    public List<Participants> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participants> participants) {
        this.participants = participants;
    }

    public String getTournamentCode() {
        return tournamentCode;
    }

    public void setTournamentCode(String tournamentCode) {
        this.tournamentCode = tournamentCode;
    }
}
