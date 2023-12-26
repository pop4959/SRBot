package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.data.Config;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUserStats;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Players extends Command {
    private final Config config;
    private final SteamWebApiClient steamWebApiClient;

    public Players(Config config, SteamWebApiClient steamWebApiClient) {
        super("players", "Get current player count for SpeedRunners");
        this.config = config;
        this.steamWebApiClient = steamWebApiClient;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        var stats = new SteamUserStats(steamWebApiClient);
        String message;
        try {
            var playerCount = stats
                .getNumberOfCurrentPlayers(config.srAppId)
                .get(config.queryTimeout, TimeUnit.MILLISECONDS);
            message = "Current player count: %d".formatted(playerCount);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            message = config.language.noOnline;
        }
        event
            .reply(message)
            .setEphemeral(true)
            .queue();
    }
}
