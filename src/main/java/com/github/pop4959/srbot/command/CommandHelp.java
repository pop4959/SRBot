package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandHelp extends BotCommand {

    public CommandHelp() {
        super("help");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        BotCommand cmd = BotCommandHandler.getCommand(args.length > 0 && BotCommandHandler.isCommand(args[0]) ? args[0] : this.getName());
        event.getChannel().sendMessage("\nUsage: " + COMMAND_PREFIX + cmd.getName() + " " + cmd.getProperty("arguments") + "\nAliases: " + (cmd.getProperties("aliases") != null ? cmd.getProperties("aliases").toString().replaceAll("[\\[\\]]", "") : "None") + "\nDescription: " + cmd.getProperty("description")).queue();
    }

}
