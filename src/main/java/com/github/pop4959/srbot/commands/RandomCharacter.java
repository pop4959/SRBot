package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RandomCharacter extends Command {
    private final Config config;

    public RandomCharacter(Config config) {
        super("random_character", "Pick a random character");
        this.config = config;
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        List<String> characters = config.characters;
        double randomIdx = Math.random() * characters.size();
        String randomCharacter = characters.get((int)randomIdx);
        event
            .reply(randomCharacter)
            .queue();
    }
}
