package com.github.pop4959.srbot.models;

import java.util.List;

public class LocalizedStat {
    public String group;
    public List<LocalizedStatItem> items;

    public class LocalizedStatItem {
        public String key;
        public String value;
    }
}

