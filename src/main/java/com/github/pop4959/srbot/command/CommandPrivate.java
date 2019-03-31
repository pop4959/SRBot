package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandPrivate extends BotCommand {
	public CommandPrivate(){
		super("private");
	}

	public void execute(MessageReceivedEvent event, String[] args) {
		String url = "https://i.imgur.com/rJDeGgl.jpg";
		event.getChannel().sendMessage(EmbedTemplates.empty(event.getGuild()).setImage(url).build()).queue();
	}
}
