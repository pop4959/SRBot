package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandCommands extends BotCommand {

    public CommandCommands() {
        super("commands");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        List<String> commands = new ArrayList<>();
        for (String cmdName : Data.commands().getCommandConfigurationMap().keySet()) {
            if (!BotCommandHandler.getCommand(cmdName).getConfig().isHidden())
                commands.add(cmdName);
        }
        event.getChannel().sendMessage(EmbedTemplates.plaintext(event.getGuild(), "Commands", commands.toString().replaceAll("[\\[\\]]", "")).build()).queue();
    }

}
