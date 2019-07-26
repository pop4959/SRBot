package com.github.pop4959.srbot.models;

public class LeaderboardEntry {
    private Integer score;
    private Integer rank;
    private String ugcid;
    private String steamID;

    public String getSteamID() {
        return steamID;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getRank() {
        return rank;
    }

    public String getUgcid() {
        return ugcid;
    }

    public Integer getTier() {
        return Character.getNumericValue(score.toString().charAt(0));
    }

    public Integer getPoints() {
        return Integer.parseInt(score.toString().substring(1));
    }
}
