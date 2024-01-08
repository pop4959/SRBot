package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import com.github.pop4959.srbot.utils.LeaderboardThread;
import com.github.pop4959.srbot.utils.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

import static com.github.pop4959.srbot.constants.CommandFields.*;
import static com.github.pop4959.srbot.utils.Utils.httpGetJson;

public class Leaderboard extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;

    public Leaderboard(Config config, SteamWebApiClient steamWebApiClient) {
        super("leaderboard", "Get user's position on the ranking leaderboard");
        this.config = config;
        this.steamWebApiClient = steamWebApiClient;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return super.getSlashCommand()
            .addOption(OptionType.STRING, STEAM_ID_FIELD_NAME, STEAM_ID_FIELD_DESC, true)
            .addOption(OptionType.INTEGER, SEASON_FIELD_NAME, SEASON_FIELD_DESC, true);
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws Exception {
        event.deferReply(true).queue();

        var potentialSteamId = event.getOption(STEAM_ID_FIELD_NAME, OptionMapping::getAsString);
        if (potentialSteamId == null) {
            event.getHook().sendMessage(config.messages.noId).queue();
            return;
        }

        var steamId = Utils.resolveSteamId(potentialSteamId, steamWebApiClient, config.queryTimeout);
        if (steamId == null) {
            event.getHook().sendMessage(config.messages.wrongId).queue();
            return;
        }

        var season = event.getOption(SEASON_FIELD_NAME, OptionMapping::getAsInt);
        if (season == null) {
            event.getHook().sendMessage(config.messages.noSeason).queue();
            return;
        }
        if (season < 1 || season > 4) {
            event.getHook().sendMessage(config.messages.wrongSeason).queue();
            return;
        }

        var steamUser = new SteamUser(steamWebApiClient);
        var steamProfile = Utils.getSteamProfile(steamId, steamWebApiClient, config.queryTimeout);

        var leaderboardUrl = new URI("%s%d&steamid=%s".formatted(config.apiUrl.leaderboard, season, steamId)).toURL();
        var leaderboard = httpGetJson(leaderboardUrl, "", com.github.pop4959.srbot.models.Leaderboard.class).leaderboardEntryInformation;

        var size = leaderboard.leaderboardEntries.length;
        if (size == 0) {
            event.getHook().sendMessage(config.messages.noPlay).queue();
            return;
        }

        var idx = 0;
        var threads = new Thread[size];
        var list = new ArrayList<String>();

        for (var entry : leaderboard.leaderboardEntries) {
            threads[idx] = new LeaderboardThread(entry, steamUser, steamProfile, list, config.queryTimeout);
            threads[idx++].start();
        }

        for (var i = 0; i < size; i++) {
            threads[i].join();
        }

        var standard = Pattern.compile("^(\\d{1,8})\\.\\s.+");
        var current = Pattern.compile("^\\*\\*(\\d{1,8})\\.\\s.+");

        list.sort(Comparator.comparingInt(str -> {
            var matcher1 = standard.matcher(str);
            var matcher2 = current.matcher(str);

            if (matcher1.find()) {
                return Integer.parseInt(matcher1.group(1));
            } else if (matcher2.find()) {
                return Integer.parseInt(matcher2.group(1));
            } else {
                return 0;
            }
        }));

        var message = String.join("", list);

        System.out.println(message);
        var embeds = new ArrayList<MessageEmbed>();
        var embed = new EmbedBuilder()
            .setAuthor(
                "Leaderboard for %s".formatted(steamProfile.getName()),
                steamProfile.getProfileUrl(),
                steamProfile.getAvatarFullUrl()
            )
            .addField("", message, false);
        embeds.add(embed.build());
        event.getHook().sendMessageEmbeds(embeds).queue();
    }
}
