package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;


public class CommandMatchmaker extends BotCommand {

    private static final List<Long> RANK_ROLES = Data.config().getRoleIds();

    public CommandMatchmaker() {
        super("matchmaker");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        Role scanRole = getMemberRankRole(event.getMember());
        if (scanRole == null) {
            event.getChannel().sendMessage("You must have a rank role in order to use this command.").queue();
            return;
        }
        Set<Member> lobby = new HashSet<>();
        lobby.add(event.getMember());
        findPlayersForLobby(lobby, guild.getMembersWithRoles(scanRole));
        int rank = RANK_ROLES.indexOf(scanRole.getIdLong()), distance = 1;
        while (lobby.size() < 4 && distance < 9) {
            List<Member> players = new ArrayList<>();
            if (rank - distance >= 0)
                players.addAll(guild.getMembersWithRoles(guild.getRoleById(RANK_ROLES.get(rank - distance))));
            if (rank + distance < 9)
                players.addAll(guild.getMembersWithRoles(guild.getRoleById(RANK_ROLES.get(rank + distance))));
            findPlayersForLobby(lobby, players);
            ++distance;
        }
        StringBuilder names = new StringBuilder();
        int i = 0;
        for (Member member : lobby) {
            names.append(member.getEffectiveName() + " (" + getMemberRankRole(member).getName() + ")" + (++i == lobby.size() ? "" : ", "));
        }
        event.getChannel().sendMessage("Suggested lobby: " + names).queue();
    }

    private void findPlayersForLobby(Set<Member> lobby, List<Member> pool) {
        Collections.shuffle(pool);
        for (Member member : pool) {
            Game game = member.getGame();
            if (game != null && "SpeedRunners".equals(game.getName())) {
                lobby.add(member);
                if (lobby.size() == 4)
                    return;
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

}
