package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;


public class CommandMatchmaker extends BotCommand {

    private static final List<Long> RANK_ROLES = Data.config().getRoleIds();
    private static final List<String> RANK_EMOTES = Data.config().getRankEmotes();
    private static final long KING_OF_SPEED = Data.config().getKingOfSpeedDiscord();
    private static final LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

    public CommandMatchmaker() {
        super("matchmaker");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        Role scanRole = getMemberRankRole(event.getMember());
        if (scanRole == null) {
            event.getChannel().sendMessage(LANGUAGE.get("noRank")).queue();
            return;
        }
        Set<Member> lobby = new HashSet<>();
        lobby.add(event.getMember());
        findPlayersForLobby(lobby, guild.getMembersWithRoles(scanRole));
        int rank = RANK_ROLES.indexOf(scanRole.getIdLong()), distance = 1;
        while (lobby.size() < 4 && distance < 9) {
            List<Member> players = new ArrayList<>();
            if (rank - distance >= 0) {
                players.addAll(guild.getMembersWithRoles(guild.getRoleById(RANK_ROLES.get(rank - distance))));
            }
            if (rank + distance < 9) {
                players.addAll(guild.getMembersWithRoles(guild.getRoleById(RANK_ROLES.get(rank + distance))));
            }
            findPlayersForLobby(lobby, players);
            ++distance;
        }
        EmbedBuilder embed = EmbedTemplates.title(guild, "Suggested lobby");
        int i = 0;
        for (Member member : lobby) {
            Role role = getMemberRankRole(member);
            embed.addField(member.getEffectiveName(), String.format("%s %s", getMemberRankEmote(member, role), role.getName()), true);
            if (++i % 2 == 0) {
                embed.addBlankField(true);
            }
        }
        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void findPlayersForLobby(Set<Member> lobby, List<Member> pool) {
        Collections.shuffle(pool);
        for (Member member : pool) {
            Game game = member.getGame();
            if (game != null && "SpeedRunners".equals(game.getName())) {
                lobby.add(member);
                if (lobby.size() == 4) {
                    return;
                }
            }
        }
    }

    private Role getMemberRankRole(Member member) {
        for (Role role : member.getRoles()) {
            if (RANK_ROLES.contains(role.getIdLong())) {
                return role;
            }
        }
        return null;
    }

    private String getMemberRankEmote(Member member, Role role) {
        if (member.getUser().getIdLong() == KING_OF_SPEED) {
            return RANK_EMOTES.get(9);
        } else {
            return RANK_EMOTES.get(RANK_ROLES.indexOf(role.getIdLong()));
        }
    }

}