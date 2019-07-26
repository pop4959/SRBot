package com.github.pop4959.srbot.models;

public class Leaderboard {

    class LeaderboardEntryInformation {
        Integer appID;
        Integer leaderboardID;
        Integer totalLeaderBoardEntryCount;
        LeaderboardEntry[] leaderboardEntries;
    }

    private LeaderboardEntryInformation leaderboardEntryInformation;

    public Integer getEntriesLength() {
        return leaderboardEntryInformation.leaderboardEntries.length;
    }

    public LeaderboardEntry[] getEntries() {
        return leaderboardEntryInformation.leaderboardEntries;
    }
}
