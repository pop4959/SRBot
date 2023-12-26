package com.github.pop4959.srbot;

import com.github.pop4959.srbot.commands.*;
import com.github.pop4959.srbot.data.Config;
import com.google.gson.Gson;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;

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
            var steamWebApiClient = new SteamWebApiClient(config.files.steamToken);

            // commands
            var commands = new ArrayList<Command>();
            commands.add(new Active(config));
            commands.add(new Changelog(config));
            commands.add(new Chart(config, steamWebApiClient));
            commands.add(new Players(config, steamWebApiClient));
            commands.add(new Playtime(config, steamWebApiClient));
            commands.add(new RandomCharacter(config));

            // bot
            var bot = new Bot(commands, config, logger);
            bot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
