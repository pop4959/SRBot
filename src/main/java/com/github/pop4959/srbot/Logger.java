package com.github.pop4959.srbot;

import com.github.pop4959.srbot.data.Data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    protected static final String LOG_FILE = (
            new SimpleDateFormat("yyyy-MM-dd'.log'"))
            .format(new Date(System.currentTimeMillis()));

    public static void log(String info, String destination) {
        if (destination.contains("c")) {
            System.out.println(info);
        }
        if (destination.contains("t")) {
            Main.getJda().getGuildById(Data.config().getLogging().getServer())
                    .getTextChannelById(Data.config().getLogging().getChannel())
                    .sendMessage(info).queue();
        }
        if (destination.contains("f")) {
            try {
                File logFolder = new File("logs/");
                if (!logFolder.exists()) {
                    logFolder.mkdir();
                }
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