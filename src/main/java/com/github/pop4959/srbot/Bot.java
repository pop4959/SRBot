package com.github.pop4959.srbot;

import com.github.pop4959.srbot.commands.Command;
import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
            .createDefault(config.secrets.discordToken)
            .setAutoReconnect(true)
            .setActivity(Activity.customStatus(config.botStatus))
            .addEventListeners(this)
            .build();
        jda
            .updateCommands()
            .addCommands(commands.stream().map(Command::getSlashCommand).toList())
            .queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
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
            e.printStackTrace();
            logger.log(e.getMessage(), "cf");
        }
    }
}
