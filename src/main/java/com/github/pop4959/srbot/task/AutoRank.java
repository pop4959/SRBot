package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.RichPresence;
import net.dv8tion.jda.core.entities.Role;
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
        List<String> roleKeys = Data.config().getRoleKeys();
        List<Long> roleValues = Data.config().getRoleIds();
        for (int i = 0; i < roleKeys.size(); ++i) {
            ROLES.put(roleKeys.get(i), roleValues.get(i));
        }
    }

    @Override
    public void onUserUpdateGame(UserUpdateGameEvent event) {
        Guild guild = event.getGuild();
        Game game = event.getNewGame();
        if (guild.getIdLong() == SERVER && game != null && "SpeedRunners".equals(game.getName())) {
            RichPresence.Image image = game.asRichPresence().getSmallImage();
            if (image != null) {
                String roleKey = image.getKey();
                if (ROLES.containsKey(roleKey)) {
                    Member member = event.getMember();
                    long roleId = ROLES.get(roleKey);
                    String league = guild.getRoleById(roleId).getName();
                    boolean showMessage = hasRankRole(member),
                            roleChanged = manageRankRoles(event.getGuild(), member, roleId);
                    if (roleChanged) {
                        event.getGuild().getTextChannelById(CHANNEL).sendMessage(String.format("%s%s%s!",
                                member.getAsMention(),
                                showMessage ? " just reached " : " started at ",
                                league)).queue();
                    }
                }
            }
        }
    }

    public static boolean manageRankRoles(Guild guild, Member member, long newRole) {
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

    private boolean hasRankRole(Member member) {
        for (Role memberRole : member.getRoles()) {
            if (ROLES.containsValue(memberRole.getIdLong())) {
                return true;
            }
        }
        return false;
    }

}
