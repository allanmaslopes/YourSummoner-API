package com.your.summoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Passive {

    @JsonProperty
    public String name;

    @JsonProperty
    public String description;

    @JsonProperty
    public Image image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
