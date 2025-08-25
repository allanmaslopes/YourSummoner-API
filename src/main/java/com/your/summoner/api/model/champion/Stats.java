package com.your.summoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Stats {

    @JsonProperty
    private Integer hp;

    @JsonProperty
    private Integer hpperlevel;

    @JsonProperty
    private Integer mp;

    @JsonProperty
    private Integer mpperlevel;

    @JsonProperty
    private Integer movespeed;

    @JsonProperty
    private Integer armor;

    @JsonProperty
    private Double armorperlevel;

    @JsonProperty
    private Integer spellblock;

    @JsonProperty
    private Double spellblockperlevel;

    @JsonProperty
    private Integer attackrange;

    @JsonProperty
    private Integer hpregen;

    @JsonProperty
    private Integer hpregenperlevel;

    @JsonProperty
    private Integer mpregen;

    @JsonProperty
    private Integer mpregenperlevel;

    @JsonProperty
    private Integer crit;

    @JsonProperty
    private Integer critperlevel;

    @JsonProperty
    private Integer attackdamage;

    @JsonProperty
    private Integer attackdamageperlevel;

    @JsonProperty
    private Double attackspeedperlevel;

    @JsonProperty
    private Double attackspeed;

    public Stats(Integer hp, Integer hpperlevel, Integer mp, Integer mpperlevel, Integer movespeed, Integer armor, Double armorperlevel, Integer spellblock, Double spellblockperlevel, Integer attackrange, Integer hpregen, Integer hpregenperlevel, Integer mpregen, Integer mpregenperlevel, Integer crit, Integer critperlevel, Integer attackdamage, Integer attackdamageperlevel, Double attackspeedperlevel, Double attackspeed) {
        this.hp = hp;
        this.hpperlevel = hpperlevel;
        this.mp = mp;
        this.mpperlevel = mpperlevel;
        this.movespeed = movespeed;
        this.armor = armor;
        this.armorperlevel = armorperlevel;
        this.spellblock = spellblock;
        this.spellblockperlevel = spellblockperlevel;
        this.attackrange = attackrange;
        this.hpregen = hpregen;
        this.hpregenperlevel = hpregenperlevel;
        this.mpregen = mpregen;
        this.mpregenperlevel = mpregenperlevel;
        this.crit = crit;
        this.critperlevel = critperlevel;
        this.attackdamage = attackdamage;
        this.attackdamageperlevel = attackdamageperlevel;
        this.attackspeedperlevel = attackspeedperlevel;
        this.attackspeed = attackspeed;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getHpperlevel() {
        return hpperlevel;
    }

    public void setHpperlevel(Integer hpperlevel) {
        this.hpperlevel = hpperlevel;
    }

    public Integer getMp() {
        return mp;
    }

    public void setMp(Integer mp) {
        this.mp = mp;
    }

    public Integer getMpperlevel() {
        return mpperlevel;
    }

    public void setMpperlevel(Integer mpperlevel) {
        this.mpperlevel = mpperlevel;
    }

    public Integer getMovespeed() {
        return movespeed;
    }

    public void setMovespeed(Integer movespeed) {
        this.movespeed = movespeed;
    }

    public Integer getArmor() {
        return armor;
    }

    public void setArmor(Integer armor) {
        this.armor = armor;
    }

    public Double getArmorperlevel() {
        return armorperlevel;
    }

    public void setArmorperlevel(Double armorperlevel) {
        this.armorperlevel = armorperlevel;
    }

    public Integer getSpellblock() {
        return spellblock;
    }

    public void setSpellblock(Integer spellblock) {
        this.spellblock = spellblock;
    }

    public Double getSpellblockperlevel() {
        return spellblockperlevel;
    }

    public void setSpellblockperlevel(Double spellblockperlevel) {
        this.spellblockperlevel = spellblockperlevel;
    }

    public Integer getAttackrange() {
        return attackrange;
    }

    public void setAttackrange(Integer attackrange) {
        this.attackrange = attackrange;
    }

    public Integer getHpregen() {
        return hpregen;
    }

    public void setHpregen(Integer hpregen) {
        this.hpregen = hpregen;
    }

    public Integer getHpregenperlevel() {
        return hpregenperlevel;
    }

    public void setHpregenperlevel(Integer hpregenperlevel) {
        this.hpregenperlevel = hpregenperlevel;
    }

    public Integer getMpregen() {
        return mpregen;
    }

    public void setMpregen(Integer mpregen) {
        this.mpregen = mpregen;
    }

    public Integer getMpregenperlevel() {
        return mpregenperlevel;
    }

    public void setMpregenperlevel(Integer mpregenperlevel) {
        this.mpregenperlevel = mpregenperlevel;
    }

    public Integer getCrit() {
        return crit;
    }

    public void setCrit(Integer crit) {
        this.crit = crit;
    }

    public Integer getCritperlevel() {
        return critperlevel;
    }

    public void setCritperlevel(Integer critperlevel) {
        this.critperlevel = critperlevel;
    }

    public Integer getAttackdamage() {
        return attackdamage;
    }

    public void setAttackdamage(Integer attackdamage) {
        this.attackdamage = attackdamage;
    }

    public Integer getAttackdamageperlevel() {
        return attackdamageperlevel;
    }

    public void setAttackdamageperlevel(Integer attackdamageperlevel) {
        this.attackdamageperlevel = attackdamageperlevel;
    }

    public Double getAttackspeedperlevel() {
        return attackspeedperlevel;
    }

    public void setAttackspeedperlevel(Double attackspeedperlevel) {
        this.attackspeedperlevel = attackspeedperlevel;
    }

    public Double getAttackspeed() {
        return attackspeed;
    }

    public void setAttackspeed(Double attackspeed) {
        this.attackspeed = attackspeed;
    }
}
