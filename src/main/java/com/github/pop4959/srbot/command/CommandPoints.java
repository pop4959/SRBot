package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.models.Ranking;
import com.github.pop4959.srbot.models.Season;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.Utils;
import com.google.gson.Gson;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;

public class CommandPoints extends BotCommand {

    @SuppressWarnings("unchecked")
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
        String id = Utils.resolveID64(user, args[0]);
        if (id == null) {
            event.getChannel().sendMessage(LANGUAGE.get("wrongId")).queue();
            return;
        }

        event.getChannel().sendTyping().queue();

        URL currentSeasonUrl = Utils.getUrl(event, LANGUAGE.get("apiRank") + id),
                oldSeasonUrl = Utils.getUrl(event, LANGUAGE.get("apiSeason") + id);
        String currentSeasonJson = Utils.getJson(Utils.getConnection(event, currentSeasonUrl)),
                oldSeasonJson = Utils.getJson(Utils.getConnection(event, oldSeasonUrl));
        SteamPlayerProfile steamProfile = Utils.getSteamProfile(event, user, id);

        boolean isNull = Utils.checkNull(currentSeasonUrl, oldSeasonUrl, currentSeasonJson, oldSeasonJson);
        if (isNull) {
            event.getChannel().sendMessage(LANGUAGE.get("private")).queue();
            return;
        }

        Gson gson = new Gson();
        Ranking ranking = gson.fromJson(currentSeasonJson, Ranking.class);
        int currentEloTier = 0;
        while (currentEloTier < 8 && BOUNDARIES[currentEloTier + 1].getRight() <= ranking.getRating()) {
            currentEloTier++;
        }

        StringBuilder embedMessage = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        addSeason(embedMessage,
                SEASONS[0],
                RANK_EMOTES.get(id.equals(KOS) ? 9 : ranking.getTier()),
                BOUNDARIES[ranking.getTier()].getLeft(),
                Integer.toString(ranking.getScore()));

        boolean kos = id.equals(KOS);
        Season[] seasons = gson.fromJson(oldSeasonJson, Season[].class);
        for (Season season : seasons) {
            boolean beta = season.getSeasonid() == 2;
            double score = season.getScore();
            int seasonid = season.getSeasonid(),
                    tier = 0;

            if (seasonid == 1) {
                continue;
            }

            while (tier < 8 && BOUNDARIES[tier + 1].getRight() <= (score * (beta ? 1 : 10))) {
                ++tier;
            }

            addSeason(embedMessage, SEASONS[beta ? 1 : (seasonid - 1)],
                    RANK_EMOTES.get(kos ? 9 : tier),
                    BOUNDARIES[tier].getLeft(),
                    decimalFormat.format(score));
        }
        event.getChannel().sendMessage(EmbedTemplates.points(event.getGuild(), embedMessage.toString(),
                String.format("Points for %s", steamProfile.getName()),
                steamProfile.getProfileUrl(), steamProfile.getAvatarFullUrl()).build()).queue();
    }

    private static void addSeason(StringBuilder embedMessage, String season, String emote, String league, String points) {
        embedMessage.append(season).append(" ").append(emote).append(" ").append(league)
                .append(" League (").append(points).append(" points)\n");
    }

}