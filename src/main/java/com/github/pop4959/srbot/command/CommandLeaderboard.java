package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import com.github.pop4959.srbot.util.LeaderboardThread;
import com.github.pop4959.srbot.util.Steam;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLeaderboard extends BotCommand {
	private static final LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

	public CommandLeaderboard() {
		super("leaderboard");
	}

	public void execute(MessageReceivedEvent event, String[] args) {
		long startTime = System.nanoTime();

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

		int tmp = 0;
		int size = leaderboardEntries.length();
		Thread[] tab = new Thread[size];
		List<String> list = new ArrayList<>();

		for (Object o : leaderboardEntries) {
			JSONObject entry = (JSONObject) o;
			tab[tmp] = new LeaderboardThread(entry, user, steamProfile, list);
			tab[tmp++].start();
		}
		for (byte i = 0; i < size; ++i) {
			try {
				tab[i].join();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return;
			}
		}

		list.sort(Comparator.comparingInt(str -> {
			String formatted;
			Pattern normal = Pattern.compile("^(\\d{1,12})\\.\\s.+"),
					asterix = Pattern.compile("^\\*\\*(\\d{1,12})\\.\\s.+");
			Matcher matcher1 = normal.matcher(str),
					matcher2 = asterix.matcher(str);

			if (matcher1.find())
				formatted = matcher1.group(1);
			else if (matcher2.find())
				formatted = matcher2.group(1);
			else
				return 0;

			return Integer.parseInt(formatted);
		}));

		for (String a : list) message.append(a);

		event.getChannel().sendMessage(EmbedTemplates.points(
				event.getGuild(),
				message.toString(),
				String.format("Leaderboard for %s", steamProfile.getName()),
				steamProfile.getProfileUrl(),
				steamProfile.getAvatarFullUrl()
		).build()).queue();

		// WRITING

		long end = System.nanoTime() - startTime;
		System.out.println(end);

		Path path = Paths.get("G:\\exectime1.txt");
		String content = "";
		try {
			content = new String(Files.readAllBytes(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Files.write(path, (content + end + "\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}