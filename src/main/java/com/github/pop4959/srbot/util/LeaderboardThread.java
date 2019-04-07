package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.data.Data;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LeaderboardThread extends Thread{

    private final JSONObject object;
    private final SteamUser user;
    private final SteamPlayerProfile steamProfile;
    private String message = null;
    private final List<String> list;

    public LeaderboardThread(JSONObject a, SteamUser b, SteamPlayerProfile c, List<String> d) {
        object = a;
        user = b;
        steamProfile = c;
        list = d;
    }

    @Override
    public void run() {
        try {
            SteamPlayerProfile userProfile = user.getPlayerProfile(Long.parseLong(object.getString("steamID")))
                    .get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
            String embold = steamProfile.getName().equals(userProfile.getName()) ? "**" : "";
            message = String.format("%s%d. %s%s%n", embold, object.getInt("rank"), userProfile.getName(), embold);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        synchronized (list) {
            list.add(message);
        }
    }
}
