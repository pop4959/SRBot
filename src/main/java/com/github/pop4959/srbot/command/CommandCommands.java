package com.github.pop4959.srbot.command;

import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.util.EmbedTemplates;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommandCommands extends BotCommand {

    public CommandCommands() {
        super("commands");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        List<String> commands = new ArrayList<>();
        for (Object o : ((JSONObject) Data.asJSON(COMMANDS_FILE, "")).keySet()) {
            if (((String) BotCommandHandler.getCommand((String) o).getProperty("hidden")).equalsIgnoreCase("false"))
                commands.add((String) o);
        }
        event.getChannel().sendMessage(EmbedTemplates.plaintext("Commands", commands.toString().replaceAll("[\\[\\]]", "")).build()).queue();
    }

}
