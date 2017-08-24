package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Data;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public abstract class BotCommand implements IBotCommand {

    private final transient String name;

    protected static final String COMMANDS_FILE = Data.fromJSON("files.commands"), COMMAND_PREFIX = Data.fromJSON("commandPrefix");

    protected BotCommand(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String getPrefix() {
        return COMMAND_PREFIX;
    }

    public Object getProperty(String identifier) {
        try {
            Object property = Data.fromJSON(COMMANDS_FILE, name + "." + identifier);
            return property;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public List<Object> getProperties(String identifier) {
        List<Object> properties = new ArrayList<>();
        try {
            for (Object o : ((JSONArray) Data.asJSON(COMMANDS_FILE, name + "." + identifier)).toList()) {
                properties.add(o);
            }
            if (properties.isEmpty()) {
                return null;
            } else {
                return properties;
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

}
