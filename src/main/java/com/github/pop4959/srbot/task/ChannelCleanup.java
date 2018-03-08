package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.Logger;
import com.github.pop4959.srbot.command.CommandChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Set;

public class ChannelCleanup extends ListenerAdapter {

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        cleanup(event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        cleanup(event.getChannelLeft());
    }

    private void cleanup(VoiceChannel channel) {
        if (channel.getMembers().size() == 0) {
            Set<VoiceChannel> channels = CommandChannel.getChannels();
            if (channels.contains(channel)) {
                channels.remove(channel);
                Logger.log("Channel '" + channel.getName() + "' was deleted.", "ctf");
                channel.delete().queue();
            }
        }
    }
}
