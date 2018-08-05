package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BotCommandListener extends ListenerAdapter {

    private static long SERVER = Data.config().getServers().getMain();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message.length() > BotCommand.getPrefix().length() && message.substring(0, BotCommand.getPrefix().length()).equalsIgnoreCase(BotCommand.getPrefix())) {
            String commandString = StringUtils.substringBefore(message.substring(BotCommand.getPrefix().length()), " ").toLowerCase();
            if (BotCommandHandler.isCommand(commandString)) {
                String[] args = StringUtils.substringAfter(message, " ").split(" ");
                BotCommand command = BotCommandHandler.getCommand(commandString);
                if (command.getConfig().isExclusive() && event.getGuild().getIdLong() != SERVER) {
                    event.getChannel().sendMessage("That command cannot be used on this server.").queue();
                    return;
                }
                if (command.getConfig().getPermission().equalsIgnoreCase("default") || event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                    command.execute(event, args.length == 1 && args[0] == "" ? new String[]{} : args);
                }
            }
        }
    }

}
