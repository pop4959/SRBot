package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUserStats;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandPlayers extends BotCommand {

    public CommandPlayers() {
        super("players");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        SteamUserStats stats = new SteamUserStats(Main.getClient());
        try {
            event.getChannel().sendMessage(
                    String.format(
                            "Current players: %d",
                            stats.getNumberOfCurrentPlayers(Data.config().getSrAppId())
                                    .get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS)
                    )
            ).queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("Unable to fetch number of players currently online.").queue();
        }
    }

}
