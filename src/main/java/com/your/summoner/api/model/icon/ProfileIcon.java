package com.your.summoner.api.model.icon;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.your.summoner.api.model.champion.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileIcon {

    @JsonIgnore
    public Map<String, ProfileIcon> profileIconMap = new HashMap<>();

    @JsonProperty
    private Integer id;

    private Image image;

    //Essa notação @JsonAnySetter permite que utilize o map baseado
    //Nos getters e setters da sua classe
    @JsonAnySetter
    void setChampion(String key, ProfileIcon profileIcon) {
        //método put "cadastra" um objeto com sua chave
        profileIconMap.put(key, profileIcon);
    }
}
