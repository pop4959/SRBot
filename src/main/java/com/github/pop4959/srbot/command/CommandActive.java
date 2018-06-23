package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommandActive extends BotCommand {

    private static final List<Long> RANK_ROLES = Data.config().getRoleIds();

    public CommandActive() {
        super("active");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        List<String> players = new ArrayList<>();
        for (long rankId : RANK_ROLES) {
            for (Member member : guild.getMembersWithRoles(guild.getRoleById(rankId))) {
                Game game = member.getGame();
                if (game != null && "SpeedRunners".equals(game.getName())) {
                    players.add(member.getEffectiveName());
                }
            }
        }
        if (players.isEmpty()) {
            event.getChannel().sendMessage("There are currently no players from this server online in SpeedRunners.").queue();
        } else {
            event.getChannel().sendMessage(EmbedTemplates.plaintext("Currently playing SpeedRunners from this server", StringUtils.join(players, ", ")).build()).queue();
        }
    }

}
