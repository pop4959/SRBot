package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class CommandChart extends BotCommand {

    private static final LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

    public CommandChart() {
        super("chart");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage(LANGUAGE.get("noId")).queue();
            return;
        } else if (args.length < 2) {
            event.getChannel().sendMessage(LANGUAGE.get("noSeason")).queue();
            return;
        }

        String id = Utils.resolveID64(new SteamUser(Main.getClient()), args[0]);
        if (id == null) {
            event.getChannel().sendMessage(LANGUAGE.get("wrongId")).queue();
            return;
        }

        String[] seasons = {"off", "beta", "winter", "christmas"};

        int season = -1;
        boolean isInt = true;

        try {
            season = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            isInt = false;
        }

        if (!isInt) {
            for (int i = 0; i < 4; ++i) {
                if (seasons[i].toLowerCase().equals(args[1].toLowerCase())) {
                    season = ++i;
                    break;
                }
            }
        }

        if (season < 1 || season > 4) {
            event.getChannel().sendMessage(LANGUAGE.get("wrongSeason")).queue();
            return;
        }

        event.getChannel().sendTyping().queue();

        String output;
        try {
            Process chart = Runtime.getRuntime().exec(String.format("node ../src/main/javascript/main.js %s %s", id, season));
            BufferedReader out = new BufferedReader(new InputStreamReader(chart.getInputStream()));
            output = out.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            event.getChannel().sendMessage(LANGUAGE.get("errPy")).queue();
            return;
        }

        Pattern pattern = Pattern.compile("charts/\\S{32}\\.png");
        if (pattern.matcher(output).matches()) {
            event.getChannel().sendFile(new File(output)).queue();
        } else {
            event.getChannel().sendMessage(output).queue();
        }

    }
}
