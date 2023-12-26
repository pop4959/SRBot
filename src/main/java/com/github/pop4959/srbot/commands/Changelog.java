package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.data.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Changelog extends Command {
    private final Config config;

    public Changelog(Config config) {
        super("changelog", "Get most recent changelog");
        this.config = config;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws Exception {
        event
            .reply(config.language.changelog)
            .setEphemeral(true)
            .queue();
    }
}
