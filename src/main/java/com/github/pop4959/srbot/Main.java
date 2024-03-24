package com.github.pop4959.srbot;

import com.github.pop4959.srbot.commands.*;
import com.github.pop4959.srbot.models.Config;
import com.github.pop4959.srbot.services.AutoRank;
import com.github.pop4959.srbot.services.ChatLog;
import com.github.pop4959.srbot.services.GuildActivity;
import com.github.pop4959.srbot.services.SuperChat;
import com.google.gson.Gson;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.FileReader;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        try {
            var gson = new Gson();

            // config
            var configPath = "data/config.json";
            var config = gson.fromJson(new FileReader(configPath), Config.class);

            // logger
            var logger = new Logger(config);

            // steam
            var steamWebApiClient = new SteamWebApiClient(config.secrets.steamToken);

            // services
            var services = new ArrayList<ListenerAdapter>() {
                {
                    add(new AutoRank(config));
                    add(new GuildActivity(config, logger));
                    add(new SuperChat());
                    add(new ChatLog(logger));
                }
            };

            // commands
            var commands = new ArrayList<Command>() {
                {
                    add(new Active(config));
                    add(new Changelog(config));
                    add(new Chart(config, steamWebApiClient));
                    add(new Leaderboard(config, steamWebApiClient));
                    add(new Players(config, steamWebApiClient));
                    add(new Playtime(config, steamWebApiClient));
                    add(new Points(config, steamWebApiClient));
                    add(new Private(config));
                    add(new RandomCharacter(config));
                    add(new Say());
                    add(new Stats(config, steamWebApiClient));
                }
            };

            // bot
            var bot = new Bot(commands, services, config, logger);
            logger.setBot(bot);
            bot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
