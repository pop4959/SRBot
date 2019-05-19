package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUserStats;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandStats extends BotCommand {

    private static Map<String, Integer> VALUES = new HashMap<>();
    private static LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();
    private static String[] CODES = new String[29];

    static {
        int n = 0;
        Set entrySet = LANGUAGE.entrySet();
        for (Object o : entrySet) {
            String[] kv = o.toString().split("=");
            if (n++ < 29) {
                CODES[n - 1] = kv[0];
            }
        }
    }

    public CommandStats() {
        super("stats");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendTyping().queue();
        if (args.length > 0) {
            SteamUser user = new SteamUser(Main.getClient());
            SteamUserStats steamUserStats = new SteamUserStats(Main.getClient());
            String id = Steam.resolveID64(user, args[0]);
            if (id == null) {
                event.getChannel().sendMessage(LANGUAGE.get("wrongId")).queue();
                return;
            }
            try {
                SteamPlayerProfile steamProfile = user.getPlayerProfile(Long.parseLong(id))
                        .get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
                VALUES.clear();
                steamUserStats.getUserStatsForGame(Long.parseLong(id), Data.config().getSrAppId())
                        .get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS)
                        .getStats().forEach(stat -> VALUES.put(stat.getName(), stat.getValue()));
                event.getChannel().sendMessage(EmbedTemplates.empty(event.getGuild())
                        .setDescription(groupForCodes(20))
                        .addField("Movement", groupForCodes(21, 22, 23, 24), true)
                        .addField("Sliding", groupForCodes(25, 26), true)
                        .addField("Grappling", groupForCodes(27, 28), true)
                        .addField("Golden Hook", groupForCodes(0, 1, 2), true)
                        .addField("Bomb", groupForCodes(3, 4, 5), true)
                        .addField("Shockwave", groupForCodes(6, 7, 8), true)
                        .addField("Crate", groupForCodes(9, 10, 11, 12, 13), true)
                        .addField("Rocket", groupForCodes(14, 15, 16), true)
                        .addField("Drill", groupForCodes(17, 18, 19), true)
                        .setAuthor("Stats for " + steamProfile.getName(), steamProfile.getProfileUrl(), steamProfile.getAvatarFullUrl())
                        .build()).queue();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                event.getChannel().sendMessage(LANGUAGE.get("private")).queue();
            }
        } else {
            event.getChannel().sendMessage(LANGUAGE.get("noId")).queue();
        }
    }
    private String fieldForCodeIndex(int index) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "GB"));
        String code = CODES[index];
        Integer value = VALUES.get(code);
        if (value == null) value = 0;
        return String.format("%s: %s", LANGUAGE.get(code), nf.format(value));
    }

    private String groupForCodes(int... indices) {
        StringBuilder sb = new StringBuilder();
        for (int i : indices) sb.append(fieldForCodeIndex(i)).append("\n");
        return sb.toString();
    }

}