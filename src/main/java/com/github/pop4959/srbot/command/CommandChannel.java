package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.function.Consumer;

public class CommandChannel extends BotCommand {

    private static Map<Long, VoiceChannel> channels = new HashMap<>();

    public CommandChannel() {
        super("channel");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            StringBuilder channelName = new StringBuilder();
            int channelSize = 0;
            if (args.length > 1) {
                try {
                    channelSize = Integer.parseInt(args[args.length - 1]);
                    if (channelSize < 0 || channelSize > 99)
                        channelSize = 0;
                    for (int i = 0; i < args.length - 1; i++) {
                        channelName.append(args[i] + (i != args.length - 1 ? " " : ""));
                    }
                } catch (NumberFormatException e) {
                    for (int i = 0; i < args.length; i++) {
                        channelName.append(args[i] + (i != args.length - 1 ? " " : ""));
                    }
                }
            } else {
                channelName.append(args[0]);
            }
            if (event.getMember().getVoiceState().getChannel() != null) {
                Consumer<Channel> channelCallback = (channel) -> {
                    channels.put(channel.getIdLong(), (VoiceChannel) channel);
                    event.getGuild().getController().moveVoiceMember(event.getMember(), (VoiceChannel) channel).queue();
                };
                try {
                    event.getGuild().getController().createVoiceChannel(channelName.toString()).setParent(event.getJDA().getCategoryById((String) this.getProperty("category"))).setUserlimit(channelSize).queue(channelCallback);
                } catch (IllegalArgumentException e) {
                    event.getChannel().sendMessage("The channel name you provide must be between 2 and 100 characters in length.").queue();
                }
            } else {
                event.getChannel().sendMessage("You must be connected to voice to create a custom channel.").queue();
            }
        } else {
            event.getChannel().sendMessage("You must provide the name of a channel to create!").queue();
        }
    }

    public static Map<Long, VoiceChannel> getChannels() {
        return channels;
    }

}
