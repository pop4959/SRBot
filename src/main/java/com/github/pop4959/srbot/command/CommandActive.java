package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class CommandActive extends BotCommand {

    private static final List<Long> RANK_ROLES = Data.config().getRoleIds();
    private static LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

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
            event.getChannel().sendMessage(LANGUAGE.get("noOnline")).queue();
        } else {
            Collections.sort(players);
            event.getChannel().sendMessage(EmbedTemplates.plaintext(guild,
                    String.format("%s (%d)", LANGUAGE.get("current"), players.size()),
                    StringUtils.join(players, ", ")).build()).queue();
        }
    }

}
