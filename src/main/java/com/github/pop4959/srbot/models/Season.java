package com.github.pop4959.srbot.models;

import com.google.gson.annotations.SerializedName;

public class Season {
    @SerializedName("seasonid")
    public Integer seasonId;

    public Integer tier;

    @SerializedName("gamecount")
    public Integer gameCount;

    public Double score;

    @SerializedName("iselo")
    public Boolean isElo;
}
