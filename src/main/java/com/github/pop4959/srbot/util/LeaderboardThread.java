package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.data.Data;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LeaderboardThread extends Thread{

	private final JSONObject object;
	private final SteamUser user;
	private final SteamPlayerProfile steamProfile;
	private String message = null;
	private final List<String> list;
	private static final LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

	public LeaderboardThread(JSONObject a, SteamUser b, SteamPlayerProfile c, List<String> d) {
		object = a;
		user = b;
		steamProfile = c;
		list = d;
	}

	@Override
	public void run() {
		try {
			String points;
			NumberFormat nf = NumberFormat.getInstance(new Locale("en", "GB"));
			SteamPlayerProfile userProfile = user.getPlayerProfile(Long.parseLong(object.getString("steamID")))
					.get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);

			String embold = steamProfile.getName().equals(userProfile.getName()) ? "**" : "";
//			URL currentSeasonUrl = Steam.getUrl(LANGUAGE.get("apiRank") + userProfile.getSteamId());
//			URLConnection currentSeasonConn = Steam.getConnection(Steam.getUrl(LANGUAGE.get("apiRank") + userProfile.getSteamId()));
			String currentSeasonJson = Steam.getJson(Steam.getConnection(Steam.getUrl(LANGUAGE.get("apiRank") + userProfile.getSteamId())));
//			boolean isNull = Steam.checkNull(currentSeasonUrl, currentSeasonConn, currentSeasonJson);
			if (currentSeasonJson == null)
				throw new NullPointerException("Is null");
			JSONObject currentSeasonData = new JSONObject(currentSeasonJson);
			points = nf.format(currentSeasonData.getInt("score"));
			message = String.format("%s%d. %s %s%s%n", embold, object.getInt("rank"), userProfile.getName(), points, embold);
		} catch (NullPointerException e) {
			SteamPlayerProfile userProfile = null;
			try {
				userProfile = user.getPlayerProfile(Long.parseLong(object.getString("steamID")))
						.get(Data.config().getQueryTimeout(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException ex) {
				ex.printStackTrace();
			}
			String embold = steamProfile.getName().equals(userProfile.getName()) ? "**" : "";
			message = String.format("%s%d. %s%s%n", embold, object.getInt("rank"), userProfile.getName(), embold);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			System.out.println(e.getMessage());
		}
		synchronized (list) {
			list.add(message);
		}
	}
}
