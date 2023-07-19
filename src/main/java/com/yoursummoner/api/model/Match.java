package com.yoursummoner.api.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoursummoner.api.model.match.InfoMatch;
import com.yoursummoner.api.model.match.MetaData;

public class Match {

    @JsonProperty("metadata")
    private MetaData metaData;

    @JsonProperty("info")
    private InfoMatch infoMatch;

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public InfoMatch getInfoMatch() {
        return infoMatch;
    }

    public void setInfoMatch(InfoMatch infoMatch) {
        this.infoMatch = infoMatch;
    }
}
