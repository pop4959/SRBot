package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.core.exceptions.BadRequestException;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUserStats;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandStats extends BotCommand {

    private static Map<String, String> NAMES = new HashMap<>();
    private static Map<String, Integer> VALUES = new HashMap<>();
    private static String[] CODES = new String[]{"weapon_hook_shot", "weapon_hook_hit", "weapon_hook_dodged", "weapon_bomb_planted", "weapon_bomb_victims", "weapon_bomb_suicides", "weapon_shockwave_fired", "weapon_shockwave_victims", "weapon_shockwave_obstacles_removed", "weapon_crate_dropped", "weapon_crate_suicide", "weapon_crate_victims", "weapon_crate_air_victims", "weapon_crate_rocket_victims", "weapon_rocket_shot", "weapon_rocket_victims", "weapon_rocket_suicide", "weapon_drill_used", "weapon_drill_victims", "weapon_drill_obstacles_removed", "game_wins", "distance_traveled", "jumps", "obstacles_hit", "overtakes", "slide_tackles", "slide_tackles_in_air", "hooks_shot", "hooks_hit"};

    static {
        String[] labels = new String[]{"Shot", "Hit", "Dodged", "Planted", "Victims", "Suicides", "Fired", "Victims", "Obstacles Removed", "Dropped", "Suicides", "Victims", "Victims in Air", "Rockets Blocked", "Fired", "Victims", "Suicides", "Used", "Victims", "Obstacles Removed", "Wins", "Distance", "Jumps", "Obstacles Hit", "Overtakes", "Tackles", "Tackles in Air", "Shot", "Hit"};
        for (int i = 0; i < labels.length; ++i)
            NAMES.put(CODES[i], labels[i]);
    }

    public CommandStats() {
        super("stats");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            SteamUser user = new SteamUser(Main.getClient());
            SteamUserStats steamUserStats = new SteamUserStats(Main.getClient());
            String id = Steam.resolveID64(user, args[0]);
            if (id == null) {
                event.getChannel().sendMessage("Invalid Steam ID or vanity URL provided.").queue();
                return;
            }
            try {
                SteamPlayerProfile steamProfile = user.getPlayerProfile(Long.parseLong(id)).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
                steamUserStats.getUserStatsForGame(Long.parseLong(id), Data.config().getSrAppId()).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS).getStats().forEach(stat -> VALUES.put(stat.getName(), stat.getValue()));
                event.getChannel().sendMessage((new EmbedBuilder().setDescription(groupforCodes(20)).setColor(new Color(Data.config().getEmbedColor().getR(), Data.config().getEmbedColor().getG(), Data.config().getEmbedColor().getB())).addField("Movement", groupforCodes(21, 22, 23, 24), true).addField("Sliding", groupforCodes(25, 26), true).addField("Grappling", groupforCodes(27, 28), true).addField("Golden Hook", groupforCodes(0, 1, 2), true).addField("Bomb", groupforCodes(3, 4, 5), true).addField("Shockwave", groupforCodes(6, 7, 8), true).addField("Crate", groupforCodes(9, 10, 11, 12, 13), true).addField("Rocket", groupforCodes(14, 15, 16), true).addField("Drill", groupforCodes(17, 18, 19), true).setAuthor("Stats for " + steamProfile.getName(), steamProfile.getProfileUrl(), steamProfile.getAvatarFullUrl())).build()).queue();
            } catch (BadRequestException | InterruptedException | ExecutionException | TimeoutException e) {
                event.getChannel().sendMessage("The requested user does not own the game, or has their account's game details set to private.").queue();
            }
        } else {
            event.getChannel().sendMessage("Please provide a Steam ID or vanity URL.").queue();
        }
    }

    String fieldForCodeIndex(int index) {
        String code = CODES[index];
        return NAMES.get(code) + ": " + VALUES.get(code);
    }

    String groupforCodes(int... indices) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i : indices)
            stringBuilder.append(fieldForCodeIndex(i) + "\n");
        return stringBuilder.toString();
    }

}
