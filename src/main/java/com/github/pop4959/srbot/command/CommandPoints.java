package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;

public class CommandPoints extends BotCommand {

    private static final Pair<String, Integer>[] BOUNDARIES = new Pair[]{
            Pair.of("Entry", 0),
            Pair.of("Beginner", 8000),
            Pair.of("Advanced", 9000),
            Pair.of("Expert", 10000),
            Pair.of("Bronze", 12000),
            Pair.of("Silver", 17000),
            Pair.of("Gold", 20000),
            Pair.of("Platinum", 24000),
            Pair.of("Diamond", 29000)
    };
    private static final List<String> RANK_EMOTES = Data.config().getRankEmotes();
    private static final String KOS = Data.config().getKingOfSpeedSteam();
    private static LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();
    private static final String[] SEASONS = {
            "Off-season",
            "Beta season",
            "Winter season",
            "Christmas season"
    };

    public CommandPoints() {
        super("points");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage(LANGUAGE.get("noId")).queue();
            return;
        }

        SteamUser user = new SteamUser(Main.getClient());
        String id = Steam.resolveID64(user, args[0]);
        if (id == null) {
            event.getChannel().sendMessage(LANGUAGE.get("wrongId")).queue();
            return;
        }

        event.getChannel().sendTyping().queue();

        URL currentSeasonUrl = Steam.getUrl(event, LANGUAGE.get("apiRank") + id),
                oldSeasonUrl = Steam.getUrl(event, LANGUAGE.get("apiSeason") + id);
        URLConnection currentSeasonConn = Steam.getConnection(event, currentSeasonUrl),
                oldSeasonConn = Steam.getConnection(event, oldSeasonUrl);
        String currentSeasonJson = Steam.getJson(event, currentSeasonConn),
                oldSeasonJson = Steam.getJson(event, oldSeasonConn);
        SteamPlayerProfile steamProfile = Steam.getSteamProfile(event, user, id);

        boolean isNull = Steam.checkNull(
                currentSeasonUrl,
                oldSeasonUrl,
                currentSeasonConn,
                oldSeasonConn,
                currentSeasonJson,
                oldSeasonJson
        );
        if (isNull) return;

        JSONObject currentSeasonData = new JSONObject(currentSeasonJson);
        int currentEloTier = 0;
        while (currentEloTier < 8 && BOUNDARIES[currentEloTier + 1].getRight() <= currentSeasonData.getDouble("rating")) {
            currentEloTier++;
        }

        StringBuilder embedMessage = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        addSeason(
                embedMessage,
                SEASONS[0],
                RANK_EMOTES.get(id.equals(KOS) ? 9 : currentSeasonData.getInt("tier")),
                BOUNDARIES[currentSeasonData.getInt("tier")].getLeft(),
                Integer.toString(currentSeasonData.getInt("score"))
        );

        JSONArray oldSeasonDataArray = new JSONArray(oldSeasonJson);
        for (Object o : oldSeasonDataArray) {
            try {
                JSONObject oldSeasonData = (JSONObject) o;
                try {
                    int season = oldSeasonData.getInt("seasonid"), tier;
                    double score = oldSeasonData.getDouble("score");
                    if (season == 2){
                        tier = 0;
                        while (tier < 8 && (BOUNDARIES[tier + 1].getRight() / 10) <= (long) score) tier++;
                        addSeason(
                                embedMessage,
                                SEASONS[1],
                                RANK_EMOTES.get(id.equals(KOS) ? 9 : tier),
                                BOUNDARIES[tier].getLeft(),
                                decimalFormat.format(score)
                        );
                    } else if (season != 1){
                        tier = 0;
                        while (tier < 8 && BOUNDARIES[tier + 1].getRight() <= score) tier++;
                        addSeason(
                                embedMessage,
                                SEASONS[season-1],
                                RANK_EMOTES.get(id.equals(KOS) ? 9 : tier),
                                BOUNDARIES[tier].getLeft(),
                                decimalFormat.format(score)
                        );
                    }
                } catch (Exception e) {
                    event.getChannel().sendMessage(LANGUAGE.get("errJson")).queue();
                }
            } catch (Exception e) {
                event.getChannel().sendMessage(LANGUAGE.get("errJson")).queue();
            }
        }
        event.getChannel().sendMessage(EmbedTemplates.points(
                event.getGuild(), embedMessage.toString(),
                String.format("Points for %s", steamProfile.getName()),
                steamProfile.getProfileUrl(), steamProfile.getAvatarFullUrl()
        ).build()).queue();
    }

    private static void addSeason(StringBuilder embedMessage, String season, String emote, String league, String points) {
        embedMessage.append(season).append(" ").append(emote).append(" ").append(league)
                .append(" League (").append(points).append(" points)\n");
    }

}