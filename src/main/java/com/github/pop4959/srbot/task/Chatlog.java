package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.Logger;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Chatlog extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Logger.log(dateFormat.format(new Date(System.currentTimeMillis())) + " | " + (event.getChannelType() == ChannelType.TEXT ? event.getChannel().getName() : event.getChannel().getId()) + " | " + event.getAuthor().getName() + ": " + event.getMessage().getContent(), "cf");
    }

}
