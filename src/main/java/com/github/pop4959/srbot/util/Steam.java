package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.data.Data;
import com.ibasco.agql.core.exceptions.BadRequestException;
import com.ibasco.agql.protocols.valve.steam.webapi.enums.VanityUrlType;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Steam {

    private static final LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

    public static String resolveID64(SteamUser user, String input) {
        Matcher steamId = Pattern.compile("765\\d{14}+").matcher(input),
                vanityUrl = Pattern.compile("https?://steamcommunity\\.com/id/.+").matcher(input);
        String id = null;
        if (!steamId.find()) {
            if (vanityUrl.find()) {
                String match = vanityUrl.group();
                if (match.endsWith("/")) {
                    id = StringUtils.substringBetween(match, "id/", "/");
                } else {
                    id = StringUtils.substringAfter(match, "id/");
                }
            }
            try {
                Long result = user.getSteamIdFromVanityUrl(
                        id == null ? input : id,
                        VanityUrlType.INDIVIDUAL_PROFILE
                ).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
                if (result != null) {
                    id = result.toString();
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            id = steamId.group();
        }
        return id;
    }

    public static String getJson(MessageReceivedEvent event, URLConnection con) {
        String json = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            json = reader.lines().collect(Collectors.joining("\n"));
            if (json == null) throw new NullPointerException();
            if (!json.contains("{")) throw new IllegalArgumentException();
        } catch (Exception e) {
            event.getChannel().sendMessage(LANGUAGE.get("private")).queue();
        } return json;
    }

    public static SteamPlayerProfile getSteamProfile(MessageReceivedEvent event, SteamUser user, String id) {
        SteamPlayerProfile steamProfile = null;
        try {
            steamProfile = user.getPlayerProfile(Long.parseLong(id)).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
            if (steamProfile == null) throw new NullPointerException();
        } catch (Exception e) {
            event.getChannel().sendMessage(LANGUAGE.get("unableData")).queue();
        } return steamProfile;
    }

    public static URLConnection getConnection(MessageReceivedEvent event, URL url){
        URLConnection con = null;
        try{
            con = url.openConnection();
            if (con == null) throw new NullPointerException();
        } catch (Exception e) {
            event.getChannel().sendMessage(LANGUAGE.get("errDD")).queue();
        } return con;
    }

    public static URL getUrl(MessageReceivedEvent event, String str){
        URL url = null;
        try {
            url = new URL(str);
        } catch (Exception e){
            event.getChannel().sendMessage(LANGUAGE.get("errDD")).queue();
        } return url;
    }

    public static boolean checkNull(Object ...tab){
        for (Object o : tab) if (o == null) return true;
        return false;
    }

}
