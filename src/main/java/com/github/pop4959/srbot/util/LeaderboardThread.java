package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.data.Data;
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

    public LeaderboardThread(LeaderboardEntry a, SteamUser b, SteamPlayerProfile c, List<String> d) {
        entry = a;
        user = b;
        steamProfile = c;
        list = d;
    }

    @Override
    public void run() {
        try {
            SteamPlayerProfile userProfile = user.getPlayerProfile(Long.parseLong(entry.getSteamID()))
                    .get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
            String embold = steamProfile.getName().equals(userProfile.getName()) ? "**" : "";
            String points = NumberFormat.getInstance(new Locale("en", "GB")).format(entry.getPoints());
            message = String.format("%s%d. %s - %s%s%n", embold, entry.getRank(), userProfile.getName(), points, embold);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println(e.getMessage());
        }
        synchronized (list) {
            list.add(message);
        }
    }
}
