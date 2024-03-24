package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Active extends Command {
    private final Config config;

    public Active(Config config) {
        super("active", "Get active users playing SpeedRunners");
        this.config = config;
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws Exception {
        Guild guild = event.getGuild();
        if (guild == null) {
            throw new Exception("Could not find guild");
        }

        List<Role> guildRoles = guild
            .getRoles()
            .stream()
            .filter(r -> config.roleIds.contains(r.getIdLong()))
            .toList();
        List<Member> members = guild.getMembersWithRoles(guildRoles);
        ArrayList<String> players = new ArrayList<String>();

        for (Member member : members) {
            List<Activity> activities = member.getActivities();
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
            ArrayList<MessageEmbed> embeds = new ArrayList<MessageEmbed>();
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(String.format("%s (%d)", config.messages.current, players.size()))
                .setDescription(StringUtils.join(players, ", "));
            embeds.add(embed.build());
            event
                .reply(MessageCreateData.fromEmbeds(embeds))
                .queue();
        }
    }
}
