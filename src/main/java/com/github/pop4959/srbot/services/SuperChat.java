package com.github.pop4959.srbot.services;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SuperChat extends ListenerAdapter {
    private final HashMap<String, String> RESPONSES = new HashMap<>() {
        {
            put("hi", "hello");
            put("hello", "hi");
            put("11", "Wow!");
            put("12", "Well played!");
            put("13", "Nice move!");
            put("21", "Good luck!");
            put("22", "Good game!");
            put("23", "Goodbye!");
            put("31", "Thanks!");
            put("32", "Haha!");
            put("33", "Grrr!");
            put("41", "Oops!");
            put("42", "Sorry!");
            put("43", "Nooooo!");
        }
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (!event.getAuthor().isBot()) {
            if (RESPONSES.containsKey(message)) {
                event.getChannel().sendMessage(RESPONSES.get(message)).queue();
            }
        }
    }
}
