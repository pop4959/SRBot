package com.github.pop4959.srbot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Pop4959 extends Command {
    public Pop4959() {
        super("pop4959", "Annoys pop");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws Exception {
        var emojis = new String[]{
            "❤",
            "\uD83D\uDC99",
            "\uD83D\uDC9C",
            "\uD83D\uDC9A",
            "\uD83D\uDC9B",
            "\uD83D\uDC96"
        };
        event.reply(emojis[(int) (Math.random() * 6)] + "<@208016650757341196>").queue();
    }
}
