package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import com.github.pop4959.srbot.utils.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamPlayerService;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.pop4959.srbot.constants.CommandFields.*;

public class Playtime extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;

    public Playtime(Config config, SteamWebApiClient steamWebApiClient) {
        super("playtime", "Get user playtime");
        this.config = config;
        this.steamWebApiClient = steamWebApiClient;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return super.getSlashCommand()
            .addOption(OptionType.STRING, STEAM_ID_FIELD_NAME, STEAM_ID_FIELD_DESC, true)
            .addOption(OptionType.STRING, UNIT_FIELD_NAME, UNIT_FIELD_DESC);
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

        var steamUser = new SteamUser(steamWebApiClient);
        var steamPlayerService = new SteamPlayerService(steamWebApiClient);
        var message = config.messages.unableData;
        try {
            var longSteamId = Long.parseLong(steamId);
            var steamPlayerProfile = steamUser
                .getPlayerProfile(longSteamId)
                .get(config.queryTimeout, TimeUnit.MILLISECONDS);
            var game = steamPlayerService
                .getOwnedGames(longSteamId, true, false)
                .get(config.queryTimeout, TimeUnit.MILLISECONDS)
                .stream()
                .filter(g -> g.getAppId() == config.srAppId)
                .findFirst()
                .get();

            var unit = event.getOption(UNIT_FIELD_NAME, OptionMapping::getAsString);
            var timeSpent = getTimeSpent(unit, game.getTotalPlaytime());
            message = "%s has played %s of SpeedRunners.".formatted(steamPlayerProfile.getName(), timeSpent);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        event.getHook().sendMessage(message).queue();
    }

    private String getTimeSpent(String unit, double minutes) {
        var df = new DecimalFormat("#.##");
        if (unit == null) {
            unit = "hours";
        }
        return switch (unit.toLowerCase()) {
            case "seconds" -> "%s %s".formatted(df.format(minutes * 60), unit);
            case "hours" -> "%s %s".formatted(df.format(minutes / 60), unit);
            case "days" -> "%s %s".formatted(df.format(minutes / 60 / 24), unit);
            case "months" -> "%s %s".formatted(df.format(minutes / 60 / 24 / 30), unit);
            case "years" -> "%s %s".formatted(df.format(minutes / 60 / 24 / 365), unit);
            default -> "%f %s".formatted(minutes, unit);
        };
    }
}
