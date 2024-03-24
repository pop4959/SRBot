package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import com.github.pop4959.srbot.models.Ranking;
import com.github.pop4959.srbot.models.Season;
import com.github.pop4959.srbot.models.RankBoundaries;
import com.github.pop4959.srbot.utils.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import static com.github.pop4959.srbot.constants.CommandFields.STEAM_ID_FIELD_NAME;
import static com.github.pop4959.srbot.constants.CommandFields.STEAM_ID_FIELD_DESC;
import static com.github.pop4959.srbot.utils.Utils.httpGetJson;

public class Points extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;
    private final ArrayList<RankBoundaries> RANK_BOUNDARIES = new ArrayList<>() {
        {
            add(new RankBoundaries("Diamond", 29_000, 100_000));
            add(new RankBoundaries("Platinum", 24_000, 50_000));
            add(new RankBoundaries("Gold", 20_000, 25_000));
            add(new RankBoundaries("Silver", 17_000, 10_000));
            add(new RankBoundaries("Bronze", 12_000, 5_000));
            add(new RankBoundaries("Expert", 10_000, 1_000));
            add(new RankBoundaries("Advanced", 9_000, 500));
            add(new RankBoundaries("Beginner", 8_000, 300));
            add(new RankBoundaries("Entry", 0, 0));
        }
    };
    private final ArrayList<String> SEASON_NAMES = new ArrayList<>() {
        {
            add("Off-season");
            add("Beta season");
            add("Winter season");
            add("Christmas season");
        }
    };

    public Points(Config config, SteamWebApiClient steamWebApiClient) {
        super("points", "Get user points");
        this.config = config;
        this.steamWebApiClient = steamWebApiClient;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return super.getSlashCommand()
            .addOption(OptionType.STRING, STEAM_ID_FIELD_NAME, STEAM_ID_FIELD_DESC, true);
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws Exception {
        event.deferReply(false).queue();

        String potentialSteamId = event.getOption(STEAM_ID_FIELD_NAME, OptionMapping::getAsString);
        if (potentialSteamId == null) {
            event.getHook().sendMessage(config.messages.noId).queue();
            return;
        }

        String steamId = Utils.resolveSteamId(potentialSteamId, steamWebApiClient, config.queryTimeout);
        if (steamId == null) {
            event.getHook().sendMessage(config.messages.wrongId).queue();
            return;
        }

        SteamPlayerProfile steamProfile = Utils.getSteamProfile(steamId, steamWebApiClient, config.queryTimeout);

        URL currentSeasonUrl = new URI(config.apiUrl.rank + steamId).toURL();
        URL oldSeasonUrl = new URI(config.apiUrl.season + steamId).toURL();

        Ranking ranking = httpGetJson(currentSeasonUrl, config.messages.privateProfileDD, Ranking.class);
        Season[] seasons = httpGetJson(oldSeasonUrl, config.messages.privateProfileDD, Season[].class);

        boolean isKos = Objects.equals(steamId, config.kingOfSpeedSteam);
        DecimalFormat dcFormat = new DecimalFormat("#.##");
        String messageTemplate = " %s %s League (%s)";

        ArrayList<MessageEmbed> embeds = new ArrayList<MessageEmbed>();
        EmbedBuilder embed = new EmbedBuilder()
            .setAuthor(
                steamProfile.getName(),
                steamProfile.getProfileUrl(),
                steamProfile.getAvatarFullUrl()
            )
            .setTitle("Ranking points");

        {
            // off-season
            RankBoundaries offSeasonTierPair = RANK_BOUNDARIES
                .stream()
                .filter(b -> b.offSeasonBoundary <= ranking.score)
                .findFirst()
                .orElse(RANK_BOUNDARIES.get(RANK_BOUNDARIES.size() - 1));

            int offSeasonTierIndex = RANK_BOUNDARIES.indexOf(offSeasonTierPair);
            String offSeasonSeasonName = SEASON_NAMES.get(0);
            String offSeasonRankEmoji = config.rankEmojis.get(isKos ? 9 : offSeasonTierIndex);
            String offSeasonFormattedScore = dcFormat.format(ranking.score);
            String offSeasonMessage = messageTemplate.formatted(offSeasonRankEmoji, offSeasonTierPair.rank, offSeasonFormattedScore);

            embed.addField(new MessageEmbed.Field(offSeasonSeasonName, offSeasonMessage, false));
        }

        for (Season season : seasons) {
            if (season.seasonId == 1) continue; // skip off-season, data is wrong
            boolean isBeta = season.seasonId == 2; // if beta, multiply by 10

            RankBoundaries tierPair = RANK_BOUNDARIES
                .stream()
                .filter(b -> b.eloSeasonBoundary < season.score * (isBeta ? 10 : 1))
                .findFirst()
                .orElse(RANK_BOUNDARIES.get(RANK_BOUNDARIES.size() - 1));

            int tierIndex = RANK_BOUNDARIES.indexOf(tierPair);
            String seasonName = SEASON_NAMES.get(season.seasonId - 1);
            String rankEmoji = config.rankEmojis.get(isKos ? RANK_BOUNDARIES.size() + 1 : tierIndex);
            String formattedScore = dcFormat.format(season.score);
            String message = messageTemplate.formatted(rankEmoji, tierPair.rank, formattedScore);

            embed.addField(new MessageEmbed.Field(seasonName, message, false));
        }

        embeds.add(embed.build());
        event
            .getHook()
            .sendMessageEmbeds(embeds)
            .queue();
    }
}
