package com.github.pop4959.srbot.services;

import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AutoRank extends ListenerAdapter {
    private final Map<String, Long> ROLES = new HashMap<>();
    private final Config config;

    public AutoRank(@NotNull Config config) {
        this.config = config;
        for (int i = 0; i < config.roleKeys.size(); i++) {
            ROLES.put(config.roleKeys.get(i), config.roleIds.get(i));
        }
    }

    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent event) {
        Guild guild = event.getGuild();
        List<Activity> activities = event.getMember().getActivities();
        Activity game = activities
            .stream()
            .filter(a -> a.getType() == Activity.ActivityType.PLAYING)
            .findFirst()
            .get();
        if (guild.getIdLong() == config.servers.main && game.getName().equals("SpeedRunners")) {
            RichPresence.Image image = Objects.requireNonNull(game.asRichPresence()).getSmallImage();
            if (image == null) return;
            String roleKey = image.getKey();

            if (ROLES.containsKey(roleKey)) {
                Member member = event.getMember();
                Long roleId = ROLES.get(roleKey);
                String league = guild.getRoleById(roleId).getName();
                boolean hasRole = hasRole(member);
                boolean roleChanged = manageRankRoles(event.getGuild(), member, roleId);

                if (roleChanged) {
                    event
                        .getGuild()
                        .getTextChannelById(config.mainChannel)
                        .sendMessage(
                            "%s%s%s!".formatted(
                                member.getAsMention(),
                                hasRole ? " just reached " : " started at ",
                                league
                            )
                        )
                        .queue();
                }
            }
        }
    }

    private boolean hasRole(@NotNull Member member) {
        for (Role memberRole : member.getRoles()) {
            if (ROLES.containsValue(memberRole.getIdLong())) {
                return true;
            }
        }
        return false;
    }

    private boolean manageRankRoles(Guild guild, @NotNull Member member, long newRole) {
        if (config.autoRankBlacklist.contains(member.getUser().getIdLong())) {
            return false;
        }

        for (Role memberRole : member.getRoles()) {
            long memberRoleId = memberRole.getIdLong();
            if (memberRoleId == newRole) {
                return false;
            } else if (ROLES.containsValue(memberRoleId)) {
                guild.removeRoleFromMember(member, guild.getRoleById(memberRoleId)).queue();
            }
        }
        guild.addRoleToMember(member, guild.getRoleById(newRole)).queue();
        return true;
    }
}
