package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandHelp extends BotCommand {

    public CommandHelp() {
        super("help");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        BotCommand cmd = args.length > 0 && BotCommandHandler.isCommand(args[0])
                ? BotCommandHandler.getCommand(args[0]) : this;
        event.getChannel().sendMessage(EmbedTemplates.plaintext(event.getGuild(),
                String.format("Help - %s", cmd.getName()),
                String.format("Usage: %s%s %s%nDescription: %s%s",
                        COMMAND_PREFIX, cmd.getName(),
                        cmd.getConfig().getArguments(),
                        cmd.getConfig().getDescription(),
                        cmd.getConfig().getAliases().isEmpty() ? "" : String.format("%nAliases: %s",
                                cmd.getConfig().getAliases().toString().replaceAll("[\\[\\]]", "")))).build()).queue();
    }

}
