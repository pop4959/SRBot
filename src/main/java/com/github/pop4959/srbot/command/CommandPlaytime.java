package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.Utils;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamPlayerService;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandPlaytime extends BotCommand {

    private static LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

    public CommandPlaytime() {
        super("playtime");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            SteamUser user = new SteamUser(Main.getClient());
            SteamPlayerService service = new SteamPlayerService(Main.getClient());
            String id = Utils.resolveID64(user, args[0]);
            if (id == null) {
                event.getChannel().sendMessage(LANGUAGE.get("wrongId")).queue();
                return;
            }
            try {
                SteamPlayerProfile steamProfile = user.getPlayerProfile(
                        Long.parseLong(id)).get(Data.config().getQueryTimeout(),
                        TimeUnit.MILLISECONDS
                );
                service.getOwnedGames(Long.parseLong(id), true, false)
                        .get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS).stream()
                        .filter(game -> game.getAppId() == Data.config().getSrAppId())
                        .forEach(game -> event.getChannel().sendMessage(
                                String.format("%s has played %s of SpeedRunners.",
                                        steamProfile.getName(),
                                        makeTimeString(args.length > 1 ? args[1] : "none", game.getTotalPlaytime())
                                )).queue());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                event.getChannel().sendMessage(LANGUAGE.get("unableTime")).queue();
            }
        } else {
            event.getChannel().sendMessage(LANGUAGE.get("noId")).queue();
        }
    }

    private String makeTimeString(int minutes) {
        double mins = (double) minutes;
        DecimalFormat df = new DecimalFormat("#.##");
        if (minutes < 60)
            return String.format("%d minutes", minutes);
        else if (minutes < 1440)
            return df.format(mins / 60) + " hours";
        else if (minutes < 10080)
            return df.format(mins / 1440) + " days";
        else if (minutes < 43200)
            return df.format(mins / 10080) + " weeks";
        else if (minutes < 525600)
            return df.format(mins / 43200) + " months";
        else
            return df.format(mins / 525600) + " years";
    }

    private String makeTimeString(String preference, int minutes) {
        double mins = (double) minutes;
        DecimalFormat df = new DecimalFormat("#.##");
        String pref = preference.toLowerCase();
        switch (pref) {
            case "minutes":
                return String.format("%d minutes", minutes);
            case "hours":
                return df.format(mins / 60) + " hours";
            case "days":
                return df.format(mins / 1440) + " days";
            case "weeks":
                return df.format(mins / 10080) + " weeks";
            case "months":
                return df.format(mins / 43200) + " months";
            case "years":
                return df.format(mins / 525600) + " years";
            default:
                return makeTimeString(minutes);
        }
    }

}
