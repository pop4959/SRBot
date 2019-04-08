package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;

public class CommandChannel extends BotCommand {

    private static Set<VoiceChannel> channels = new HashSet<>();
    private static LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

    public CommandChannel() {
        super("channel");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        int len = args.length;
        if (len > 0) {
            StringBuilder channelNameBuilder = new StringBuilder();
            int channelSize = 0;
            if (len > 1) {
                try {
                    channelSize = Integer.parseInt(args[len - 1]);
                    if (channelSize < 0 || channelSize > 99) {
                        channelSize = 0;
                    }
                    for (int i = 0; i < len - 1; ++i) {
                        channelNameBuilder.append(args[i]).append(i != (len - 1) ? " " : "");
                    }
                } catch (NumberFormatException e) {
                    for (int i = 0; i < len; ++i) {
                        channelNameBuilder.append(args[i]).append(i != (len - 1) ? " " : "");
                    }
                }
            } else {
                channelNameBuilder.append(args[0]);
            }
            if (event.getMember().getVoiceState().getChannel() != null) {
                Consumer<Channel> channelCallback = (channel) -> {
                    channels.add((VoiceChannel) channel);
                    event.getGuild().getController().moveVoiceMember(event.getMember(), (VoiceChannel) channel).queue();
                };
                try {
                    String channelName = channelNameBuilder.toString().trim();
                    event.getGuild().getController().createVoiceChannel(channelName)
                            .setParent(event.getJDA().getCategoryById(Data.config().getVoiceCategory()))
                            .setUserlimit(channelSize).queue(channelCallback);
                    event.getChannel().sendMessage(
                            String.format("%s \"%s\"%s", channelName, LANGUAGE.get("channelSuccess"),
                                    channelSize == 0 ? "" : String.format(" (User limit: %d)", channelSize))).queue();
                } catch (IllegalArgumentException e) {
                    event.getChannel().sendMessage(LANGUAGE.get("channelLen")).queue();
                }
            } else {
                event.getChannel().sendMessage(LANGUAGE.get("channelCon")).queue();
            }
        } else {
            event.getChannel().sendMessage(LANGUAGE.get("channelName")).queue();
        }
    }

    public static Set<VoiceChannel> getChannels() {
        return channels;
    }

}
