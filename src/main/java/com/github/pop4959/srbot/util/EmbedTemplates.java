package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.Data;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;

public class EmbedTemplates {

    public static EmbedBuilder plaintext(String title, String description) {
        return (new EmbedBuilder()).setColor(new Color((int) Data.asJSON("embedColor.r"), (int) Data.asJSON("embedColor.g"), (int) Data.asJSON("embedColor.b"))).setAuthor(null, null, null).setTitle(title).setDescription(description);
    }

}
