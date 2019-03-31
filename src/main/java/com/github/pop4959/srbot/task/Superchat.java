package com.github.pop4959.srbot.task;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;

public class Superchat extends ListenerAdapter {

	private static final HashMap<String, String> RESPONSES = new HashMap<>();

	static {
		RESPONSES.put("hi", "hello");
		RESPONSES.put("hello", "hi");
		RESPONSES.put("11", "Wow!");
		RESPONSES.put("12", "Well played!");
		RESPONSES.put("13", "Nice move!");
		RESPONSES.put("21", "Good luck!");
		RESPONSES.put("22", "Good game!");
		RESPONSES.put("23", "Goodbye!");
		RESPONSES.put("31", "Thanks!");
		RESPONSES.put("32", "Haha!");
		RESPONSES.put("33", "Grrr!");
		RESPONSES.put("41", "Oops!");
		RESPONSES.put("42", "Sorry!");
		RESPONSES.put("43", "Nooooo!");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw();
		if (!event.getAuthor().isBot()) {
			if (RESPONSES.containsKey(message)) {
				event.getChannel().sendMessage(RESPONSES.get(message)).queue();
			}
		}
	}

}
