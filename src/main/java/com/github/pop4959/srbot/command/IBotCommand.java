package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public interface IBotCommand {

    void execute(MessageReceivedEvent event, String[] args);

    String getName();

    Object getProperty(String identifier);

    List<Object> getProperties(String identifier);

}
