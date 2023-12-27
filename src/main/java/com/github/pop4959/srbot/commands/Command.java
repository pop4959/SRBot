package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class Command {
    public final String name;
    public final String description;
    protected Command(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public abstract void execute(SlashCommandInteractionEvent event) throws Exception;
    public SlashCommandData getSlashCommand() {
        return Commands.slash(name, description);
    }
}
