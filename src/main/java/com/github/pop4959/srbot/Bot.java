package com.github.pop4959.srbot;

import com.github.pop4959.srbot.commands.Command;
import com.github.pop4959.srbot.data.Config;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class Bot extends ListenerAdapter {

    private final ArrayList<Command> commands;
    private final Config config;
    private final Logger logger;

    public Bot(
        ArrayList<Command> commands,
        Config config,
        Logger logger
    ) {
        this.commands = commands;
        this.config = config;
        this.logger = logger;
    }

    public void start() {
        System.out.println();
        var jda = JDABuilder
            .createDefault(config.files.discordToken)
            .setAutoReconnect(true)
            .setActivity(Activity.listening(config.gameName))
            .addEventListeners(this)
            .build();
        jda
            .updateCommands()
            .addCommands(
                Commands
                    .slash("playtime", "xdd")
                    .addOption(OptionType.STRING, "asd", "aa", true)
                    .addOption(OptionType.STRING, "zxc", "Time unit to display. Hours by default")
            )
            .queue();

        // if they already exist
//        commands.forEach(c -> jda.upsertCommand(c.name, c.description).queue());
        jda.upsertCommand("playtime", "xdd").queue();
    }

    @Override
    public void onReady(ReadyEvent event) {
        logger.log("Connected to Discord", "tf");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            var name = event.getName();
            var command = commands
                .stream()
                .filter(c -> Objects.equals(c.name, name))
                .findFirst()
                .get();
            command.execute(event);
        } catch (Exception e) {
            logger.log(e.getMessage(), "cf");
        }
    }
}
