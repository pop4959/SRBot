package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.Logger;
import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class GuildActivity extends ListenerAdapter {

    private static final long SERVER = Data.config().getServers().getMain();

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getGuild().getIdLong() == SERVER) {
            Logger.log(event.getMember().getAsMention() + " has joined the server. [" + event.getGuild().getMembers().size() + "]", "ctf");
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        if (event.getGuild().getIdLong() == SERVER) {
            Logger.log(event.getMember().getAsMention() + " has left the server. [" + event.getGuild().getMembers().size() + "]", "ctf");
        }
    }

}
