package com.github.pop4959.srbot.commands;

import com.github.pop4959.srbot.models.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Private extends Command {
    private final Config config;

    public Private(Config config) {
        super("private", "Get information on how to change your steam account to public");
        this.config = config;
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws Exception {
        ArrayList<MessageEmbed> embeds = new ArrayList<MessageEmbed>();
        EmbedBuilder embed = new EmbedBuilder()
            .setImage(config.messages.imgUrl)
            .setColor(event.getGuild().getSelfMember().getColor());
        embeds.add(embed.build());
        event.replyEmbeds(embeds).queue();
    }
}
