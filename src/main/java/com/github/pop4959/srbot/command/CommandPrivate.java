package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Map;

public class CommandPrivate extends BotCommand {

    private static final Map<String, String> LANGUAGE = Data.config().getLanguage();

    public CommandPrivate() {
        super("private");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(EmbedTemplates.empty(event.getGuild()).setImage(LANGUAGE.get("imgUrl")).build()).queue();
    }
}
