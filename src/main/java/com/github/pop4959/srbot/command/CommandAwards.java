package com.github.pop4959.srbot.command;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CommandAwards extends BotCommand {

    public CommandAwards() {
        super("awards");
    }

    // pop ignore this file

    private static final String IMG = "https://cdn.discordapp.com/attachments/457577772852838411/540855919781871640/yobama.png";
    private static final String CHANNELID = "562288814584758305";
    private static final String[] EMOTES = {
        "562306532289544193"
    };

    private static final String[] MONTHS = {
        "Styczeń",
        "Luty",
        "Marzec",
        "Kwiecień",
        "Maj",
        "Czerwiec",
        "Lipiec",
        "Sierpień",
        "Wrzesień",
        "Październik",
        "Listopad",
        "Grudzień"
    };
    private static final String[] INFO = {
        "```py",
        "def ZASADY:",
        "\t1. Jedno zgłoszenie, na osobę, na miesiąc.",
        "\t2. Oddać głos można po przez reakcje emoji \":emo_vote:\" przy zgłoszeniu.",
        "\t3. Głosowanie trwa kalendarzowy miesiąc.",
        "\t4. Zwycięska emotka zostaje dodana na serwer na kolejny miesiąc z nazwą \":emoawardswinner:\".",
        "\t5. Cykl powtarza się w każdym miesiącu, na koniec roku odbywa się głosowanie na emoji roku.\n",
        "def DODATKOWE INFO:",
        "\t1. Wiadomości z poprzedniego okresu są usuwane.",
        "\t2. Wiadomości niebędące zgłoszeniami mogą być kasowane i uznane za spam!",
        "```"
    };

    public void execute(MessageReceivedEvent event, String[] args) {
        event.getMessage().delete().queue();
        int biggest = 0;
        Message winner = null;
        TextChannel channel = event.getGuild().getTextChannelById(CHANNELID);
        Map<Message, Integer> submissions = new HashMap<>();

        for (Message message : channel.getIterableHistory().cache(false)) {
            int counter = 0;
            List<MessageReaction> reactions = message.getReactions();
            List<Message.Attachment> attachments = message.getAttachments();
            if (attachments.size() != 1 || !attachments.get(0).isImage()) break;

            for (MessageReaction messageReaction: reactions)
                if (messageReaction.getReactionEmote().getId().equals(EMOTES[0]))
                    counter++;

            submissions.put(message, counter);
        }

        for (Map.Entry<Message, Integer> entry : submissions.entrySet()) {
            if (entry.getValue() > biggest) {
                biggest = entry.getValue();
                winner = entry.getKey();
            }
        }

        if (winner != null) {
            Message.Attachment attachment = winner.getAttachments().get(0);
            File file = new File("winner.png");
            attachment.download(file);
            winner.getAttachments().get(0).download(file);
        }

        // STUFF AFTER COUNTING REACTIONS

        String eq = "==========",
            space = "                         ";
        Date date = new Date();
        EmbedBuilder eb = new EmbedBuilder();
        Emote emote = event.getGuild().getEmoteById(EMOTES[0]);
        Calendar cal = Calendar.getInstance(new Locale("pl", "PL"));

        cal.setTime(date);
        int year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH);

        String[] intro = {
            String.format("%s Emoji Awards %s", eq, eq),
            String.format("♚%s%s %d%s♚", space, MONTHS[month], year, space),
            String.format("%s :fa_winkputin: :fa_lul: :fa_success: %s", eq, eq)
        };

        for (Message message : channel.getIterableHistory().cache(false))
            message.delete().queue();

        channel.sendMessage(String.join("\n", intro) + String.join("\n", INFO)).queue();
        eb.setImage(IMG).setTitle("Przykładowe zgłoszenie:");
        RestAction<Message> ra = channel.sendMessage(eb.build());
        Message message = ra.complete();
        message.addReaction(emote).queue();
        event.getChannel().sendMessage((winner == null) ? "No winner" : "Winner - " + winner.getAuthor().getAsMention()).queue();
    }
}
