package com.github.pop4959.srbot.utils;

import com.google.gson.Gson;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.enums.VanityUrlType;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static @Nullable String resolveSteamId(String possibleSteamId, SteamWebApiClient client, int queryTimeout) {
        SteamUser steamUser = new SteamUser(client);

        Matcher steamId64 = Pattern.compile("765\\d{14}+").matcher(possibleSteamId);
        Matcher vanityUrl = Pattern.compile("https?://steamcommunity\\.com/id/.+").matcher(possibleSteamId);

        String steamId = null;
        if (!steamId64.find()) {
            if (vanityUrl.find()) {
                String match = vanityUrl.group();
                if (match.endsWith("/")) {
                    steamId = StringUtils.substringBetween(match, "id/", "/");
                } else {
                    steamId = StringUtils.substringAfter(match, "id/");
                }
            }
            try {
                Long result = steamUser.getSteamIdFromVanityUrl(
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

    public static SteamPlayerProfile getSteamProfile(String steamId, SteamWebApiClient client, int queryTimeout) throws ExecutionException, InterruptedException, TimeoutException {
        SteamUser steamUser = new SteamUser(client);
        return steamUser
            .getPlayerProfile(Long.parseLong(steamId))
            .get(queryTimeout, TimeUnit.MILLISECONDS);
    }

    public static <T> T httpGetJson(@NotNull URL url, String errMsg, Class<T> tClass) throws IOException {
        URLConnection connection = url.openConnection();
        String seasonJson = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            seasonJson = reader.lines().collect(Collectors.joining("\n"));
            if (seasonJson.isEmpty() || seasonJson.equals(errMsg)) throw new IOException();
            if (!seasonJson.contains("{")) throw new IllegalArgumentException();
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(seasonJson, tClass);
    }
}
