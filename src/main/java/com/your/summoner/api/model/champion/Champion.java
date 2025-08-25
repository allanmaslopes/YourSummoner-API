package com.your.summoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Champion {

    //Map que vai servir para construir cada instancia de campeão
    //E mapear com sua própria chave
    @JsonIgnore
    public Map<String, Champion> championMap = new HashMap<>();
    //A notação JsonProperty faz exatemente o que diz!
    //Ela quem vai mapear cada chave no Json para o atributo com mesmo nome!
    @JsonProperty
    private String version;
    @JsonProperty
    private String id;
    @JsonProperty
    private String key;
    @JsonProperty
    private String name;
    @JsonProperty
    private String title;
    @JsonProperty
    private String blurb;
    @JsonProperty
    private Info info;
    @JsonProperty
    private Image image;
    @JsonProperty
    private List<String> tags;
    @JsonProperty
    private String partype;
    @JsonProperty
    private Stats stats;

    public Champion(String version, String id, String key, String name, String title, String blurb, Info info, Image image, List<String> tags, String partype, Stats stats) {
        this.version = version;
        this.id = id;
        this.key = key;
        this.name = name;
        this.title = title;
        this.blurb = blurb;
        this.info = info;
        this.image = image;
        this.tags = tags;
        this.partype = partype;
        this.stats = stats;
    }

    //Essa notação @JsonAnySetter permite que utilize o map baseado
    //Nos getters e setters da sua classe
    @JsonAnySetter
    void setChampion(String key, Champion champion) {
        //método put "cadastra" um objeto com sua chave
        championMap.put(key, champion);
    }

    //Getter do Map
    public Map<String, Champion> getchampionMap() {
        return championMap;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getPartype() {
        return partype;
    }

    public void setPartype(String partype) {
        this.partype = partype;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }
}
