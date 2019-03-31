package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.Logger;
import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class GuildActivity extends ListenerAdapter {

	private static final long SERVER = Data.config().getServers().getMain();

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild g = event.getGuild();
		if (g.getIdLong() == SERVER) {
			User u = event.getMember().getUser();
			Logger.log(
					String.format(
							"%s#%s (%s) has joined the server. [%d]",
							u.getName(),
							u.getDiscriminator(),
							u.getId(),
							g.getMembers().size()
					), "ctf"
			);
		}
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		Guild g = event.getGuild();
		if (g.getIdLong() == SERVER) {
			User u = event.getMember().getUser();
			Logger.log(
					String.format(
							"%s#%s (%s) has left the server. [%d]",
							u.getName(),
							u.getDiscriminator(),
							u.getId(),
							g.getMembers().size()
					), "ctf"
			);
		}
	}

}
