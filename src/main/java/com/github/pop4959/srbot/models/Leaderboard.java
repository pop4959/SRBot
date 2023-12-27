package com.github.pop4959.srbot.models;

public class Leaderboard {
    public LeaderboardEntryInformation leaderboardEntryInformation;

    public class LeaderboardEntryInformation {
        public Integer appID;
        public Integer leaderboardID;
        public Integer totalLeaderBoardEntryCount;
        public LeaderboardEntry[] leaderboardEntries;
    }
}
