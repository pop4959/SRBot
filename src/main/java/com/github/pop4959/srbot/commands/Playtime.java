package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import com.github.pop4959.srbot.utils.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamPlayerService;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.pop4959.srbot.constants.CommandFieldNames.STEAM_ID_FIELD;
import static com.github.pop4959.srbot.constants.CommandFieldNames.UNIT_FIELD;

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
        return Commands
            .slash(name, description)
            .addOption(OptionType.STRING, STEAM_ID_FIELD, "Steam ID or vanity URL", true)
            .addOption(OptionType.STRING, UNIT_FIELD, "Time unit to display. Hours by default");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws Exception {
        event.deferReply(true).queue();
        var possibleSteamId = event.getOption(STEAM_ID_FIELD, OptionMapping::getAsString);
        var steamUser = new SteamUser(steamWebApiClient);
        var steamPlayerService = new SteamPlayerService(steamWebApiClient);
        var steamId = Utils.resolveSteamId(possibleSteamId, steamWebApiClient, config.queryTimeout);
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

            var unit = event.getOption(UNIT_FIELD, OptionMapping::getAsString);
            var timeSpent = getTimeSpent(unit, game.getTotalPlaytime());
            var message = "%s has played %s of SpeedRunners.".formatted(steamPlayerProfile.getName(), timeSpent);
            event
                .getHook()
                .editOriginal(message)
                .queue();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            event
                .getHook()
                .editOriginal(config.messages.unableData)
                .queue();
        }
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
