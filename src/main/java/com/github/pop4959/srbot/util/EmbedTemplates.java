package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;

public class EmbedTemplates {

    public static EmbedBuilder plaintext(String title, String description) {
        return (new EmbedBuilder()).setColor(new Color(Data.config().getEmbedColor().getR(), Data.config().getEmbedColor().getG(), Data.config().getEmbedColor().getB())).setAuthor(null, null, null).setTitle(title).setDescription(description);
    }

}
