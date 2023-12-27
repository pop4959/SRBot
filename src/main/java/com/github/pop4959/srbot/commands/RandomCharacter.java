package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RandomCharacter extends Command {
    private final Config config;

    public RandomCharacter(Config config) {
        super("random_character", "Pick a random character");
        this.config = config;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var characters = config.characters;
        var randomIdx = Math.random() * characters.size();
        var randomCharacter = characters.get((int)randomIdx);
        event
            .reply(randomCharacter)
            .setEphemeral(true)
            .queue();
    }
}
