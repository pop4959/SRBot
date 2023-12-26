package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.data.Config;
import com.github.pop4959.srbot.models.Ranking;
import com.github.pop4959.srbot.utils.Utils;
import com.google.gson.Gson;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Points extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;
    private final String steamIdField = "steamid";

    public Points(Config config, SteamWebApiClient steamWebApiClient) {
        super("points", "get user points");
        this.config = config;
        this.steamWebApiClient = steamWebApiClient;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands
            .slash(name, description)
            .addOption(OptionType.STRING, steamIdField, "Steam ID", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws Exception {
        event
            .deferReply()
            .setEphemeral(true)
            .queue();
        var potentialSteamId = event.getOption(steamIdField, OptionMapping::getAsString);
        var steamId = Utils.resolveSteamId(potentialSteamId, steamWebApiClient, config.queryTimeout);

        var rankUrl = new URL(config.language.apiRank + steamId);
        var connection = rankUrl.openConnection();

        String json;
        try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            json = reader.lines().collect(Collectors.joining("\n"));
            if (json.isEmpty() || json.equals(config.language.privateProfileDD)) throw new IOException();
            if (!json.contains("{")) throw new IllegalArgumentException();
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        var ranking = new Gson().fromJson(json, Ranking.class);
    }
}
