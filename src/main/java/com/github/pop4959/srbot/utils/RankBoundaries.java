package com.github.pop4959.srbot.utils;

public class RankBoundaries {

    public final String rank;
    public final int eloSeasonBoundary;
    public final int offSeasonBoundary;

    public RankBoundaries(String rank, int eloSeasonBoundary, int offSeasonBoundary) {
        this.rank = rank;
        this.eloSeasonBoundary = eloSeasonBoundary;
        this.offSeasonBoundary = offSeasonBoundary;
    }
}
