package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoRank extends ListenerAdapter {

    private static final long SERVER = Data.config().getServers().getMain();
    private static final long CHANNEL = Data.config().getMainChannel();
    private static final Map<String, Long> ROLES = new HashMap<>();
    private static final List<Long> BLACKLIST = Data.config().getAutoRankBlacklist();

    static {
        String[] roleKeys = new String[]{"348050330413563914", "348050371316547584", "348050417902682112", "348049472800161792", "348050519841046538", "348050557333798922", "348050592817479680", "348050641266147328", "348050671460941824"};
        long[] roleValues = new long[]{447856686724546571L, 447856627962609664L, 447856592503832576L, 447856504813649930L, 447856480310525953L, 447856450707128321L, 447856390359220234L, 447856348332556301L, 447856313574227977L};
        for (int i = 0; i < roleKeys.length; ++i) {
            ROLES.put(roleKeys[i], roleValues[i]);
        }
    }

    @Override
    public void onUserUpdateGame(UserUpdateGameEvent event) {
        Guild guild = event.getGuild();
        Game game = event.getNewGame();
        if (guild.getIdLong() == SERVER && "SpeedRunners".equals(game.getName())) {
            String roleKey = game.asRichPresence().getSmallImage().getKey();
            if (ROLES.containsKey(roleKey)) {
                Member member = event.getMember();
                long roleId = ROLES.get(roleKey);
                String league = guild.getRoleById(roleId).getName();
                if (manageRankRoles(event.getGuild(), member, roleId)) {
                    event.getGuild().getTextChannelById(CHANNEL).sendMessage(member.getAsMention() + " just reached " + league + "!").queue();
                }
            }
        }
    }

    private boolean manageRankRoles(Guild guild, Member member, long newRole) {
        if (BLACKLIST.contains(member.getUser().getIdLong())) {
            return false;
        }
        GuildController guildController = guild.getController();
        for (Role memberRole : member.getRoles()) {
            long memberRoleId = memberRole.getIdLong();
            if (memberRoleId == newRole) {
                return false;
            } else if (ROLES.values().contains(memberRoleId)) {
                guildController.removeSingleRoleFromMember(member, guild.getRoleById(memberRoleId)).queue();
            }
        }
        guildController.addSingleRoleToMember(member, guild.getRoleById(newRole)).queue();
        return true;
    }

}
