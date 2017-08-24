package com.github.pop4959.srbot.command;

import java.util.HashMap;
import java.util.List;

public class BotCommandHandler {

    private static HashMap<String, BotCommand> commands = new HashMap<>();

    public static void registerCommand(String name, BotCommand command) {
        commands.put(name, command);
        List<Object> aliases = command.getProperties("aliases");
        if (aliases != null) {
            for (Object alias : aliases) {
                commands.put((String) alias, command);
            }
        }
    }

    public static BotCommand getCommand(String name) {
        return commands.get(name);
    }

    public static boolean isCommand(String name) {
        return commands.containsKey(name);
    }

}
