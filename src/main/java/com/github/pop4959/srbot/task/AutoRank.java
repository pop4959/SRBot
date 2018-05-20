package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.List;

public class AutoRank extends ListenerAdapter {

    private static final long server = Data.config().getServers().getMain();
    private static final long[] rankRoleIds = new long[]{447856686724546571L, 447856627962609664L, 447856592503832576L, 447856504813649930L, 447856480310525953L, 447856450707128321L, 447856390359220234L, 447856348332556301L, 447856313574227977L};

    @Override
    public void onUserUpdateGame(UserUpdateGameEvent event) {
        Game game = event.getNewGame();
        if (event.getGuild().getIdLong() == server && "SpeedRunners".equals(game.getName())) {
            RichPresence richPresence = game.asRichPresence();
            String league = richPresence.getSmallImage().getText();
            if (league != null && league.contains("League")) {
                Member member = event.getMember();
                boolean changedRank = manageRankRoles(event.getGuild(), member, league);
                if (changedRank) {
                    event.getGuild().getTextChannelById(320646972338077697L).sendMessage(member.getAsMention() + " just reached " + league + "!").queue();
                }
            }
        }
    }

    private boolean manageRankRoles(Guild guild, Member member, String newLeague) {
        GuildController gc = guild.getController();
        if (member.getRoles().stream().filter(role -> role.getName().equals(newLeague)).count() == 1) {
            return false;
        }
        for (long roleId : rankRoleIds) {
            gc.removeSingleRoleFromMember(member, guild.getRoleById(roleId)).queue();
        }
        List<Role> newRole = guild.getRolesByName(newLeague, false);
        if (!newRole.isEmpty()) {
            gc.addSingleRoleToMember(member, newRole.get(0)).queue();
        }
        return true;
    }

}
