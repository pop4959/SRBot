package com.github.pop4959.srbot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import static com.github.pop4959.srbot.constants.CommandFields.*;

public class Say extends Command {
    public Say() {
        super("say", "Speak through SRBot");
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return super.getSlashCommand()
            .addOption(OptionType.STRING, MESSAGE_FIELD_NAME, MESSAGE_FIELD_DESC, true)
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws Exception {
        String message = event.getOption(MESSAGE_FIELD_NAME, OptionMapping::getAsString);
        if (message != null) {
            event.reply(message).queue();
        }
    }
}
