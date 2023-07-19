package com.yoursummoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ChampionDetail {

    @JsonProperty
    private String id;

    @JsonProperty
    private String key;

    @JsonProperty
    private String name;

    @JsonProperty
    private String title;

    @JsonProperty
    private Image image;

    @JsonProperty
    private List<Skin> skins;

    @JsonProperty
    private String lore;

    @JsonProperty
    private String blurb;

    @JsonProperty
    private List<String> allytips;

    @JsonProperty
    private List<String> enemyTips;

    @JsonProperty
    private List<String> tags;

    @JsonProperty
    private String partype;

    @JsonProperty
    private Info info;

    @JsonProperty
    private Stats stats;

    @JsonProperty
    private Passive passive;

    @JsonProperty
    private List<Object> recommended;

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

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Skin> getSkins() {
        return skins;
    }

    public void setSkins(List<Skin> skins) {
        this.skins = skins;
    }

    public String getLore() {
        return lore;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public List<String> getAllytips() {
        return allytips;
    }

    public void setAllytips(List<String> allytips) {
        this.allytips = allytips;
    }

    public List<String> getEnemyTips() {
        return enemyTips;
    }

    public void setEnemyTips(List<String> enemyTips) {
        this.enemyTips = enemyTips;
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

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Passive getPassive() {
        return passive;
    }

    public void setPassive(Passive passive) {
        this.passive = passive;
    }

    public List<Object> getRecommended() {
        return recommended;
    }

    public void setRecommended(List<Object> recommended) {
        this.recommended = recommended;
    }
}
