package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Data;
import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.core.exceptions.BadRequestException;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandPoints extends BotCommand {

    private static final Pair<String, Integer>[] BOUNDARIES = new Pair[]{Pair.of("Entry", 0), Pair.of("Beginner", 800), Pair.of("Advanced", 900), Pair.of("Expert", 1000), Pair.of("Bronze", 1200), Pair.of("Silver", 1700), Pair.of("Gold", 2000), Pair.of("Platinum", 2400), Pair.of("Diamond", 2900)};

    public CommandPoints() {
        super("points");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            SteamUser user = new SteamUser(Main.getClient());
            String id = Steam.resolveID64(user, args[0]);
            if (id == null) {
                event.getChannel().sendMessage("Invalid Steam ID or vanity URL provided.").queue();
                return;
            }
            String jsonString = "";
            try {
                URL url = new URL("http://api.speedrunners.doubledutchgames.com/GetRanking?id=" + id);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                int character;
                while ((character = in.read()) > -1) {
                    jsonString += ((char) character);
                }
            } catch (IOException e) {
                event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
                return;
            }
            if (!jsonString.contains("{")) {
                event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
                return;
            }
            JSONObject jsonData = new JSONObject(jsonString);
            int eloTier = 0;
            while (eloTier < 8 && BOUNDARIES[eloTier + 1].getRight() <= jsonData.getDouble("rating")) {
                eloTier++;
            }
            try {
                event.getChannel().sendMessage(EmbedTemplates.plaintext("Points for " + user.getPlayerProfile(Long.parseLong(id)).get(Integer.parseInt(Data.fromJSON("queryTimeout")), TimeUnit.MILLISECONDS).getName(), "Beta season: " + jsonData.getDouble("rating") + " points [" + BOUNDARIES[eloTier].getLeft() + " League]\nOff-season: " + jsonData.getInt("score") + " points [" + BOUNDARIES[jsonData.getInt("tier")].getLeft() + " League]").build()).queue();
            } catch (BadRequestException | InterruptedException | ExecutionException | TimeoutException e) {
                event.getChannel().sendMessage("Unable to retrieve data for the requested user.").queue();
                return;
            }
        } else {
            event.getChannel().sendMessage("Please provide a Steam ID or vanity URL.").queue();
        }
    }

}
