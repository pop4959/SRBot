package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Data;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.util.List;

public class BotCommandListener extends ListenerAdapter {

    private static boolean RESTRICTION = (boolean) Data.asJSON("commandRestriction.enabled");
    private static List<Object> CHANNELS = ((JSONArray) Data.asJSON("commandRestriction.allowedChannels")).toList();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String message = event.getMessage().getContent();
        if (message.length() > BotCommand.getPrefix().length() && message.substring(0, BotCommand.getPrefix().length()).equalsIgnoreCase(BotCommand.getPrefix())) {
            String command = StringUtils.substringBefore(message.substring(BotCommand.getPrefix().length()), " ");
            if (BotCommandHandler.isCommand(command)) {
                if (!RESTRICTION || RESTRICTION && CHANNELS.contains(event.getChannel().getIdLong()) || event.getChannelType() == ChannelType.PRIVATE) {
                    String[] args = StringUtils.substringAfter(message, " ").split(" ");
                    BotCommandHandler.getCommand(command).execute(event, args.length == 1 && args[0] == "" ? new String[]{} : args);
                } else {
                    event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage("You're not permitted to use bot commands in #" + event.getChannel().getName() + ". Please use the designated channels instead!").queue());
                }
            }
        }

    }

}
