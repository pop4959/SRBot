package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import com.github.pop4959.srbot.utils.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import static com.github.pop4959.srbot.constants.CommandFields.*;

public class Chart extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;

    public Chart(Config config, SteamWebApiClient steamWebApiClient) {
        super("chart", "View ranking points chart");
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

        var possibleSteamId = event.getOption(STEAM_ID_FIELD_NAME, OptionMapping::getAsString);
        if (possibleSteamId == null) {
            event.getHook().sendMessage(config.messages.wrongId).queue();
            return;
        }

        var steamId = Utils.resolveSteamId(possibleSteamId, steamWebApiClient, config.queryTimeout);
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

        String output;
        try {
            var chartProcessBuilder = new ProcessBuilder("node", "chart.js", steamId, season.toString());
            chartProcessBuilder.directory(new File("./scripts"));
            var chartProcess = chartProcessBuilder.start();

            var out = new BufferedReader(new InputStreamReader(chartProcess.getInputStream()));
            output = out.readLine();
            if (output == null) {
                var err = new BufferedReader(new InputStreamReader(chartProcess.getErrorStream()));
                err.lines().forEach(System.out::println);
                output = config.messages.unknownError;
            }
            System.out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
            event.getHook().sendMessage(config.messages.errChart).queue();
            return;
        }

        var outputFile = new File("scripts/%s".formatted(output));
        event.getHook().sendFiles(FileUpload.fromData(outputFile)).queue();
    }
}
