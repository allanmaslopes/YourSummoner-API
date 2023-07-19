package com.yoursummoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Spell {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private String tooltip;

    @JsonProperty
    private LevelTip levelTip;

    @JsonProperty
    private Integer maxrank;

    @JsonProperty
    private List<Integer> cooldown;

    @JsonProperty
    private String cooldownBurn;

    @JsonProperty
    private List<Integer> cost;

    @JsonProperty
    private String costBurn;

    @JsonProperty(namespace = "datavalues")
    private Object dataValues;

    @JsonProperty
    private List<Integer> effect;

    @JsonProperty
    private List<String> effectBurn;

    @JsonProperty
    private List<Object> vars;

    @JsonProperty
    private String costType;

    @JsonProperty(namespace = "maxammo")
    private String maxAmmo;

    @JsonProperty
    private List<Integer> range;

    @JsonProperty
    private String rangeBurn;

    @JsonProperty
    private Image image;

    @JsonProperty
    private String resource;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public LevelTip getLevelTip() {
        return levelTip;
    }

    public void setLevelTip(LevelTip levelTip) {
        this.levelTip = levelTip;
    }

    public Integer getMaxrank() {
        return maxrank;
    }

    public void setMaxrank(Integer maxrank) {
        this.maxrank = maxrank;
    }

    public List<Integer> getCooldown() {
        return cooldown;
    }

    public void setCooldown(List<Integer> cooldown) {
        this.cooldown = cooldown;
    }

    public String getCooldownBurn() {
        return cooldownBurn;
    }

    public void setCooldownBurn(String cooldownBurn) {
        this.cooldownBurn = cooldownBurn;
    }

    public List<Integer> getCost() {
        return cost;
    }

    public void setCost(List<Integer> cost) {
        this.cost = cost;
    }

    public String getCostBurn() {
        return costBurn;
    }

    public void setCostBurn(String costBurn) {
        this.costBurn = costBurn;
    }

    public Object getDataValues() {
        return dataValues;
    }

    public void setDataValues(Object dataValues) {
        this.dataValues = dataValues;
    }

    public List<Integer> getEffect() {
        return effect;
    }

    public void setEffect(List<Integer> effect) {
        this.effect = effect;
    }

    public List<String> getEffectBurn() {
        return effectBurn;
    }

    public void setEffectBurn(List<String> effectBurn) {
        this.effectBurn = effectBurn;
    }

    public List<Object> getVars() {
        return vars;
    }

    public void setVars(List<Object> vars) {
        this.vars = vars;
    }

    public String getCostType() {
        return costType;
    }

    public void setCostType(String costType) {
        this.costType = costType;
    }

    public String getMaxAmmo() {
        return maxAmmo;
    }

    public void setMaxAmmo(String maxAmmo) {
        this.maxAmmo = maxAmmo;
    }

    public List<Integer> getRange() {
        return range;
    }

    public void setRange(List<Integer> range) {
        this.range = range;
    }

    public String getRangeBurn() {
        return rangeBurn;
    }

    public void setRangeBurn(String rangeBurn) {
        this.rangeBurn = rangeBurn;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
