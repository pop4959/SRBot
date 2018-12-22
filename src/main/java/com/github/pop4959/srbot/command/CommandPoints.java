package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.core.exceptions.BadRequestException;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class CommandPoints extends BotCommand {

    private static final Pair<String, Integer>[] BOUNDARIES = new Pair[]{Pair.of("Entry", 0), Pair.of("Beginner", 8000), Pair.of("Advanced", 9000), Pair.of("Expert", 10000), Pair.of("Bronze", 12000), Pair.of("Silver", 17000), Pair.of("Gold", 20000), Pair.of("Platinum", 24000), Pair.of("Diamond", 29000)};
    private static final List<String> RANK_EMOTES = Data.config().getRankEmotes();
    private static final String KOS = Data.config().getKingOfSpeedSteam();

    public CommandPoints() {
        super("points");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("Please provide a Steam ID or vanity URL.").queue();
            return;
        }
        SteamUser user = new SteamUser(Main.getClient());
        String id = Steam.resolveID64(user, args[0]);
        if (id == null) {
            event.getChannel().sendMessage("Invalid Steam ID or vanity URL provided.").queue();
            return;
        }
        URL currentSeasonUrl, oldSeasonUrl;
        try {
            currentSeasonUrl = new URL("http://api.speedrunners.doubledutchgames.com/GetRanking?id=" + id);
            oldSeasonUrl = new URL("http://api.speedrunners.doubledutchgames.com/GetSeasonHistory?id=" + id);
        } catch (MalformedURLException e) {
            event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
            return;
        }
        URLConnection currentSeasonConn, oldSeasonConn;
        try {
            currentSeasonConn = currentSeasonUrl.openConnection();
            oldSeasonConn = oldSeasonUrl.openConnection();
            if (currentSeasonConn == null || oldSeasonConn == null) throw new NullPointerException();
        } catch (Exception e) {
            event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
            return;
        }
        String currentSeasonJson, oldSeasonJson;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentSeasonConn.getInputStream(), StandardCharsets.UTF_8))) {
            currentSeasonJson = reader.lines().collect(Collectors.joining("\n"));
            if (currentSeasonJson == null) throw new NullPointerException();
            if (!currentSeasonJson.contains("{")) throw new IllegalArgumentException();
        } catch (Exception e) {
            event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(oldSeasonConn.getInputStream(), StandardCharsets.UTF_8))) {
            oldSeasonJson = reader.lines().collect(Collectors.joining("\n"));
            if (oldSeasonJson == null) throw new NullPointerException();
            if (!oldSeasonJson.contains("{")) throw new IllegalArgumentException();
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
        JSONObject currentSeasonData = new JSONObject(currentSeasonJson);
        boolean isCurrentSeason = currentSeasonData.getInt("season") == 4;
        int currentEloTier = 0;
        while (currentEloTier < 8 && BOUNDARIES[currentEloTier + 1].getRight() <= currentSeasonData.getDouble("rating")) {
            currentEloTier++;
        }
        StringBuilder embedMessage = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        addSeason(embedMessage, "Off-Season", RANK_EMOTES.get(id.equals(KOS) ? 9 : currentSeasonData.getInt("tier")), BOUNDARIES[currentSeasonData.getInt("tier")].getLeft(), Integer.toString(currentSeasonData.getInt("score")));
        JSONArray oldSeasonDataArray = new JSONArray(oldSeasonJson);
        for (Object o : oldSeasonDataArray) {
            try {
                JSONObject oldSeasonData = (JSONObject) o;
                try {
                    switch (oldSeasonData.getInt("seasonid")) {
                        case 2:
                            int season2EloTier = 0;
                            while (season2EloTier < 8 && (BOUNDARIES[season2EloTier + 1].getRight() / 10) <= oldSeasonData.getDouble("score")) {
                                season2EloTier++;
                            }
                            addSeason(embedMessage, "Beta Season", RANK_EMOTES.get(id.equals(KOS) ? 9 : season2EloTier), BOUNDARIES[season2EloTier].getLeft(), decimalFormat.format(oldSeasonData.getDouble("score")));
                            break;
                        case 3:
                            int season3EloTier = 0;
                            while (season3EloTier < 8 && BOUNDARIES[season3EloTier + 1].getRight() <= oldSeasonData.getDouble("score")) {
                                season3EloTier++;
                            }
                            addSeason(embedMessage, "Winter Season", RANK_EMOTES.get(id.equals(KOS) ? 9 : season3EloTier), BOUNDARIES[season3EloTier].getLeft(), decimalFormat.format(oldSeasonData.getDouble("score")));
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                }
            } catch (ClassCastException e) {
            }
        }
        if (isCurrentSeason) {
            addSeason(embedMessage, "Christmas Season", RANK_EMOTES.get(id.equals(KOS) ? 9 : currentEloTier), BOUNDARIES[currentEloTier].getLeft(), decimalFormat.format(currentSeasonData.getDouble("rating")));
        }
        event.getChannel().sendMessage(EmbedTemplates.points(event.getGuild(), embedMessage.toString(), "Points for " + steamProfile.getName(), steamProfile.getProfileUrl(), steamProfile.getAvatarFullUrl()).build()).queue();
    }

    private static void addSeason(StringBuilder embedMessage, String season, String emote, String league, String points) {
        embedMessage.append(season);
        embedMessage.append(" ");
        embedMessage.append(emote);
        embedMessage.append(" ");
        embedMessage.append(league);
        embedMessage.append(" League (");
        embedMessage.append(points);
        embedMessage.append(" points)\n");
    }

}
