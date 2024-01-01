package com.github.pop4959.srbot.services;

import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AutoRank extends ListenerAdapter {
    private final Map<String, Long> ROLES = new HashMap<>();
    private final Config config;

    public AutoRank(@NotNull Config config) {
        this.config = config;
        for (var i = 0; i < config.roleKeys.size(); i++) {
            ROLES.put(config.roleKeys.get(i), config.roleIds.get(i));
        }
    }

    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent event) {
        var guild = event.getGuild();
        var activities = event.getMember().getActivities();
        var game = activities
            .stream()
            .filter(a -> a.getType() == Activity.ActivityType.PLAYING)
            .findFirst()
            .get();
        if (guild.getIdLong() == config.servers.main && game.getName().equals("SpeedRunners")) {
            var image = Objects.requireNonNull(game.asRichPresence()).getSmallImage();
            if (image == null) return;
            var roleKey = image.getKey();

            if (ROLES.containsKey(roleKey)) {
                var member = event.getMember();
                var roleId = ROLES.get(roleKey);
                var league = guild.getRoleById(roleId).getName();
                var hasRole = hasRole(member);
                var roleChanged = manageRankRoles(event.getGuild(), member, roleId);

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
        for (var memberRole : member.getRoles()) {
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

        for (var memberRole : member.getRoles()) {
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
