package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.CommandsData;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandCommands extends BotCommand {

    private static long SERVER = Data.config().getServers().getMain();

    public CommandCommands() {
        super("commands");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        List<String> commands = new ArrayList<>();
        for (String cmdName : Data.commands().getCommandConfigurationMap().keySet()) {
            CommandsData.CommandConfiguration cc = BotCommandHandler.getCommand(cmdName).getConfig();
            if (!cc.isHidden() && (!cc.isExclusive() || event.getGuild().getIdLong() == SERVER))
                commands.add(cmdName);
        }
        event.getChannel().sendMessage(EmbedTemplates.plaintext(event.getGuild(), "Commands", commands.toString().replaceAll("[\\[\\]]", "")).build()).queue();
    }

}
