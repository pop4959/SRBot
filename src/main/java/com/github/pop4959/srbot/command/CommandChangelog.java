package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.LinkedHashMap;

public class CommandChangelog extends BotCommand {

    private static final LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();

    public CommandChangelog() {
        super("changelog");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(LANGUAGE.get("changelog")).queue();
    }

}
