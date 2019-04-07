package com.github.pop4959.srbot.task;

import com.github.pop4959.srbot.Logger;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class Chatlog extends ListenerAdapter {

    private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
    private static final File ATTACHMENT_LOCATION = new File("attachments/");

    static {
        LOG_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Logger.log(
                String.format(
                        "%s | %s | %s: %s",
                        LOG_DATE_FORMAT.format(Date.from(Instant.now())),
                        event.getChannelType() == ChannelType.TEXT
                                ? event.getChannel().getName()
                                : event.getChannel().getId(),
                        event.getAuthor().getName(),
                        event.getMessage().getContentRaw()
                ), "cf"
        );
        if (!ATTACHMENT_LOCATION.exists()) {
            if (!ATTACHMENT_LOCATION.mkdir()) {
                return;
            }
        }
        if (ATTACHMENT_LOCATION.getFreeSpace() > 1e10) {
            for (Message.Attachment attachment : event.getMessage().getAttachments()) {
                attachment.download(
                        Paths.get(
                                ATTACHMENT_LOCATION.getAbsolutePath(),
                                String.format(
                                        "%s-%s",
                                        UUID.randomUUID().toString(),
                                        attachment.getFileName()
                                )
                        ).toFile()
                );
            }
        }
    }

}