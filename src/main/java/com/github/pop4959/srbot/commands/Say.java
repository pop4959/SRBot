package com.github.pop4959.srbot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import static com.github.pop4959.srbot.constants.CommandFieldNames.SAY_FIELD;

public class Say extends Command {
    public Say() {
        super("say", "Speak through SRBot");
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands
            .slash(name, description)
            .addOption(OptionType.STRING, "message", "Message to send", true)
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws Exception {
        var message = event.getOption(SAY_FIELD, OptionMapping::getAsString);
        if (message != null) {
            event.reply(message).queue();
        }
    }
}
