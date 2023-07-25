package com.your.summoner.api.model.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.your.summoner.api.model.Status;


public class ErrorJsonApi {

    @JsonProperty(namespace = "status")
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
