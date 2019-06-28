package com.github.pop4959.srbot.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Ranking {
    private Integer count;
    private Double rating;
    private Integer season;
    private Integer score;
    private Long time;
    private Integer tier;

    @JsonCreator
    public Ranking(@JsonProperty(value = "count") Integer count,
                   @JsonProperty(value = "rating") Double rating,
                   @JsonProperty(value = "season") Integer season,
                   @JsonProperty(value = "score") Integer score,
                   @JsonProperty(value = "time") Long time,
                   @JsonProperty(value = "tier") Integer tier) {
        this.count = count;
        this.rating = rating;
        this.season = season;
        this.score = score;
        this.time = time;
        this.tier = tier;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }
}
