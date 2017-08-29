package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.Logger;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Chatlog extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Logger.log((event.getChannelType() == ChannelType.TEXT ? event.getChannel().getName() : event.getChannel().getId()) + " | " + event.getAuthor().getName() + ": " + event.getMessage().getContent(), "cf");
    }

}
