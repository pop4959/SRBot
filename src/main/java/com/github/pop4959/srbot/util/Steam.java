package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.data.Data;
import com.ibasco.agql.core.exceptions.BadRequestException;
import com.ibasco.agql.protocols.valve.steam.webapi.enums.VanityUrlType;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Steam {

    public static String resolveID64(SteamUser user, String input) {
        Matcher steamId = Pattern.compile("765\\d{14}+").matcher(input), vanityUrl = Pattern.compile("https?://steamcommunity\\.com/id/.+").matcher(input);
        String id = null;
        if (steamId.find()) {
            id = steamId.group();
        } else {
            if (vanityUrl.find()) {
                String match = vanityUrl.group();
                if (match.endsWith("/")) {
                    id = StringUtils.substringBetween(match, "id/", "/");
                } else {
                    id = StringUtils.substringAfter(match, "id/");
                }
            }
            try {
                Long result = user.getSteamIdFromVanityUrl(id == null ? input : id, VanityUrlType.INDIVIDUAL_PROFILE).get(Integer.parseInt(Data.fromJSON("queryTimeout")), TimeUnit.MILLISECONDS);
                if (result != null) {
                    id = result.toString();
                } else {
                    return null;
                }
            } catch (BadRequestException | InterruptedException | ExecutionException | TimeoutException e) {
                return null;
            }
        }
        return id;
    }

}
