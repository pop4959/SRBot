package com.github.pop4959.srbot.data;

import com.github.pop4959.srbot.models.Language;

import java.util.List;

public class Config {

    public String gameName;
    public int srAppId;
    public int queryTimeout;
    public Servers servers;
    public Logging logging;
    public Files files;
    public List<String> characters;
    public List<Long> autoRankBlacklist;
    public List<String> roleKeys;
    public List<Long> roleIds;
    public List<String> rankEmotes;
    public String kingOfSpeedSteam;
    public long kingOfSpeedDiscord;
    public Language language;

    public class Servers {

        public long main;
        public long admin;
    }

    public class Logging {

        public long server;
        public long channel;

    }

    public class Files {

        public String discordToken;
        public String steamToken;
        public String commands;

    }
}
