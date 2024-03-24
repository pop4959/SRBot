package com.github.pop4959.srbot.services;

import com.github.pop4959.srbot.Logger;
import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;


public class GuildActivity extends ListenerAdapter {
    private final Config config;
    private final Logger logger;

    public GuildActivity(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        if (guild.getIdLong() == config.servers.main) {
            User user = event.getMember().getUser();
            logger.log(
                "%s (%s) has joined the server. [%d]".formatted(
                    user.getName(),
                    user.getId(),
                    guild.getMembers().size()
                ),
                "ctf"
            );
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        if (guild.getIdLong() == config.servers.main) {
            User user = event.getMember().getUser();
            logger.log(
                "%s (%s) has left the server. [%d]".formatted(
                    user.getName(),
                    user.getId(),
                    guild.getMembers().size()
                ),
                "ctf"
            );
        }
    }
}
