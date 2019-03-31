package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class CommandSay extends BotCommand {

	public CommandSay() {
		super("say");
	}

	public void execute(MessageReceivedEvent event, String[] args) {
		event.getChannel().sendMessage(StringUtils.join(args, " ")).queue();
		event.getMessage().delete().queue();
	}

}
