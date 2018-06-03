package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamPlayerService;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandHours extends BotCommand {

    public CommandHours() {
        super("hours");
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
                service.getOwnedGames(Long.parseLong(id), true, false).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS).stream().filter(game -> game.getAppId() == Data.config().getSrAppId()).forEach(game -> event.getChannel().sendMessage(steamProfile.getName() + " has played " + (game.getTotalPlaytime() / 60) + " hours, " + (game.getTotalPlaytime() % 60) + " minutes of SpeedRunners.").queue());
            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                event.getChannel().sendMessage("Unable to fetch the requested player's hours.").queue();
            }
        } else {
            event.getChannel().sendMessage("Please provide a Steam ID or vanity URL.").queue();
        }
    }

}
