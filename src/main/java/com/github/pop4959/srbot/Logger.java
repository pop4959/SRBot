package com.github.pop4959.srbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    protected static final String LOG_FILE = (new SimpleDateFormat("yyyy-MM-dd'.log'")).format(new Date(System.currentTimeMillis()));

    public static void log(String info, String destination) {
        if (destination.contains("c")) {
            System.out.println(info);
        }
        if (destination.contains("t")) {
            Main.getJda().getGuildById((Long) Data.asJSON("servers." + Data.fromJSON("logging.server"))).getTextChannelById((Long) Data.asJSON("logging.channel")).sendMessage(info).queue();
        }
        if (destination.contains("f")) {
            try {
                File log = new File("logs/" + LOG_FILE);
                if (!log.exists()) {
                    log.createNewFile();
                }
                FileWriter writer = new FileWriter(log, true);
                BufferedWriter buffered = new BufferedWriter(writer);
                buffered.append(info);
                buffered.newLine();
                buffered.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
