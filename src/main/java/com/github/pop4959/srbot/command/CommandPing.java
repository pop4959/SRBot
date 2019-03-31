package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandPing extends BotCommand {

	public CommandPing() {
		super("ping");
	}

	public void execute(MessageReceivedEvent event, String[] args) {
		event.getChannel().sendMessage(String.format("Pong! (%dms)", event.getJDA().getPing())).queue();
	}

}
