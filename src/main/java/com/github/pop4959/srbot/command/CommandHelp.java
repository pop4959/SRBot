package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandHelp extends BotCommand {

    public CommandHelp() {
        super("help");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        BotCommand cmd = BotCommandHandler.getCommand(args.length > 0 && BotCommandHandler.isCommand(args[0]) ? args[0] : this.getName());
        event.getChannel().sendMessage(EmbedTemplates.plaintext("Help - " + cmd.getName(), "Usage: " + COMMAND_PREFIX + cmd.getName() + " " + cmd.getProperty("arguments") + "\nDescription: " + cmd.getProperty("description") + (cmd.getProperties("aliases") != null ? "\nAliases: " + cmd.getProperties("aliases").toString().replaceAll("[\\[\\]]", "") : "")).build()).queue();
    }

}
