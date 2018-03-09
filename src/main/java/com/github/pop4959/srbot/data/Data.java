package com.github.pop4959.srbot.data;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class Data {

    private static final String CONFIG_PATH = "data/config.json";
    private static final String COMMANDS_PATH = "data/commands.json";

    private static ConfigData config;
    private static CommandsData commands;

    static {
        Gson gson = new Gson();
        try {
            config = gson.fromJson(new FileReader(new File(CONFIG_PATH)), ConfigData.class);
            commands = gson.fromJson(new FileReader(new File(COMMANDS_PATH)), CommandsData.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ConfigData config() {
        return config;
    }

    public static CommandsData commands() {
        return commands;
    }

    public static String fromFile(String path) {
        try {
            return new String(Files.readAllBytes(new File(path).toPath()), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
