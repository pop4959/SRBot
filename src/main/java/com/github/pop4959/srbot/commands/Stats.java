package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import com.github.pop4959.srbot.utils.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUserStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.github.pop4959.srbot.constants.CommandFields.STEAM_ID_FIELD_NAME;
import static com.github.pop4959.srbot.constants.CommandFields.STEAM_ID_FIELD_DESC;

public class Stats extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;

    public Stats(Config config, SteamWebApiClient steamWebApiClient) {
        super("stats", "Get user stats");
        this.config = config;
        this.steamWebApiClient = steamWebApiClient;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands
            .slash(name, description)
            .addOption(OptionType.STRING, STEAM_ID_FIELD_NAME, STEAM_ID_FIELD_DESC, true);
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

        var steamProfile = Utils.getSteamProfile(steamId, steamWebApiClient, config.queryTimeout);
        var steamUserStats = new SteamUserStats(steamWebApiClient);

        var stats = steamUserStats
            .getUserStatsForGame(Long.parseLong(steamId), config.srAppId)
            .get(config.queryTimeout, TimeUnit.MILLISECONDS)
            .getStats();

        var embeds = new ArrayList<MessageEmbed>();
        var embed = new EmbedBuilder()
            .setAuthor(
                "Stats for %s".formatted(steamProfile.getName()),
                steamProfile.getProfileUrl(),
                steamProfile.getAvatarFullUrl()
            );
        var numberFormat = NumberFormat.getInstance(Locale.CANADA);

        var localizedWin = config.localizedStats
            .stream()
            .filter(s -> s.group.equals("Wins"))
            .findFirst()
            .get()
            .items
            .getFirst();

        var wins = stats
            .stream()
            .filter(s -> s.getName().equals(localizedWin.key))
            .findFirst()
            .get()
            .getValue();

        embed.setDescription("%s: %s".formatted(localizedWin.value, numberFormat.format(wins)));
        for (var stat : config.localizedStats) {
            if (stat.group.equals("Wins")) continue; // skip wins, they are for description only

            var fieldValue = new StringBuilder();
            for (var localizedStatItem : stat.items) {
                var statValue = stats
                    .stream()
                    .filter(s -> s.getName().equals(localizedStatItem.key))
                    .findFirst()
                    .get()
                    .getValue();
                fieldValue.append("%s: %s\n".formatted(localizedStatItem.value, numberFormat.format(statValue)));
            }

            embed.addField(stat.group, fieldValue.toString().trim(), true);
        }

        embeds.add(embed.build());
        event.getHook().sendMessageEmbeds(embeds).queue();
    }
}
