package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandChangelog extends BotCommand {

	public CommandChangelog() {
		super("changelog");
	}

	public void execute(MessageReceivedEvent event, String[] args) {
		event.getChannel().sendMessage("<http://steamcommunity.com/app/207140/discussions/2/598199244892373717/>").queue();
	}

}
