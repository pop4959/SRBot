package com.github.pop4959.srbot.utils;

import com.github.pop4959.srbot.models.LeaderboardEntry;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LeaderboardThread extends Thread {
    private final LeaderboardEntry entry;
    private final SteamUser user;
    private final SteamPlayerProfile steamProfile;
    private String message = null;
    private final List<String> list;
    private final int queryTimeout;

    public LeaderboardThread(
        LeaderboardEntry a,
        SteamUser b,
        SteamPlayerProfile c,
        List<String> d,
        int queryTimeout
    ) {
        entry = a;
        user = b;
        steamProfile = c;
        list = d;
        this.queryTimeout = queryTimeout;
    }

    @Override
    public void run() {
        try {
            var userProfile = user
                .getPlayerProfile(Long.parseLong(entry.steamID))
                .get(queryTimeout, TimeUnit.MILLISECONDS);
            var embold = steamProfile.getName().equals(userProfile.getName()) ? "**" : "";
            var rank = Integer.parseInt(entry.score.toString().substring(1));
            var points = NumberFormat.getInstance(Locale.UK).format(rank);
            message = String.format("%s%d. %s - %s%s%n", embold, entry.rank, userProfile.getName(), points, embold);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println(e.getMessage());
        }
        synchronized (list) {
            list.add(message);
        }
    }
}
