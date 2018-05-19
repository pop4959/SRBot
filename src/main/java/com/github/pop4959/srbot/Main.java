package com.github.pop4959.srbot;

import com.github.pop4959.srbot.command.BotCommand;
import com.github.pop4959.srbot.command.BotCommandHandler;
import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

import javax.security.auth.login.LoginException;
import java.util.Set;

public class Main extends ListenerAdapter {

    private static JDA jda;
    private static SteamWebApiClient client;

    public static void main(String[] arguments) {
        Reflections reflections = new Reflections(ClasspathHelper.forClass(Main.class));
        Set<Class<? extends BotCommand>> commands = reflections.getSubTypesOf(BotCommand.class);
        Set<Class<? extends ListenerAdapter>> listeners = reflections.getSubTypesOf(ListenerAdapter.class);
        try {
            JDABuilder jdabuilder = new JDABuilder(AccountType.BOT).setToken(Data.fromFile(Data.config().getFiles().getDiscordToken())).setAutoReconnect(true).setGame(Game.of(Game.GameType.LISTENING, Data.config().getGameName()));
            for (Class<? extends ListenerAdapter> listener : listeners) {
                jdabuilder.addEventListener(listener.newInstance());
            }
            jda = jdabuilder.buildAsync();
            for (Class<? extends BotCommand> command : commands) {
                BotCommand instance = command.newInstance();
                BotCommandHandler.registerCommand(instance.getName().toLowerCase(), instance);
            }
        } catch (LoginException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        client = new SteamWebApiClient(Data.fromFile(Data.config().getFiles().getSteamToken()));
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
