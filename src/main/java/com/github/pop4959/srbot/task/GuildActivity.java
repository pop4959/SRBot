package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.Logger;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class GuildActivity extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Logger.log(event.getMember().getAsMention() + " has joined the server. [" + event.getGuild().getMembers().size() + "]", "ctf");
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        Logger.log(event.getMember().getAsMention() + " has left the server. [" + event.getGuild().getMembers().size() + "]", "ctf");
    }

}
