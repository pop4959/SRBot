package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.core.exceptions.BadRequestException;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandPoints extends BotCommand {

    private static final Pair<String, Integer>[] BOUNDARIES = new Pair[]{Pair.of("Entry", 0), Pair.of("Beginner", 8000), Pair.of("Advanced", 9000), Pair.of("Expert", 10000), Pair.of("Bronze", 12000), Pair.of("Silver", 17000), Pair.of("Gold", 20000), Pair.of("Platinum", 24000), Pair.of("Diamond", 29000)};
    private static final String[] RANK_EMOTES = new String[]{"<:entry_league:408407631078621194>", "<:beginner_league:408407630755659778>", "<:advanced_league:408407631057911808>", "<:expert_league:408407630759854081>", "<:bronze_league:408407631007318016>", "<:silver_league:408407630864842755>", "<:gold_league:408407631149924352>", "<:platinum_league:408407631041134593>", "<:diamond_league:408407631120826368>", "<:king_of_speed:408407630931820545>"};
    private static final String KOS = "76561198166738162";

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
            StringBuilder jsonStringBuilder = new StringBuilder();
            try {
                URL url = new URL("http://api.speedrunners.doubledutchgames.com/GetRanking?id=" + id);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                int character;
                while ((character = in.read()) > -1)
                    jsonStringBuilder.append((char) character);
            } catch (IOException e) {
                event.getChannel().sendMessage("The requested user does not own the game, is unranked, or has their profile set to private.").queue();
                return;
            }
            String jsonString = jsonStringBuilder.toString();
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
                SteamPlayerProfile steamProfile = user.getPlayerProfile(Long.parseLong(id)).get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
                event.getChannel().sendMessage(EmbedTemplates.points("Off-season: " + jsonData.getInt("score") + " points (" + RANK_EMOTES[id.equals(KOS) ? 9 : jsonData.getInt("tier")] + BOUNDARIES[jsonData.getInt("tier")].getLeft() + " League )\nWinter season: " + (new DecimalFormat("#.##")).format(jsonData.getDouble("rating")) + " points (" + RANK_EMOTES[id.equals(KOS) ? 9 : eloTier] + BOUNDARIES[eloTier].getLeft() + " League )", "Points for " + steamProfile.getName(), steamProfile.getProfileUrl(), steamProfile.getAvatarFullUrl()).build()).queue();
            } catch (BadRequestException | InterruptedException | ExecutionException | TimeoutException e) {
                event.getChannel().sendMessage("Unable to retrieve data for the requested user.").queue();
            }
        } else {
            event.getChannel().sendMessage("Please provide a Steam ID or vanity URL.").queue();
        }
    }

}
