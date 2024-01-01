package com.github.pop4959.srbot.services;

import com.github.pop4959.srbot.Logger;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;

public class ChatLog extends ListenerAdapter {
    private final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
    private final File ATTACHMENT_LOCATION = new File("attachments/");
    private final Logger logger;

    public ChatLog(Logger logger) {
        this.logger = logger;
        LOG_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        logger.log(
            "%s | %s | %s: %s".formatted(
                LOG_DATE_FORMAT.format(Date.from(Instant.now())),
                event.getChannelType() == ChannelType.TEXT
                    ? event.getChannel().getName()
                    : event.getChannel().getId(),
                event.getAuthor().getName(),
                event.getMessage().getContentRaw()
            ),
            "cf"
        );
        if (!ATTACHMENT_LOCATION.exists()) {
            if (!ATTACHMENT_LOCATION.mkdir()) {
                return;
            }
        }
        if (ATTACHMENT_LOCATION.getFreeSpace() > 1e10) {
            for (var attachment : event.getMessage().getAttachments()) {
                attachment.getProxy().downloadToFile(
                    Paths.get(
                        ATTACHMENT_LOCATION.getAbsolutePath(),
                        "%s-%s".formatted(
                            UUID.randomUUID(),
                            attachment.getFileName()
                        )
                    ).toFile()
                );
            }
        }
    }
}
