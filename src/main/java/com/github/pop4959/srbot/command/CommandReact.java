package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandReact extends BotCommand {

	public CommandReact() {
		super("react");
	}

	public void execute(MessageReceivedEvent event, String[] args) {
		long channelId, messageId;
		if (args.length == 4) {
			Guild guild = event.getJDA().getGuildById(args[0]);
			if (guild == null) {
				event.getChannel().sendMessage("Invalid guild ID to react to.").queue();
				return;
			}
			try {
				channelId = Long.parseLong(args[1]);
			} catch (NumberFormatException e) {
				event.getChannel().sendMessage("Invalid channel ID to react to.").queue();
				return;
			}
			try {
				messageId = Long.parseLong(args[2]);
			} catch (NumberFormatException e) {
				event.getChannel().sendMessage("Invalid message ID to react to.").queue();
				return;
			}
			try {
				final long reactId = Long.parseLong(args[3]);
				guild.getTextChannelById(channelId).getMessageById(messageId).queue(message -> message.addReaction(event.getJDA().getEmoteById(reactId)).queue());
			} catch (NumberFormatException e) {
				List<Emote> matchingEmotes = guild.getEmotesByName(args[3], true);
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
