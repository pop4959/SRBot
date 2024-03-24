package com.github.pop4959.srbot.models;

import java.util.List;

public class Config {

    public String botStatus;
    public int srAppId;
    public int queryTimeout;
    public Servers servers;
    public long mainChannel;
    public Logging logging;
    public Secrets secrets;
    public List<String> characters;
    public List<Long> autoRankBlacklist;
    public List<String> roleKeys;
    public List<Long> roleIds;
    public List<String> rankEmojis;
    public String kingOfSpeedSteam;
    public List<LocalizedStat> localizedStats;
    public Messages messages;
    public ApiUrl apiUrl;
}
