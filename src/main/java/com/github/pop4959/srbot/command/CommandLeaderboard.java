package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class CommandLeaderboard extends BotCommand {
	private static final LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

	public CommandLeaderboard() {
		super("leaderboard");
	}

	public void execute(MessageReceivedEvent event, String[] args) {
		if (args.length < 1) {
			event.getChannel().sendMessage(LANGUAGE.get("noId")).queue();
			return;
		}

		event.getChannel().sendTyping().queue();

		SteamUser user = new SteamUser(Main.getClient());
		String id = Steam.resolveID64(user, args[0]);
		if (id == null) {
			event.getChannel().sendMessage(LANGUAGE.get("wrongId")).queue();
			return;
		}

		URL leaderboardUrl = Steam.getUrl(event, LANGUAGE.get("apiLeaderboard") + id + "&start=-10&end=10");
		URLConnection leaderboardConn = Steam.getConnection(event, leaderboardUrl);
		String leaderboardJson = Steam.getJson(event, leaderboardConn);
		SteamPlayerProfile steamProfile = Steam.getSteamProfile(event, user, id);

		boolean isNull = Steam.checkNull(leaderboardUrl, leaderboardConn, leaderboardJson, steamProfile);
		if (isNull) return;

		StringBuilder message = new StringBuilder();
		JSONArray leaderboardEntries = (new JSONObject(leaderboardJson))
				.getJSONObject("leaderboardEntryInformation")
				.getJSONArray("leaderboardEntries");
		if (leaderboardEntries.length() == 0) {
			event.getChannel().sendMessage(LANGUAGE.get("noPlay")).queue();
			return;
		}

		for (Object o : leaderboardEntries) {
			JSONObject entry = (JSONObject) o;
			try {
				SteamPlayerProfile userProfile = user.getPlayerProfile(Long.parseLong(entry.getString("steamID")))
						.get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
				String embold = steamProfile.getName().equals(userProfile.getName()) ? "**" : "";
				message.append(String.format("%s%d. %s%s\n", embold, entry.getInt("rank"), userProfile.getName(), embold));
			} catch (Exception e) {
				event.getChannel().sendMessage(LANGUAGE.get("errJson")).queue();
				return;
			}
		}

		event.getChannel().sendMessage(EmbedTemplates.points(
				event.getGuild(),
				message.toString(),
				String.format("Leaderboard for %s", steamProfile.getName()),
				steamProfile.getProfileUrl(),
				steamProfile.getAvatarFullUrl()
		).build()).queue();
	}

}