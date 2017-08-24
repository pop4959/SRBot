package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class BotCommandListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContent();
        if (message.length() > BotCommand.getPrefix().length() && message.substring(0, BotCommand.getPrefix().length()).equalsIgnoreCase(BotCommand.getPrefix())) {
            String command = StringUtils.substringBefore(message.substring(BotCommand.getPrefix().length()), " ");
            if (BotCommandHandler.isCommand(command)) {
                String[] args = StringUtils.substringAfter(message, " ").split(" ");
                BotCommandHandler.getCommand(command).execute(event, args.length == 1 && args[0] == "" ? new String[]{} : args);
            }
        }
    }

}
