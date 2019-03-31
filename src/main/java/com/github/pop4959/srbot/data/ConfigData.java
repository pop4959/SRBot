package com.github.pop4959.srbot.data;

import java.util.LinkedHashMap;
import java.util.List;

public class ConfigData {

	private String commandPrefix;
	private String gameName;
	private EmbedColor embedColor;
	private int srAppId;
	private int queryTimeout;
	private Servers servers;
	private long mainChannel;
	private Logging logging;
	private Files files;
	private long voiceCategory;
	private List<String> characters;
	private List<Long> autoRankBlacklist;
	private List<String> roleKeys;
	private List<Long> roleIds;
	private List<String> rankEmotes;
	private String kingOfSpeedSteam;
	private long kingOfSpeedDiscord;
	private String language;

	public class EmbedColor {

		private int r;
		private int g;
		private int b;

		public int getR() {
			return r;
		}

		public int getG() {
			return g;
		}

		public int getB() {
			return b;
		}

	}

	public class Servers {

		private long main;
		private long admin;

		public long getMain() {
			return main;
		}

		public long getAdmin() {
			return admin;
		}

	}

	public class Logging {

		private long server;
		private long channel;

		public long getServer() {
			return server;
		}

		public long getChannel() {
			return channel;
		}

	}

	public class Files {

		private String discordToken;
		private String steamToken;
		private String commands;

		public String getDiscordToken() {
			return discordToken;
		}

		public String getSteamToken() {
			return steamToken;
		}

		public String getCommands() {
			return commands;
		}

	}

	public String fetchLanguage(){
		return language;
	}

	public LinkedHashMap<String, String> getLanguage(){
		String lang = Data.fromFile(Data.config().fetchLanguage());
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		String[] contents = lang.split("\n");
		for (String content : contents) {
			if (content.startsWith("//")) continue;
			String[] kv = content.split("=", 2);
			map.put(kv[0].trim(), kv[1].trim());
		}
		return map;
	}

	public String getCommandPrefix() {
		return commandPrefix;
	}

	public String getGameName() {
		return gameName;
	}

	public EmbedColor getEmbedColor() {
		return embedColor;
	}

	public int getSrAppId() {
		return srAppId;
	}

	public int getQueryTimeout() {
		return queryTimeout;
	}

	public Servers getServers() {
		return servers;
	}

	public long getMainChannel() {
		return mainChannel;
	}

	public Logging getLogging() {
		return logging;
	}

	public Files getFiles() {
		return files;
	}

	public long getVoiceCategory() {
		return voiceCategory;
	}

	public List<String> getCharacters() {
		return characters;
	}

	public List<Long> getAutoRankBlacklist() {
		return autoRankBlacklist;
	}

	public List<String> getRoleKeys() {
		return roleKeys;
	}

	public List<Long> getRoleIds() {
		return roleIds;
	}

	public List<String> getRankEmotes() {
		return rankEmotes;
	}

	public String getKingOfSpeedSteam() {
		return kingOfSpeedSteam;
	}

	public long getKingOfSpeedDiscord() {
		return kingOfSpeedDiscord;
	}

}
