package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Data;
import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.ibasco.agql.core.exceptions.BadRequestException;
import com.ibasco.agql.protocols.valve.steam.webapi.enums.VanityUrlType;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandPoints extends BotCommand {

    private static final Pair<String, Integer>[] BOUNDARIES = new Pair[]{Pair.of("Entry", 0), Pair.of("Beginner", 800), Pair.of("Advanced", 900), Pair.of("Expert", 1000), Pair.of("Bronze", 1200), Pair.of("Silver", 1700), Pair.of("Gold", 2000), Pair.of("Platinum", 2400), Pair.of("Diamond", 2900)};

    public CommandPoints() {
        super("points");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            SteamUser user = new SteamUser(Main.getClient());
            Matcher steamId = Pattern.compile("765\\d{14}+").matcher(args[0]), vanityUrl = Pattern.compile("https?://steamcommunity\\.com/id/.+").matcher(args[0]);
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
                    Long result = user.getSteamIdFromVanityUrl(id == null ? args[0] : id, VanityUrlType.INDIVIDUAL_PROFILE).get(Integer.parseInt(Data.fromJSON("queryTimeout")), TimeUnit.MILLISECONDS);
                    if (result != null) {
                        id = result.toString();
                    } else {
                        event.getChannel().sendMessage("Invalid Steam ID or vanity URL provided.").queue();
                        return;
                    }
                } catch (BadRequestException | InterruptedException | ExecutionException | TimeoutException e) {
                    event.getChannel().sendMessage("Invalid Steam ID or vanity URL provided.").queue();
                    return;
                }
            }
            String jsonString = "";
            try {
                URL url = new URL("http://api.speedrunners.doubledutchgames.com/GetRanking?id=" + id);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                int character;
                while ((character = in.read()) > -1) {
                    jsonString += ((char) character);
                }
            } catch (IOException e) {
                event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
                return;
            }
            if (!jsonString.contains("{")) {
                event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
                return;
            }
            JSONObject jsonData = new JSONObject(jsonString);
            int eloTier = 0;
            while (eloTier < 8 && BOUNDARIES[eloTier + 1].getRight() < jsonData.getDouble("rating")) {
                eloTier++;
            }
            try {
                event.getChannel().sendMessage(EmbedTemplates.plaintext("Points for " + user.getPlayerProfile(Long.parseLong(id)).get(Integer.parseInt(Data.fromJSON("queryTimeout")), TimeUnit.MILLISECONDS).getName(), "Beta season: " + jsonData.getDouble("rating") + " points [" + BOUNDARIES[eloTier].getLeft() + " League]\nOff-season: " + jsonData.getInt("score") + " points [" + BOUNDARIES[jsonData.getInt("tier")].getLeft() + " League]").build()).queue();
            } catch (BadRequestException | InterruptedException | ExecutionException | TimeoutException e) {
                event.getChannel().sendMessage("Unable to retrieve data for the requested user.").queue();
                return;
            }
        } else {
            event.getChannel().sendMessage("Please provide a Steam ID or vanity URL.").queue();
        }
    }

}
