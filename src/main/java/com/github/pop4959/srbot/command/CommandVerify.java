package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class CommandVerify extends BotCommand {

    public CommandVerify() {
        super("verify");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        final String snowflake = user.getId();
        user.openPrivateChannel().queue(privateChannel -> {
            UriBuilder uriBuilder = UriBuilder.fromUri(Data.config().getWeb().getSteamRedirect())
                    .queryParam("context", snowflake.hashCode())
                    .queryParam("snowflake", snowflake);
            URI uri = uriBuilder.build();
            privateChannel.sendMessage(String.format("%s\n%s", "Please login at the following URL to verify your account. Do not share this with anyone!", uri.toString())).queue();
        });
        event.getChannel().sendMessage("You have been sent a link to verify ownership of your account. Once you have done so, your rank roles will be updated automatically!").queue();
    }

}
