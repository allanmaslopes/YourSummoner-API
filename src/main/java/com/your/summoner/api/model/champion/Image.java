package com.your.summoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Image {

    @JsonProperty
    private String full;

    @JsonProperty
    private String sprite;

    @JsonProperty
    private String group;

    @JsonProperty
    private Integer x;

    @JsonProperty
    private Integer y;

    @JsonProperty
    private Integer w;

    @JsonProperty
    private Integer h;
}
