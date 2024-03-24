package com.github.pop4959.srbot;

import com.github.pop4959.srbot.commands.Command;
import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class Bot extends ListenerAdapter {

    private final ArrayList<Command> commands;
    private final ArrayList<ListenerAdapter> services;
    private final Config config;
    private final Logger logger;
    private JDA jda;

    public Bot(
        ArrayList<Command> commands,
        ArrayList<ListenerAdapter> services,
        Config config,
        Logger logger
    ) {
        this.commands = commands;
        this.services = services;
        this.config = config;
        this.logger = logger;
    }

    public JDA getJda() {
        return jda;
    }

    public void start() {
        services.add(this);
        jda = JDABuilder
            .createDefault(config.secrets.discordToken)
            .setAutoReconnect(true)
            .setActivity(Activity.customStatus(config.botStatus))
            .enableIntents(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES
            )
            .addEventListeners((Object[]) services.toArray(ListenerAdapter[]::new))
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
            String name = event.getName();
            Command command = commands
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
