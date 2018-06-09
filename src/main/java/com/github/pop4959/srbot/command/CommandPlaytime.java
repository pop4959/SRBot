package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamPlayerService;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandPlaytime extends BotCommand {

    public CommandPlaytime() {
        super("playtime");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            SteamUser user = new SteamUser(Main.getClient());
            SteamPlayerService service = new SteamPlayerService(Main.getClient());
            String id = Steam.resolveID64(user, args[0]);
            if (id == null) {
                event.getChannel().sendMessage("Invalid Steam ID or vanity URL provided.").queue();
                return;
            }
            try {
                SteamPlayerProfile steamProfile = user.getPlayerProfile(Long.parseLong(id)).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
                service.getOwnedGames(Long.parseLong(id), true, false).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS).stream().filter(game -> game.getAppId() == Data.config().getSrAppId()).forEach(game -> event.getChannel().sendMessage(steamProfile.getName() + " has played " + makeTimeString(args.length > 1 ? args[1] : "none", game.getTotalPlaytime()) + " of SpeedRunners.").queue());
            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                event.getChannel().sendMessage("Unable to fetch the requested player's time spent.").queue();
            }
        } else {
            event.getChannel().sendMessage("Please provide a Steam ID or vanity URL.").queue();
        }
    }

    private String makeTimeString(int minutes) {
        if (minutes < 60)
            return (minutes + " minutes");
        else if (minutes < 1440)
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 60) + " hours");
        else if (minutes < 10080)
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 1440) + " days");
        else if (minutes < 43200)
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 10080) + " weeks");
        else if (minutes < 525600)
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 43200) + " months");
        else
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 525600) + " years");
    }

    private String makeTimeString(String preference, int minutes) {
        if ("minutes".equalsIgnoreCase(preference))
            return (minutes + " minutes");
        else if ("hours".equalsIgnoreCase(preference))
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 60) + " hours");
        else if ("days".equalsIgnoreCase(preference))
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 1440) + " days");
        else if ("weeks".equalsIgnoreCase(preference))
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 10080) + " weeks");
        else if ("months".equalsIgnoreCase(preference))
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 43200) + " months");
        else if ("years".equalsIgnoreCase(preference))
            return ((new DecimalFormat("#.##")).format(((double) minutes) / 525600) + " years");
        else
            return makeTimeString(minutes);
    }

}
