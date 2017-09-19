package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandPop4959 extends BotCommand {

    public CommandPop4959() {
        super("pop4959");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        String[] emojis = new String[]{"\u2764", "\uD83D\uDC99", "\uD83D\uDC9C", "\uD83D\uDC9A", "\uD83D\uDC9B", "\uD83D\uDC96"};
        event.getChannel().sendMessage(emojis[(int) (Math.random() * 6)] + "<@208016650757341196>").queue();
    }

}
