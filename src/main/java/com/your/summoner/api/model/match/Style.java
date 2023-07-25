package com.your.summoner.api.model.match;

import java.util.List;

public class Style {

    private String description;

    private List<Selection> selections;

    private Integer style;

    public Style(String description, List<Selection> selections, Integer style) {
        this.description = description;
        this.selections = selections;
        this.style = style;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Selection> getSelections() {
        return selections;
    }

    public void setSelections(List<Selection> selections) {
        this.selections = selections;
    }

    public Integer getStyle() {
        return style;
    }

    public void setStyle(Integer style) {
        this.style = style;
    }
}
