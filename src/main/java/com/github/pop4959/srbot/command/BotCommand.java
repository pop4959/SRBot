package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.CommandsData;
import com.github.pop4959.srbot.data.Data;

public abstract class BotCommand implements IBotCommand {

    private final transient String name;

    protected static final String COMMAND_PREFIX = Data.config().getCommandPrefix();

    protected BotCommand(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String getPrefix() {
        return COMMAND_PREFIX;
    }

    public CommandsData.CommandConfiguration getConfig() {
        return Data.commands().get(this.name);
    }

}
