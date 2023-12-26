package com.github.pop4959.srbot.utils;

import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.enums.VanityUrlType;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class Utils {
    public static String resolveSteamId(String possibleSteamId, SteamWebApiClient client, int queryTimeout) {
        var steamUser = new SteamUser(client);

        var steamId64 = Pattern.compile("765\\d{14}+").matcher(possibleSteamId);
        var vanityUrl = Pattern.compile("https?://steamcommunity\\.com/id/.+").matcher(possibleSteamId);

        String steamId = null;
        if (!steamId64.find()) {
            if (vanityUrl.find()) {
                var match = vanityUrl.group();
                if (match.endsWith("/")) {
                    steamId = StringUtils.substringBetween(match, "id/", "/");
                } else {
                    steamId = StringUtils.substringAfter(match, "id/");
                }
            }
            try {
                var result = steamUser.getSteamIdFromVanityUrl(
                    steamId == null ? possibleSteamId : steamId,
                    VanityUrlType.INDIVIDUAL_PROFILE
                ).get(queryTimeout, TimeUnit.MILLISECONDS);
                if (result != null) {
                    steamId = result.toString();
                } else {
                    return null;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return null;
            }
        } else {
            steamId = steamId64.group();
        }
        return steamId;
    }
}
