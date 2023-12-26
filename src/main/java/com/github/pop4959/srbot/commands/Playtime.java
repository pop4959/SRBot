package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.data.Config;
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

public class Playtime extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;
    private final String steamIdField = "steamid";
    private final String unitField = "unit";

    public Playtime(Config config, SteamWebApiClient steamWebApiClient) {
        super("playtime", "Get user playtime xdd");
        this.config = config;
        this.steamWebApiClient = steamWebApiClient;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        System.out.println("xdd");
        return Commands
            .slash(name, description)
            .addOption(OptionType.STRING, "asd", "aa", true)
            .addOption(OptionType.STRING, unitField, "Time unit to display. Hours by default");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws Exception {
        var possibleSteamId = event.getOption(steamIdField, OptionMapping::getAsString);
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

            var unit = event.getOption(unitField, OptionMapping::getAsString);
            var timeSpent = getTimeSpent(unit, game.getTotalPlaytime());
            var message = "%s has played %s of SpeedRunners.".formatted(steamPlayerProfile.getName(), timeSpent);
            event
                .reply(message)
                .setEphemeral(true)
                .queue();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            event.reply(config.language.unableData).queue();
        }
    }

    private String getTimeSpent(String unit, double minutes) {
        var df = new DecimalFormat("#.##");
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
