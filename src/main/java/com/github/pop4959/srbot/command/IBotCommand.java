package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.CommandsData;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface IBotCommand {

	void execute(MessageReceivedEvent event, String[] args);

	String getName();

	CommandsData.CommandConfiguration getConfig();

}
