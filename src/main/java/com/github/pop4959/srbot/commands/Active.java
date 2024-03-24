package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Active extends Command {
    private final Config config;

    public Active(Config config) {
        super("active", "Get active users playing SpeedRunners");
        this.config = config;
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws Exception {
        var guild = event.getGuild();
        if (guild == null) {
            throw new Exception("Could not find guild");
        }

        var guildRoles = guild
            .getRoles()
            .stream()
            .filter(r -> config.roleIds.contains(r.getIdLong()))
            .toList();
        var members = guild.getMembersWithRoles(guildRoles);
        var players = new ArrayList<String>();

        for (var member : members) {
            var activities = member.getActivities();
            if (activities.stream().anyMatch(a -> Objects.equals(a.getName(), ""))) {
                players.add(member.getEffectiveName());
            }
        }

        if (players.isEmpty()) {
            event
                .reply(config.messages.noOnline)
                .queue();
        } else {
            Collections.sort(players);
            var embeds = new ArrayList<MessageEmbed>();
            var embed = new EmbedBuilder()
                .setTitle(String.format("%s (%d)", config.messages.current, players.size()))
                .setDescription(StringUtils.join(players, ", "));
            embeds.add(embed.build());
            event
                .reply(MessageCreateData.fromEmbeds(embeds))
                .queue();
        }
    }
}
