package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Changelog extends Command {
    private final Config config;

    public Changelog(Config config) {
        super("changelog", "Get link to the official SpeedRunners changelog");
        this.config = config;
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        event
            .reply(config.messages.changelog)
            .queue();
    }
}
