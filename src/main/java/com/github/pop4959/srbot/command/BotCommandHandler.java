package com.github.pop4959.srbot.command;

import java.util.HashMap;
import java.util.List;

public class BotCommandHandler {

	private static HashMap<String, BotCommand> commands = new HashMap<>();

	public static void registerCommand(String name, BotCommand command) {
		commands.put(name, command);
		List<String> aliases = command.getConfig().getAliases();
		if (aliases != null) {
			for (String alias : aliases) {
				commands.put(alias.toLowerCase(), command);
			}
		}
	}

	public static BotCommand getCommand(String name) {
		return commands.get(name);
	}

	public static boolean isCommand(String name) {
		return commands.containsKey(name);
	}

}
