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
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

import static com.github.pop4959.srbot.constants.CommandFieldNames.SEASON_FIELD;
import static com.github.pop4959.srbot.constants.CommandFieldNames.STEAM_ID_FIELD;
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
        return Commands
            .slash(name, description)
            .addOption(OptionType.STRING, STEAM_ID_FIELD, "Steam ID or vanity URL", true)
            .addOption(OptionType.INTEGER, SEASON_FIELD, "Ranking season 1 - off season, 2 - beta season, 3 - christmas season, 4 - winter season", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws Exception {
        event.deferReply(true).queue();

        var season = event.getOption(SEASON_FIELD, OptionMapping::getAsInt);
        if (season < 1 || season > 4) {
            event.getHook().sendMessage(config.messages.wrongSeason).queue();
            return;
        }

        var potentialSteamId = event.getOption(STEAM_ID_FIELD, OptionMapping::getAsString);
        var steamId = Utils.resolveSteamId(potentialSteamId, steamWebApiClient, config.queryTimeout);
        var steamUser = new SteamUser(steamWebApiClient);
        var steamProfile = Utils.getSteamProfile(steamId, steamWebApiClient, config.queryTimeout);

        var leaderboardUrl = new URL("%s%d&steamid=%s".formatted(config.apiUrl.leaderboard, season, steamId));
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

        var message = list
            .stream()
            .reduce("", (acc, next) -> next + acc)
            .trim();

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
