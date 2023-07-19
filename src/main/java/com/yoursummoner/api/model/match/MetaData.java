package com.yoursummoner.api.model.match;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MetaData {

    @JsonProperty
    private String dataVersion;

    @JsonProperty
    private String matchId;

    @JsonProperty
    private List<String> participants;

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}
