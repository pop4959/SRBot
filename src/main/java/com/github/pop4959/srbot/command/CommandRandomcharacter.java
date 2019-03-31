package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandRandomcharacter extends BotCommand {

	public CommandRandomcharacter() {
		super("randomcharacter");
	}

	public void execute(MessageReceivedEvent event, String[] args) {
		List<String> characters = Data.config().getCharacters();
		event.getChannel().sendMessage(characters.get((int) (Math.random() * characters.size()))).queue();
	}

}
