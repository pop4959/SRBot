package com.github.pop4959.srbot;

import com.github.pop4959.srbot.command.*;
import com.github.pop4959.srbot.task.ChannelCleanup;
import com.github.pop4959.srbot.task.Chatlog;
import com.github.pop4959.srbot.task.GuildActivity;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;

import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {

    private static JDA jda;
    private static SteamWebApiClient client;

    public static void main(String[] arguments) {
        try {
            jda = jda = new JDABuilder(AccountType.BOT).setToken(Data.fromFile(Data.fromJSON("files.discordToken"))).setAutoReconnect(true).addEventListener(new Main(), new BotCommandListener(), new Chatlog(), new GuildActivity(), new ChannelCleanup()).setGame(Game.of(Data.fromJSON("gameName"))).buildAsync();
        } catch (LoginException | RateLimitedException e) {
            e.printStackTrace();
        }
        BotCommand[] commands = {new CommandChangelog(), new CommandChannel(), new CommandCommands(), new CommandHelp(), new CommandPing(), new CommandPlayers(), new CommandPoints(), new CommandRandomcharacter()};
        for (BotCommand command : commands) {
            BotCommandHandler.registerCommand(command.getName(), command);
        }
        client = new SteamWebApiClient(Data.fromFile(Data.fromJSON("files.steamToken")));
    }

    @Override
    public void onReady(ReadyEvent event) {
        Logger.log("Connected to Discord.", "tf");
    }

    public static JDA getJda() {
        return jda;
    }

    public static SteamWebApiClient getClient() {
        return client;
    }

}
