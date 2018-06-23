package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandReact extends BotCommand {

    private static final long SERVER = Data.config().getServers().getMain();

    public CommandReact() {
        super("react");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        Guild guild = event.getJDA().getGuildById(SERVER);
        long channelId, messageId;
        if (args.length == 3) {
            try {
                channelId = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Invalid channel ID to react to.").queue();
                return;
            }
            try {
                messageId = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Invalid message ID to react to.").queue();
                return;
            }
            try {
                final long reactId = Long.parseLong(args[2]);
                guild.getTextChannelById(channelId).getMessageById(messageId).queue(message -> message.addReaction(event.getJDA().getEmoteById(reactId)).queue());
            } catch (NumberFormatException e) {
                List<Emote> matchingEmotes = guild.getEmotesByName(args[2], true);
                if (matchingEmotes.isEmpty()) {
                    event.getChannel().sendMessage("Invalid reaction ID or name.").queue();
                } else {
                    guild.getTextChannelById(channelId).getMessageById(messageId).queue(message -> message.addReaction(matchingEmotes.get(0)).queue());
                }
            }
        } else {
            event.getChannel().sendMessage("Invalid command usage.").queue();
        }
    }

}
