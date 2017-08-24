package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandRandomcharacter extends BotCommand {

    public CommandRandomcharacter() {
        super("randomcharacter");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        List<Object> characters = this.getProperties("characters");
        event.getChannel().sendMessage((String) characters.get((int) (Math.random() * characters.size()))).queue();
    }

}
