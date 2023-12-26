package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.data.Config;
import com.github.pop4959.srbot.utils.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.enums.VanityUrlType;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class Chart extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;
    private final String steamIdField = "steamid";
    private final String seasonField = "season";

    public Chart(Config config, SteamWebApiClient steamWebApiClient) {
        super("chart", "Create a chart");
        this.config = config;
        this.steamWebApiClient = steamWebApiClient;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands
            .slash(name, description)
            .addOption(OptionType.STRING, steamIdField, "Steam ID or vanity URL", true)
            .addOption(OptionType.INTEGER, seasonField, "Ranking season (1-4)", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws Exception {
        event.deferReply(true).queue();

        var possibleSteamId = event.getOption(steamIdField, OptionMapping::getAsString);
        var steamId = Utils.resolveSteamId(possibleSteamId, steamWebApiClient, config.queryTimeout);

        var season = event.getOption(seasonField, OptionMapping::getAsInt);
        if (season < 1 || season > 4) {
            event.reply(config.language.wrongSeason).queue();
            return;
        }

        String output;
        try {
            var chart = Runtime.getRuntime().exec(String.format("node scripts/chart.js %s %s", steamId, season));
            var out = new BufferedReader(new InputStreamReader(chart.getInputStream()));
            output = out.readLine();
            if (output == null) {
                var err = new BufferedReader(new InputStreamReader(chart.getErrorStream()));
                err.lines().forEach(l -> logger.log(l, "ct"));
                output = config.language.unknownError;
            }
        } catch (IOException e) {
            logger.log(e.getMessage(), "ct");
            event.reply(config.language.errPy).queue();
            return;
        }

        event.reply(output).queue();
    }
}
