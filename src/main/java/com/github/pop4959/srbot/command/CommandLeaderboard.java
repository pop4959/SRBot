package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.core.exceptions.BadRequestException;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class CommandLeaderboard extends BotCommand {

    public CommandLeaderboard() {
        super("leaderboard");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("Please provide a Steam ID or vanity URL.").queue();
            return;
        }
        SteamUser user = new SteamUser(Main.getClient());
        String id = Steam.resolveID64(user, args[0]);
        if (id == null) {
            event.getChannel().sendMessage("Invalid Steam ID or vanity URL provided.").queue();
            return;
        }
        URL leaderboardUrl;
        try {
            leaderboardUrl = new URL("http://api.speedrunners.doubledutchgames.com/GetLeaderboard?season=4&steamid=" + id + "&start=-10&end=10");
        } catch (MalformedURLException e) {
            event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
            return;
        }
        URLConnection leaderboardConn;
        try {
            leaderboardConn = leaderboardUrl.openConnection();
            if (leaderboardConn == null) throw new NullPointerException();
        } catch (Exception e) {
            event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
            return;
        }
        String leaderboardJson;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(leaderboardConn.getInputStream(), StandardCharsets.UTF_8))) {
            leaderboardJson = reader.lines().collect(Collectors.joining("\n"));
            if (leaderboardJson == null) throw new NullPointerException();
            if (!leaderboardJson.contains("{")) throw new IllegalArgumentException();
        } catch (Exception e) {
            event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
            return;
        }
        SteamPlayerProfile steamProfile = null;
        try {
            steamProfile = user.getPlayerProfile(Long.parseLong(id)).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
            if (steamProfile == null) throw new NullPointerException();
        } catch (BadRequestException | InterruptedException | ExecutionException | TimeoutException | NullPointerException e) {
            event.getChannel().sendMessage("Unable to retrieve data for the requested user.").queue();
            return;
        }
        JSONArray leaderboardEntries = (new JSONObject(leaderboardJson)).getJSONObject("leaderboardEntryInformation").getJSONArray("leaderboardEntries");
        StringBuilder message = new StringBuilder();
        if (leaderboardEntries.length() == 0) {
            event.getChannel().sendMessage("The requested user has not played this season.").queue();
            return;
        }
        for (Object o : leaderboardEntries) {
            JSONObject entry = (JSONObject) o;
            try {
                SteamPlayerProfile userProfile = user.getPlayerProfile(Long.parseLong(entry.getString("steamID"))).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
                String embold = steamProfile.getName().equals(userProfile.getName()) ? "**" : "";
                message.append(String.format("%s%d. %s%s\n", embold, entry.getInt("rank"), userProfile.getName(), embold));
            } catch (BadRequestException | InterruptedException | ExecutionException | TimeoutException | NullPointerException e) {
            }
        }
        event.getChannel().sendMessage(EmbedTemplates.points(event.getGuild(), message.toString(), "Leaderboard for " + steamProfile.getName(), steamProfile.getProfileUrl(), steamProfile.getAvatarFullUrl()).build()).queue();
    }

}
